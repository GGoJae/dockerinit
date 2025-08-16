package com.dockerinit.linux.application.shared.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModuleTypeMapper {

    public static ModuleType fromCommand(String baseCommand) {
        String lowerCase = baseCommand.toLowerCase();
        if (getLinuxCommands().contains(lowerCase)) {
            lowerCase = "linux";
        }
        return switch (lowerCase) {
            // TODO Linux 커맨드와 unknown 커맨드 분기   fallback 전략으로 unknown 커맨드 넣기
            case "linux" -> ModuleType.LINUX;
            case "docker" -> ModuleType.DOCKER;
            case "git" -> ModuleType.GIT;
            default -> ModuleType.UNKNOWN;
        };
    }

    private static List<String> getLinuxCommands() {
        return List.of("ls", "mv", "tar", "mkdir", "ping", "grep");
    }
}
