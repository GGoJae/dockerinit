package com.dockerinit.linux.application.service;

import com.dockerinit.global.exception.NotFoundCustomException;
import com.dockerinit.linux.application.autocomplete.strategy.autocompleteStrategies.AutocompleteCommandStrategy;
import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.dto.request.LinuxCommandGenerateRequest;
import com.dockerinit.linux.dto.request.LinuxCommandRequest;
import com.dockerinit.linux.dto.response.LinuxCommandResponse;
import com.dockerinit.linux.infrastructure.repository.LinuxCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dockerinit.global.constants.ErrorMessage.LINUX_COMMAND_ID_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LinuxCommandService {

    private final LinuxCommandRepository repository;
    private final List<AutocompleteCommandStrategy> strategies;


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
