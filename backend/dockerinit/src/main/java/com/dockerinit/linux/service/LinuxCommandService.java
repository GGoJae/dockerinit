package com.dockerinit.linux.service;

import com.dockerinit.linux.domain.LinuxCommand;
import com.dockerinit.global.exception.NotFoundCustomException;
import com.dockerinit.linux.dto.*;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.service.strategy.ParseStrategy;
import com.dockerinit.linux.service.strategy.strategyImpl.DefaultOptionStrategy;
import com.dockerinit.linux.model.ParseCtx;
import com.dockerinit.linux.util.RedisKeys;
import com.dockerinit.linux.util.ShellTokenizer;
import com.dockerinit.linux.model.Suggestion;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.dockerinit.global.constants.ErrorMessage.LINUX_COMMAND_ID_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LinuxCommandService {

    private final StringRedisTemplate redis;
    private final LinuxCommandRepository repo;
    private final List<ParseStrategy> strategies;


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

        List<Suggestion> suggest = switch (ctx.phase()) {
            case COMMAND -> suggestCommand(ctx);
            case OPTION -> suggestOption(ctx);
            case ARGUMENT -> suggestArgument(ctx);
        };

        return new LinuxAutoCompleteResponse(ctx.phase(), ctx.baseCommand(), ctx.currentToken(), suggest);
    }

    /* ─────────────── Phase 판단 ─────────────── */
    /**
     * 전략에 일치하지 않으면 항상 OPTION 단계로 fallback 처리
     */

    private ParseCtx parse(String line, int cursor) {
        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(line);

        return strategies.stream()
                .filter(s -> s.matches(line, cursor, tokens))
                .findFirst().orElseGet(DefaultOptionStrategy::new)
                .apply(line, cursor, tokens);
    }

    /* ─────────────── Suggestion 영역 ─────────────── */

    /**
     * 명령어 자동완성
     */
    private List<Suggestion> suggestCommand(ParseCtx ctx) {
        String key = RedisKeys.autoCompleteCommand();
        String prefix = ctx.currentToken();

        List<String> res = fetchFromRedisByPrefix(key, prefix);
        if (res.isEmpty()) {
            res = repo.findTop15ByCommandStartingWith(prefix)
                    .stream().map(LinuxCommand::getCommand).toList();
            cacheStringsAsZSet(key, res);
        }
        redis.opsForZSet().incrementScore(RedisKeys.hotCommand(), prefix, 1);

        return res.stream()
                .map(cmd -> new Suggestion(cmd, cmd, ""))
                .toList();
    }

    /**
     * 옵션 자동완성
     */
    private List<Suggestion> suggestOption(ParseCtx ctx) {

        String key = RedisKeys.authCompleteOption(ctx.baseCommand());
        String prefix = ctx.currentToken();

        List<String> res = fetchFromRedisByPrefix(key, prefix);
        if (res.isEmpty()) {
            repo.findByCommand(ctx.baseCommand()).ifPresent(cmd -> {
                cacheStringsAsZSet(key, cmd.getOptions().keySet());
            });
            res = fetchFromRedisByPrefix(key, prefix);
        }

        redis.opsForZSet().incrementScore(RedisKeys.hotOption(ctx.baseCommand()), prefix, 1);

        return res.stream()
                .map(flag -> new Suggestion(
                        flag,
                        flag + " " + placeholder(ctx.baseCommand(), flag),
                        cmdOptionDesc(ctx.baseCommand(), flag)))
                .toList();
    }


    /**
     * 인수 자동완성(현재는 placeholder 하나)
     */
    private List<Suggestion> suggestArgument(ParseCtx ctx) {
        String ph = placeholder(ctx.baseCommand(), ctx.prevFlag());
        return List.of(new Suggestion(ph, ph, "placeholder"));
    }

    /* ─────────────── 공통 헬퍼 ─────────────── */

    private List<String> fetchFromRedisByPrefix(String key, String prefix) {

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
                .orElseGet(() -> "<arg>");
    }

    private String cmdOptionDesc(String cmd, String flag) {
        return repo.findByCommand(cmd)
                .map(c -> c.getOptions().get(flag))
                .map(LinuxCommand.OptionInfo::description)
                .orElseGet(() -> "");
    }
}
