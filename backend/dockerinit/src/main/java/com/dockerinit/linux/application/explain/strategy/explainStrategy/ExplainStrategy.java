package com.dockerinit.linux.application.explain.strategy.explainStrategy;

import com.dockerinit.linux.application.shared.parser.CommandLineParser;
import com.dockerinit.linux.application.explain.explainer.CommandExplainer;
import com.dockerinit.linux.application.shared.model.ModuleType;

public interface ExplainStrategy extends CommandExplainer, CommandLineParser {
    ModuleType type();
}
