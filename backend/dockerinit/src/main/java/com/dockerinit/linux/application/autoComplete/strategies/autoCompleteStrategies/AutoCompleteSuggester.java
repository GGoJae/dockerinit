package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies;

import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.dto.response.v2.SuggestionV2;
import com.dockerinit.linux.dto.vo.ParseCtx;
import com.dockerinit.linux.util.ShellTokenizer;

import java.util.List;

public interface AutoCompleteSuggester {
    List<SuggestionV2> suggest(ParseResult result, List<ShellTokenizer.Token> tokens);
}
