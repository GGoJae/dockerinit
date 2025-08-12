package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.typeStrategyImpl;

import com.dockerinit.linux.application.autoComplete.model.ExpectedToken;
import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.TypeSuggester;
import com.dockerinit.linux.dto.response.SuggestionType;
import com.dockerinit.linux.dto.response.v2.Suggestion;
import com.dockerinit.linux.util.Replace;
import com.dockerinit.linux.util.ShellTokenizer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.dockerinit.global.constants.AutoCompleteSuggest.PATH_DESC;

@Component
public class TargetTypeSuggester implements TypeSuggester {
    @Override
    public SuggestionType type() {
        return SuggestionType.TARGET;
    }

    @Override
    public List<Suggestion> collect(ParseResult ctx, List<ShellTokenizer.Token> tokens, ExpectedToken slot, Replace.Range range, int limit) {
        String cur = ctx.currentToken();
        List<Suggestion> out = new ArrayList<Suggestion>();

        for (String v : List.of("./", "../")) {
            if (cur.isEmpty() || v.startsWith(cur) || cur.startsWith(v)) {
                out.add(new Suggestion(v, v, PATH_DESC, SuggestionType.TARGET, 0.7, range.start(), range.end()));
            }
            if (out.size() >= 10) break;
        }
        return out;
    }
}
