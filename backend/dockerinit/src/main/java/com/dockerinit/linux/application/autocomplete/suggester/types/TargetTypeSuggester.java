package com.dockerinit.linux.application.autocomplete.suggester.types;

import com.dockerinit.linux.application.autocomplete.model.ExpectedToken;
import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.dto.response.common.SuggestionType;
import com.dockerinit.linux.dto.response.autocompleteV1.Suggestion;
import com.dockerinit.linux.application.autocomplete.replace.Replace;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
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
