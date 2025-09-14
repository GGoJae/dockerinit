package com.dockerinit.linux.domain.syntax;

import com.dockerinit.global.validation.ValidationCollector;

import java.util.List;

public record Synopsis(List<SynopsisPattern> patterns) {
    public Synopsis {
        patterns = List.copyOf(patterns);
        ValidationCollector.create()
                .required("patterns", patterns, "synopsis pattern이 비어있습니다.")
                .throwIfInvalid();
    }

    public List<TokenType> expectedTypeAt(int position) {
        return patterns.stream()
                .filter(p -> position < p.size())
                .map(p -> p.expectedTypeAt(position))
                .distinct().toList();
    }

}
