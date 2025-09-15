package com.dockerinit.linux.domain.syntax;

import com.dockerinit.global.validation.ValidationCollector;

import java.util.Arrays;
import java.util.List;

public record Synopsis(List<SynopsisPattern> patterns) {
    public Synopsis {
        ValidationCollector.create()
                .required("patterns", patterns, "synopsis pattern이 비어있습니다.")
                .noNullElements("patterns", patterns, "patterns 안에 null 존재")
                .throwIfInvalid();

        patterns = List.copyOf(patterns);
    }

    public List<TokenType> expectedTypeAt(int position) {
        return patterns.stream()
                .filter(p -> position < p.size())
                .map(p -> p.expectedTypeAt(position).orElseThrow())
                .distinct()
                .toList();
    }

//    public List<String> expectedLiteralsAt(int position) {
//        if (position < 0) return List.of();
//        return patterns.stream()
//                .filter(p -> position < p.size())
//                .map(p -> p.at(position))
//                .filter(td -> td.tokenType() == TokenType.LITERAL)
//                .map(TokenDescriptor::literal)
//                .distinct()
//                .toList();
//    }

    public static Synopsis fromText(String text) {
        ValidationCollector.create()
                .notBlank("synopsis", text, "synopsis가 비어있습니다.")
                .throwIfInvalid();

        List<TokenDescriptor> tokens = Arrays.stream(text.trim().split("\\s+"))
                .map(TokenDescriptor::literal)
                .toList();

        return new Synopsis(List.of(new SynopsisPattern(tokens)));
    }

    public static Synopsis fromLines(List<String> lines) {
        ValidationCollector.create()
                .required("synopsisLines", lines, "synopsis 라인이 비어있습니다.")
                .noNullElements("synopsisLines", lines, "synopsis 라인에 null이 존재")
                .throwIfInvalid();

        String joined = String.join(" ", lines.stream().map(String::trim).filter(s -> !s.isEmpty()).toList());
        return fromText(joined);
    }
}
