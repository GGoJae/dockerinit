package com.dockerinit.linux.application.service;

import com.dockerinit.global.exception.NotFoundCustomException;
import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.dto.request.CreateLinuxCommandRequest;
import com.dockerinit.linux.dto.response.doc.LinuxCommandDocResponse;
import com.dockerinit.linux.infrastructure.repository.LinuxCommandRepository;
import com.dockerinit.linux.mapper.LinuxCommandDocMapper;
import com.dockerinit.linux.mapper.LinuxCommandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<LinuxCommandDocResponse> getAll() {
        return repository.findAll().stream().map(LinuxCommandDocMapper::toDoc).toList();
    }

    public LinuxCommandDocResponse createCommand(CreateLinuxCommandRequest req) {
        LinuxCommand saved = repository.save(LinuxCommandMapper.requestToDomain(req));
        return LinuxCommandDocMapper.toDoc(saved);
    }

}
