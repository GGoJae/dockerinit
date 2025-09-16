package com.dockerinit.linux.application.service;

import com.dockerinit.global.exception.NotFoundCustomException;
import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.dto.request.CreateLinuxCommandRequest;
import com.dockerinit.linux.dto.response.doc.LinuxCommandDocResponse;
import com.dockerinit.linux.infrastructure.repository.LinuxCommandRepository;
import com.dockerinit.linux.mapper.LinuxCommandDocMapper;
import com.dockerinit.linux.mapper.LinuxCommandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.dockerinit.global.constants.ErrorMessage.LINUX_COMMAND_ID_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LinuxCommandService {

    private final LinuxCommandRepository repository;

    /* ─────────────────────────────── CRUD API ─────────────────────────────── */

    public LinuxCommandDocResponse getById(String id) {
        return repository.findById(id)
                .map(LinuxCommandDocMapper::toDoc)
                .orElseThrow(() -> NotFoundCustomException.of(LINUX_COMMAND_ID_NOT_FOUND, "LinuxCommand", "id", id));
    }

    public Page<LinuxCommandDocResponse> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(LinuxCommandDocMapper::toDoc);
    }

    public LinuxCommandDocResponse createCommand(CreateLinuxCommandRequest req) {
        LinuxCommand saved = repository.save(LinuxCommandMapper.requestToDomain(req));
        return LinuxCommandDocMapper.toDoc(saved);
    }

}
