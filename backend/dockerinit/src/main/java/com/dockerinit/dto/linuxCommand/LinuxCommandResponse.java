package com.dockerinit.dto.linuxCommand;

import java.util.List;

public record LinuxCommandResponse(
        String command,
        String commandDescription,
        List<Explanation> explanation) {

        public record Explanation(String option, String description) {
    }
}
