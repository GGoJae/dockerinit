package com.dockerinit.linux.service;

import com.dockerinit.linux.domain.LinuxCommand;
import com.dockerinit.global.exception.NotFoundCustomException;
import com.dockerinit.linux.dto.*;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.service.strategy.linuxCommandStrategy.LinuxCommandStrategy;
import com.dockerinit.linux.model.ParseCtx;
import com.dockerinit.linux.util.ShellTokenizer;
import com.dockerinit.linux.model.Suggestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.dockerinit.global.constants.ErrorMessage.LINUX_COMMAND_ID_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LinuxCommandService {

    private final LinuxCommandRepository repository;
    private final List<LinuxCommandStrategy> strategies;


    /* ─────────────────────────────── CRUD API ─────────────────────────────── */

    public LinuxCommandResponse getById(String id) {
        return repository.findById(id)
                .map(LinuxCommandResponse::of)
                .orElseThrow(() -> new NotFoundCustomException(LINUX_COMMAND_ID_NOT_FOUND, id));
    }

    public List<LinuxCommandResponse> getAll() {
        return repository.findAll().stream().map(LinuxCommandResponse::of).toList();
    }

    public LinuxCommandResponse createCommand(LinuxCommandRequest req) {
        LinuxCommand saved = repository.save(req.toEntity());
        return LinuxCommandResponse.of(saved);
    }

    /* ────────────────────────────── 리눅스 커맨드 분석 API ───────────────────────────── */

    public LinuxCommandResponse generate(LinuxCommandGenerateRequest request) {
        return null; // TODO 명령어 분석 하는 로직 작성
    }

    /* ────────────────────────────── 자동완성 API ───────────────────────────── */

    public LinuxAutoCompleteResponse autocompleteCommand(LinuxAutoCompleteRequest req) {
        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(req.line());
        String baseCmd = tokens.isEmpty() ? "" : tokens.get(0).text();

        LinuxCommandStrategy commandStrategy = strategies.stream()
                .filter(s -> s.supports(baseCmd))
                .findFirst()
                .orElseThrow();// TODO 전략 못 찾았을때는 추후 fallback 만들기 DefaultStrategy<--

        ParseCtx ctx = commandStrategy.parse(req.line(), req.cursor(), tokens);
        List<Suggestion> suggest = commandStrategy.suggest(ctx);

        return new LinuxAutoCompleteResponse(ctx.phase(), ctx.baseCommand(), ctx.currentToken(), suggest);
    }

}
