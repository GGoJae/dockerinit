package com.dockerinit.linux.application.autoComplete.model;

import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.domain.syntax.Synopsis;

import java.util.List;
import java.util.Map;

public record CommandView(
        String command,
        String category,
        String description,
        Map<String, Option> options,
        Synopsis synopsis,
        List<String> examples,
        List<String> tags,
        boolean verified
) {

    public static CommandView of(LinuxCommand linuxCommand) {
        return new CommandView(
                linuxCommand.getCommand(),
                linuxCommand.getCategory(),
                linuxCommand.getDescription(),
                Map.copyOf(linuxCommand.getOptions()),
                linuxCommand.getSynopsis(),
                List.copyOf(linuxCommand.getExamples()),
                List.copyOf(linuxCommand.getTags()),
                linuxCommand.isVerified()
        );
    }

}
