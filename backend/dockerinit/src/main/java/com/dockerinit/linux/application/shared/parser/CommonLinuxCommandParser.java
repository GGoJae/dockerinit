package com.dockerinit.linux.application.shared.parser;

import com.dockerinit.linux.application.autocomplete.model.CommandView;
import com.dockerinit.linux.application.autocomplete.model.ExpectedToken;
import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.domain.syntax.TokenType;
import com.dockerinit.linux.infrastructure.redis.RedisKeys;
import com.dockerinit.linux.infrastructure.repository.LinuxCommandRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.dockerinit.global.constants.Modules.LINUX;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonLinuxCommandParser implements CommandLineParser {

    private final LinuxCommandRepository repository;
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    private static final String MODULE = LINUX;

    @Override
    public ParseResult parse(String line, int cursor, List<ShellTokenizer.Token> tokens) {

        String baseCommand = tokens.isEmpty() ? "" : tokens.get(0).text();
        CommandView view = getCommandView(baseCommand);

        int idx = ShellTokenizer.indexOfTokenAtCursor(cursor, tokens);
        String currentTokenText = (idx >= 0 && idx < tokens.size()) ? tokens.get(idx).text() : "";

        int position = Math.max(0, idx - 1);
        Map<String, Option> options = (view != null) ? view.options() :Map.of();
        String prevFlag = computePrevFlag(tokens, idx, currentTokenText, options);

        if (view == null) {
            List<ExpectedToken> expected = ifNotFoundCommandGetET(currentTokenText);

            return new ParseResult(
                    line, cursor, baseCommand, currentTokenText, idx,
                    prevFlag, null, expected, position
            );
        }

        List<TokenType> types = view.synopsis().expectedTypeAt(position);

        List<ExpectedToken> expected = getExpectedTokens(types, currentTokenText, prevFlag, options);

        return new ParseResult(
                line, cursor, baseCommand, currentTokenText, idx,
                prevFlag, view, expected, position
        );
    }

    private static List<ExpectedToken> ifNotFoundCommandGetET(String currentTokenText) {
        List<ExpectedToken> expected = new ArrayList<>();

        if (!currentTokenText.isEmpty()) {
            // 사용자가 뭔가를 타이핑 중 → COMMAND를 가장 강하게
            expected.add(new ExpectedToken(TokenType.COMMAND, -1, 1.0, Map.of()));

            // 하이픈으로 시작하면 OPTION도 보조로
            if (currentTokenText.startsWith("-")) {
                expected.add(new ExpectedToken(TokenType.OPTION, 1, 0.9, Map.of()));
            }
        } else {
            // 완전 빈 상태 → COMMAND 제안은 있지만 낮은 우선순위
            expected.add(new ExpectedToken(TokenType.COMMAND, 10, 0.5, Map.of()));
        }
        return expected;
    }

    private CommandView getCommandView(String baseCommand) {
        if (baseCommand.isEmpty()) return null;

        // 레디스 미스에 키가 이미 있다면 null
        if (Boolean.TRUE.equals(redis.hasKey(RedisKeys.cmdMiss(MODULE, baseCommand)))) return null;

        String key = RedisKeys.cmdCache(MODULE, baseCommand);
        String json = null;
        try {
            json = redis.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("redis 가져오기 실패 key = {}, error = {}", key, e.toString());
        }

        CommandView view = tryParseJson(json);

        if (view == null) {
            view = loadFromDbAndCache(baseCommand, key);
        }

        return view;
    }

    private CommandView loadFromDbAndCache(String baseCommand, String key) {
        Optional<LinuxCommand> cmdOpt = repository.findByCommand(baseCommand);
        if (cmdOpt.isEmpty()) {
            try {
                redis.opsForValue().set(
                        RedisKeys.cmdMiss(MODULE, baseCommand), "1", Duration.ofMinutes(5));
            } catch (Exception e) {
                log.debug("레디스 miss key 저장 실패: {}", e.toString());
            }
            return null;
        }

        CommandView view = CommandView.of(cmdOpt.get());

        try {
            redis.opsForValue().set(key, mapper.writeValueAsString(view), Duration.ofHours(6));
        } catch (Exception e) {
            log.warn("redis 저장 실패 key={}, err={}", key, e.toString());
        }
        return view;
    }

    private CommandView tryParseJson(String json) {
        if (json == null) return null;

        try {
            return mapper.readValue(json, CommandView.class);
        } catch (Exception e) {
            log.warn("CommandView json parse 실패  error = {}", e.toString());
            return null;
        }
    }

    private static List<ExpectedToken> getExpectedTokens(List<TokenType> types, String currentTokenText, String prevFlag, Map<String, Option> options) {
        List<ExpectedToken> out = new ArrayList<>();

        int p = 10;
        for (TokenType t : types) out.add(new ExpectedToken(t, p++, 1.0, Map.of()));

        boolean hasPrev = prevFlag != null && !prevFlag.isBlank();
        boolean requiresArg = hasPrev && options.containsKey(prevFlag) && options.get(prevFlag).argRequired();

        // prevFlag가 인자 필수라면 ARGUMENT 최우선
        if (requiresArg) {
            out.add(new ExpectedToken(TokenType.ARGUMENT, -3, 1.2, Map.of("prevFlag", prevFlag)));

            // 사용자가 -- 로 옵션 재개 의도를 보이면 OPTION도 낮은 우선순위로 추가
            if (currentTokenText.startsWith("--")) {
                out.add(new ExpectedToken(TokenType.OPTION, 2, 0.7, Map.of()));
            }

            // 하이픈 시작 값( "-", "-5" 등 )은 인자로 더 강하게 본다
            if (currentTokenText.equals("-") || looksLikeNegativeNumber(currentTokenText)) {
                out.add(new ExpectedToken(TokenType.ARGUMENT, -4, 1.25, Map.of("prevFlag", prevFlag)));
                out.add(new ExpectedToken(TokenType.OPTION, 5, 0.5, Map.of())); // 여지는 남겨둠
            }
            return out;
        }

        // prevFlag가 없고 하이픈으로 시작하면 옵션 우선
        if (currentTokenText.startsWith("--")) {
            out.add(new ExpectedToken(TokenType.OPTION, -2, 1.1, Map.of()));
        } else if (currentTokenText.startsWith("-")) {
            out.add(new ExpectedToken(TokenType.OPTION, -1, 1.05, Map.of()));
        }
        return out;
    }

    private static boolean looksLikeNegativeNumber(String s) {
        if (s == null || s.length() < 2 || s.charAt(0) != '-') return false;
        for (int i = 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    private String computePrevFlag(List<ShellTokenizer.Token> tokens, int idx, String cur, Map<String, Option> options) {

        for (int i = idx - 1; i >= 0; i--) {
            String t = tokens.get(i).text();
            if ("|".equals(t) || ">".equals(t) || ">>".equals(t) || "<<".equals(t) || "<".equals(t)) break;
            if (t.startsWith("--")) return "";
            if (t.startsWith("-")) {
                String pending = pendingArgFlagFromCluster(t, options);
                if (!pending.isBlank()) return pending;
                break;
            }
        }
        return "";
    }

    private static String pendingArgFlagFromCluster(String token, Map<String, Option> options) {
        if (token.length() < 2 || token.equals("--")) return "";
        if (!token.startsWith("-") || token.startsWith("--")) return "";

        String letters = token.substring(1); // 예: "-xvf" -> "xvf"
        for (int i = 0; i < letters.length(); i++) {
            String flag = "-" + letters.charAt(i);
            Option o = options.get(flag);
            if (o != null && o.argRequired()) {
                boolean hasRemainderInSameToken = (i < letters.length() - 1);
                // 같은 토큰에 남은 글자가 있으면 이미 인자를 붙여쓴 상태 -> 미결 아님
                return hasRemainderInSameToken ? "" : flag; // 남은 글자가 없으면 다음 토큰이 인자
            }
            // TODO -- 옵션 해결 로직 구현 / 붙어있는 글자를 한글자씩 option 에 대입했는데 없으면 붙어있는 arg 처리
        }
        return "";
    }

}
