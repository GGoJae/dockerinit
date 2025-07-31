package com.dockerinit.dto.linuxCommand;

import com.dockerinit.domain.LinuxCommand;

import java.util.List;
import java.util.Map;

public record LinuxCommandResponse(
        String id,
        String category,
        String command,
        String description,
        String usage,
        String example,
        boolean verified,
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
                cmd.getExample(),
                cmd.isVerified(),
                cmd.getOptions(),
                cmd.getTags()
        );
    }
}