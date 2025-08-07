package com.dockerinit.linux.service.strategy.linuxCommandStrategy;

public interface LinuxCommandStrategy extends LinuxCommandParser, LinuxCommandSuggester {
    boolean supports(String command);
}
