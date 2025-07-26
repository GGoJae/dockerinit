package com.dockerinit.dto.linuxCommand;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LinuxCommandRequest {
    private String command;
    private List<String> args;
    private String target;
}
