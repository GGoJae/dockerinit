package com.dockerinit.linux.application.explain.explainer;

import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.dto.response.explainV1.ExplainResponse;

import java.util.Locale;

public interface CommandExplainer {
    ExplainResponse explain(ParseResult result, Locale locale);
}
