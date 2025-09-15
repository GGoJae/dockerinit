package com.dockerinit.linux.application.explain.strategy.explainStrategy.impl;

import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.application.shared.parser.CommonLinuxCommandParser;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
import com.dockerinit.linux.application.explain.explainer.impl.CommonLinuxCommandExplainer;
import com.dockerinit.linux.application.explain.strategy.explainStrategy.ExplainStrategy;
import com.dockerinit.linux.application.shared.model.ModuleType;
import com.dockerinit.linux.dto.response.explainV1.ExplainResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CommonLinuxExplainStrategy implements ExplainStrategy {

    private final CommonLinuxCommandExplainer explainer;
    private final CommonLinuxCommandParser parser;

    @Override
    public ModuleType type() {
        return ModuleType.LINUX;
    }

    @Override
    public ParseResult parse(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        return parser.parse(line, cursor, tokens);
    }

    @Override
    public ExplainResponse explain(ParseResult result, Locale locale) {
        return explainer.explain(result, locale);
    }
}
