package com.dockerinit.service;

import com.dockerinit.domain.LinuxCommand;
import com.dockerinit.dto.linuxCommand.LinuxCommandGenerateRequest;
import com.dockerinit.dto.linuxCommand.LinuxCommandRequest;
import com.dockerinit.dto.linuxCommand.LinuxCommandResponse;
import com.dockerinit.exception.CustomException.NotFoundCustomException;
import com.dockerinit.repository.LinuxCommandRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dockerinit.constant.ErrorMessage.LINUX_COMMAND_ID_NOT_FOUND;
import static com.dockerinit.constant.ErrorMessage.LINUX_COMMAND_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LinuxCommandService {

    private final LinuxCommandRepository repository;

    public LinuxCommandResponse getById(String id) {
        return repository.findById(id)
                .map(LinuxCommandResponse::of)
                .orElseThrow(() -> new NotFoundCustomException(LINUX_COMMAND_ID_NOT_FOUND, id));
    }

    public List<LinuxCommandResponse> getAll() {
        return repository.findAll().stream()
                .map(LinuxCommandResponse::of)
                .toList();
    }

    public LinuxCommandResponse getByCommand(String command) {
        return repository.findByCommand(command)
                .map(LinuxCommandResponse::of)
                .orElseThrow(() -> new NotFoundCustomException(LINUX_COMMAND_NOT_FOUND, command));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }




    public LinuxCommandResponse generate(LinuxCommandGenerateRequest request) {
        return null;
    }


    public List<String> autocompleteCommand(String string) {
        return null;
    }

    public List<String> autocompleteOptions(String command, String string) {
       return null;
    }

    private static final List<String> popularCommands = List.of(
            "ls", "cd", "cat", "chmod", "chown", "find", "grep", "tar", "ping"
    );

    public LinuxCommandResponse createCommand(@Valid LinuxCommandRequest request) {
        LinuxCommand savedCommand = repository.save(request.toEntity());

        return LinuxCommandResponse.of(savedCommand);
    }
}
