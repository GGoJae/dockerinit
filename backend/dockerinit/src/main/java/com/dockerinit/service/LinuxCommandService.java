package com.dockerinit.service;

import com.dockerinit.dto.linuxCommand.LinuxCommandRequest;
import com.dockerinit.dto.linuxCommand.LinuxCommandResponse;
import com.dockerinit.exception.CustomException.NotFoundCustomException;
import com.dockerinit.repository.LinuxCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dockerinit.constant.ErrorMessage.LINUX_COMMAND_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LinuxCommandService {

    private final LinuxCommandRepository repository;

    public LinuxCommandResponse getById(String id) {
        return repository.findById(id)
                .map(LinuxCommandResponse::of)
                .orElseThrow(() -> new NotFoundCustomException(LINUX_COMMAND_NOT_FOUND, id));
    }




    public LinuxCommandResponse generate(LinuxCommandRequest request) {
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

}
