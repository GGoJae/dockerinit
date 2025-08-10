package com.dockerinit.linux.domain.syntax;

import com.dockerinit.global.exception.InvalidInputCustomException;

import java.util.List;
import java.util.Map;

public record Synopsis(List<SynopsisPattern> patterns) {
    public Synopsis {
        patterns = List.copyOf(patterns);
        if (patterns.isEmpty()) {
            throw new InvalidInputCustomException("synopsis pattern이 비어있습니다.", Map.of("patterns", patterns));
        }
    }

    public List<TokenType> expectedTypeAt(int position) {
        return patterns.stream()
                .filter(p -> position < p.size())
                .map(p -> p.expectedTypeAt(position))
                .distinct().toList();
    }

}
