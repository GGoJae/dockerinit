package com.dockerinit.linux.application.autocomplete.suggester.types;

import com.dockerinit.linux.application.autocomplete.model.CommandView;
import com.dockerinit.linux.application.autocomplete.model.ExpectedToken;
import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.dto.response.common.SuggestionType;
import com.dockerinit.linux.dto.response.v1.Suggestion;
import com.dockerinit.linux.application.autocomplete.replace.Replace;
import com.dockerinit.linux.application.autocomplete.tokenizer.ShellTokenizer;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.dockerinit.global.constants.AutoCompleteSuggest.PLACE_HOLDER;

@Component
public class ArgumentTypeSuggester implements TypeSuggester {
    @Override
    public SuggestionType type() {
        return SuggestionType.ARGUMENT;
    }

    @Override
    public List<Suggestion> collect(ParseResult ctx, List<ShellTokenizer.Token> tokens, ExpectedToken slot, Replace.Range range, int limit) {
        String ph = placeholder(ctx.command(), ctx.prevFlag());
        return List.of(new Suggestion(ph, ph, "인자 placeholder", SuggestionType.ARGUMENT, 0.8, range.start(), range.end()));
    }

    private String placeholder(CommandView cmd, String flag) {
        Option option = (cmd != null) ? cmd.options().get(flag) : null;
        return (option == null) ? PLACE_HOLDER : "<" + option.argName() + ">";
    }
}
