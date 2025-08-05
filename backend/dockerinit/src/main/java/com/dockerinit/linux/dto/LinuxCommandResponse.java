package com.dockerinit.linux.dto;

import com.dockerinit.linux.domain.LinuxCommand;

import java.util.List;
import java.util.Map;

public record LinuxCommandResponse(
        String id,
        String category,
        String command,
        String description,
        String usage,
        List<String> arguments,
        List<String> examples,
        boolean verified,
        boolean optionRequired,
        Map<String, LinuxCommand.OptionInfo> options,
        List<String> tags
) {
    public static LinuxCommandResponse of(LinuxCommand cmd) {
        return new LinuxCommandResponse(
                cmd.getId(),
                cmd.getCategory(),
                cmd.getCommand(),
                cmd.getDescription(),
                cmd.getUsage(),
                cmd.getArguments(),
                cmd.getExamples(),
                cmd.isVerified(),
                cmd.isOptionRequired(),
                cmd.getOptions(),
                cmd.getTags()
        );
    }
}