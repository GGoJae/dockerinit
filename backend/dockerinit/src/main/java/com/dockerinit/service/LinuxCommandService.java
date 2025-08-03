package com.dockerinit.service;

import com.dockerinit.domain.LinuxCommand;
import com.dockerinit.dto.linuxCommand.*;
import com.dockerinit.exception.CustomException.NotFoundCustomException;
import com.dockerinit.repository.LinuxCommandRepository;
import com.dockerinit.util.RedisKeys;
import com.dockerinit.util.ShellTokenizer;
import com.dockerinit.vo.Linux.AcPhase;
import com.dockerinit.vo.Linux.Suggestion;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.dockerinit.constant.ErrorMessage.LINUX_COMMAND_ID_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LinuxCommandService {

    private final StringRedisTemplate redis;
    private final LinuxCommandRepository repo;

    /* ─────────────────────────────── CRUD API ─────────────────────────────── */

    public LinuxCommandResponse getById(String id) {
        return repo.findById(id)
                .map(LinuxCommandResponse::of)
                .orElseThrow(() -> new NotFoundCustomException(LINUX_COMMAND_ID_NOT_FOUND, id));
    }

    public List<LinuxCommandResponse> getAll() {
        return repo.findAll().stream().map(LinuxCommandResponse::of).toList();
    }

    public LinuxCommandResponse createCommand(LinuxCommandRequest req) {
        LinuxCommand saved = repo.save(req.toEntity());
        return LinuxCommandResponse.of(saved);
    }

    /* ────────────────────────────── 리눅스 커맨드 분석 API ───────────────────────────── */

    public LinuxCommandResponse generate(LinuxCommandGenerateRequest request) {
        return null; // TODO 명령어 분석 하는 로직 작성
    }

    /* ────────────────────────────── 자동완성 API ───────────────────────────── */

    public LinuxAutoCompleteResponse autocompleteCommand(LinuxAutoCompleteRequest req) {

        ParseCtx ctx = parse(req.line(), req.cursor());

        List<Suggestion> sugg = switch (ctx.phase) {
            case COMMAND  -> suggestCommand(ctx);
            case OPTION   -> suggestOption(ctx);
            case ARGUMENT -> suggestArgument(ctx);
        };

        return new LinuxAutoCompleteResponse(ctx.phase, ctx.baseCommand, ctx.currentToken, sugg);
    }

    /* ─────────────── Phase 판단 ─────────────── */

    private record ParseCtx(AcPhase phase, String baseCommand,
                            String currentToken, String prevFlag) {}

    private ParseCtx parse(String line, int cursor) {
        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(line);
        if (tokens.isEmpty())
            return new ParseCtx(AcPhase.COMMAND, "", "", null);

        ShellTokenizer.Token curTok = tokens.get(tokens.size() - 1);
        for (ShellTokenizer.Token t : tokens)
            if (cursor <= t.end()) { curTok = t; break; }

        String cur = curTok.text();
        if (tokens.size() == 1 && cursor <= curTok.end())
            return new ParseCtx(AcPhase.COMMAND, "", cur, null);

        String baseCmd   = tokens.get(0).text();
        String prevToken = tokens.size() >= 2 ? tokens.get(tokens.size() - 2).text() : "";

        if (cur.startsWith("-"))
            return new ParseCtx(AcPhase.OPTION, baseCmd, cur, null);

        if (prevToken.startsWith("-") && optionRequiresArg(baseCmd, prevToken))
            return new ParseCtx(AcPhase.ARGUMENT, baseCmd, cur, prevToken);

        if (cursor == line.length() && line.endsWith(" "))
            return new ParseCtx(AcPhase.OPTION, baseCmd, "", null);

        return new ParseCtx(AcPhase.OPTION, baseCmd, cur, null);
    }

    private boolean optionRequiresArg(String cmd, String flag) {
        return repo.findByCommand(cmd)
                .map(c -> Optional.ofNullable(c.getOptions().get(flag)))
                .flatMap(o -> o.map(info -> info.argRequired()))
                .orElse(false);
    }

    /* ─────────────── Suggestion 영역 ─────────────── */

    /** 명령어 자동완성 */
    private List<Suggestion> suggestCommand(ParseCtx ctx) {
        String key     = RedisKeys.acCmd();
        String prefix  = ctx.currentToken;

        List<String> res = fetchFromRedisByPrefix(key, prefix);
        if (res.isEmpty()) {
            res = repo.findTop15ByCommandStartingWith(prefix)
                    .stream().map(LinuxCommand::getCommand).toList();
            cacheStringsAsZSet(key, res);
        }
        redis.opsForZSet().incrementScore(RedisKeys.hotCmd(), prefix, 1);

        return res.stream()
                .map(cmd -> new Suggestion(cmd, cmd, ""))
                .toList();
    }

    /** 옵션 자동완성 */
    private List<Suggestion> suggestOption(ParseCtx ctx) {

        String key    = RedisKeys.acOpt(ctx.baseCommand);
        String prefix = ctx.currentToken;

        List<String> res = fetchFromRedisByPrefix(key, prefix);
        if (res.isEmpty()) {
            repo.findByCommand(ctx.baseCommand).ifPresent(cmd -> {
                cacheStringsAsZSet(key, cmd.getOptions().keySet());
            });
            res = fetchFromRedisByPrefix(key, prefix);
        }

        redis.opsForZSet().incrementScore(RedisKeys.hotOpt(ctx.baseCommand), prefix, 1);

        return res.stream()
                .map(flag -> new Suggestion(
                        flag,
                        flag + " " + placeholder(ctx.baseCommand, flag),
                        cmdOptionDesc(ctx.baseCommand, flag)))
                .toList();
    }



    /** 인수 자동완성(현재는 placeholder 하나) */
    private List<Suggestion> suggestArgument(ParseCtx ctx) {
        String ph = placeholder(ctx.baseCommand, ctx.prevFlag);
        return List.of(new Suggestion(ph, ph, "placeholder"));
    }

    /* ─────────────── 공통 헬퍼 ─────────────── */

    private List<String> fetchFromRedisByPrefix(String key, String prefix) {
//        Range range = Range.range().gte(prefix).lte(prefix + "\uFFFF");
//        Set<String> set = redis.opsForZSet()
//                .rangeByLex(key, range, Limit.limit().count(15));

        Range<String> range = Range.closed(prefix, prefix + "\uFFFF");
        Limit limit = Limit.limit().count(15);
        Set<String> strings = redis.opsForZSet()
                .rangeByLex(key, range, limit);

        return strings == null ? List.of() : new ArrayList<>(strings);
    }

    private void cacheStringsAsZSet(String key, Collection<String> values) {
        if (values.isEmpty()) return;

        Set<TypedTuple<String>> tuples = values.stream()
                .map(v -> new DefaultTypedTuple<String>(v, 0.0))
                .collect(Collectors.toSet());

        redis.opsForZSet().add(key, tuples);
    }

    private String placeholder(String cmd, String flag) {
        return repo.findByCommand(cmd)
                .map(c -> c.getOptions().getOrDefault(flag, null))
                .map(o -> "<" + o.argName() + ">")
                .orElse("<arg>");
    }

    private String cmdOptionDesc(String cmd, String flag) {
        return repo.findByCommand(cmd)
                .map(c -> c.getOptions().get(flag))
                .map(LinuxCommand.OptionInfo::description)
                .orElse("");
    }
}
