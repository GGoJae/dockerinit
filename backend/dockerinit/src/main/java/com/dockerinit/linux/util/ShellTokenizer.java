package com.dockerinit.linux.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShellTokenizer {

    public static List<Token> tokenize(String line) {
        List<Token> out = new ArrayList<>();
        int start = 0;
        boolean inQuote = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') inQuote = !inQuote;
            if (!inQuote && Character.isWhitespace(ch)) {
                if (start < i) out.add(new Token(line.substring(start, i), start, i));
                start = i + 1;
            }
        }

        if (start < line.length()) {
            out.add(new Token(line.substring(start), start, line.length()));
        } else if (line.endsWith(" ")) {
            out.add(new Token("", line.length(), line.length()));
        }

        return out;
    }

    public static Token currentToken(int cursor, List<Token> tokens) {
        return tokens.stream()
                .filter(t -> t.begin() <= cursor && cursor <= t.end())
                .findFirst()
                .orElseGet(() -> tokens.get(tokens.size() - 1));
    }

    public static int indexOfTokenAtCursor(int cursor, List<Token> tokens) {
        return tokens.stream()
                .filter(t -> t.begin() <= cursor && cursor <= t.end())
                .map(token -> tokens.indexOf(token))
                .findFirst().orElseGet(() -> -1);
    }

    public record Token(String text, int begin, int end) {}
}
