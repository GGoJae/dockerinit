package com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies;

import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.dto.response.v2.SuggestionV2;
import com.dockerinit.linux.dto.vo.ParseCtx;

import java.util.List;

public interface AutoCompleteSuggester {
    List<SuggestionV2> suggest(ParseResult result);
}
