package com.dockerinit.linux.application.explain.strategy.explainStrategy.impl;

import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
import com.dockerinit.linux.application.explain.strategy.explainStrategy.ExplainStrategy;
import com.dockerinit.linux.application.shared.model.ModuleType;
import com.dockerinit.linux.dto.response.explainV1.ExplainResponse;
import com.dockerinit.linux.dto.response.explainV1.Details;
import com.dockerinit.linux.dto.response.explainV1.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

import static com.dockerinit.global.constants.AppInfo.CURRENT_EXPLAIN_VERSION;

@Component
public class UnknownExplainStrategy implements ExplainStrategy {
    @Override
    public ModuleType type() {
        return ModuleType.UNKNOWN;
    }

    @Override
    public ParseResult parse(String line, int cursor, List<ShellTokenizer.Token> tokens) {
        return new ParseResult(line, cursor, "", "", -1, "", null, List.of(), 0);
    }

    @Override
    public ExplainResponse explain(ParseResult result, Locale locale) {
        return new ExplainResponse(
                CURRENT_EXPLAIN_VERSION,
                new Header(result.baseCommand(), "알 수 없는 명령어입니다.", List.of()),
                new Details(List.of(), List.of(), List.of()),
                List.of(),
                false
        );
    }

}
