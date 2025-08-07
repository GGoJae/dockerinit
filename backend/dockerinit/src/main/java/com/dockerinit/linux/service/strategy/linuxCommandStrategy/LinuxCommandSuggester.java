package com.dockerinit.linux.service.strategy.linuxCommandStrategy;

import com.dockerinit.linux.model.ParseCtx;
import com.dockerinit.linux.model.Suggestion;

import java.util.List;

public interface LinuxCommandSuggester {
    List<Suggestion> suggest(ParseCtx ctx);
}
