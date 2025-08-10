package com.dockerinit.linux.dto.response;

import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.domain.syntax.Option;

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
        Map<String, Option> options,
        List<String> tags
) {
    public static LinuxCommandResponse of(LinuxCommand cmd) {
        return new LinuxCommandResponse(
                cmd.getId(),
                cmd.getCategory(),
                cmd.getCommand(),
                cmd.getDescription(),
                null,           // TODO 나중에 synopsis DTO 로 바꿀것
                cmd.getArguments(),
                cmd.getExamples(),
                cmd.isVerified(),
                cmd.isOptionRequired(),
                cmd.getOptions(),
                cmd.getTags()
        );
    }
}