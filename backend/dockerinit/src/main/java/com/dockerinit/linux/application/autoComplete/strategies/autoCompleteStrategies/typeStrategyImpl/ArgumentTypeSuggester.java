package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.typeStrategyImpl;

import com.dockerinit.linux.application.autoComplete.model.CommandView;
import com.dockerinit.linux.application.autoComplete.model.ExpectedToken;
import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.TypeSuggester;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.dto.response.SuggestionType;
import com.dockerinit.linux.dto.response.v2.SuggestionV2;
import com.dockerinit.linux.util.Replace;
import com.dockerinit.linux.util.ShellTokenizer;
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
    public List<SuggestionV2> collect(ParseResult ctx, List<ShellTokenizer.Token> tokens, ExpectedToken slot, Replace.Range range, int limit) {
        String ph = placeholder(ctx.command(), ctx.prevFlag());
        return List.of(new SuggestionV2(ph, ph, "인자 placeholder", SuggestionType.ARGUMENT, 0.8, range.start(), range.end()));
    }

    private String placeholder(CommandView cmd, String flag) {
        Option option = (cmd != null) ? cmd.options().get(flag) : null;
        return (option == null) ? PLACE_HOLDER : "<" + option.argName() + ">";
    }
}
