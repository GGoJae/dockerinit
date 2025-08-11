package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies;

import com.dockerinit.linux.application.autoComplete.model.ExpectedToken;
import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.dto.response.SuggestionType;
import com.dockerinit.linux.dto.response.v2.SuggestionV2;
import com.dockerinit.linux.util.Replace;
import com.dockerinit.linux.util.ShellTokenizer;

import java.util.List;

public interface TypeSuggester {
    SuggestionType type();
    List<SuggestionV2> collect(
            ParseResult ctx,
            List<ShellTokenizer.Token> tokens,
            ExpectedToken slot,
            Replace.Range range,
            int limit
    );
}
