package com.dockerinit.linux.application.service;

import com.dockerinit.global.exception.NotFoundCustomException;
import com.dockerinit.linux.application.autoComplete.model.CommandView;
import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.AutoCompleteCommandStrategy;
import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.dto.request.LinuxAutoCompleteRequest;
import com.dockerinit.linux.dto.request.LinuxCommandGenerateRequest;
import com.dockerinit.linux.dto.request.LinuxCommandRequest;
import com.dockerinit.linux.dto.response.LinuxCommandResponse;
import com.dockerinit.linux.dto.response.SuggestionType;
import com.dockerinit.linux.dto.response.v2.*;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.util.ShellTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dockerinit.global.constants.AutoCompleteSuggest.MAX_SUGGEST;
import static com.dockerinit.global.constants.ErrorMessage.LINUX_COMMAND_ID_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LinuxCommandService {

    private final LinuxCommandRepository repository;
    private final List<AutoCompleteCommandStrategy> strategies;


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

}
