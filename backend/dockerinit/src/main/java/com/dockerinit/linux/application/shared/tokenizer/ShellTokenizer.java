package com.dockerinit.linux.application.shared.tokenizer;

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

        // TODO 파이프 리다이렉션 세미콜론을 구분자로 취급 하는 로직 테스트 필요.....
//        boolean inS = false, inD = false, esc = false;
//        for (int i = 0; i < line.length(); i++) {
//            char ch = line.charAt(i);
//            if (esc) { esc = false; continue; }
//            if (ch == '\\') { esc = true; continue; }
//            if (ch == '\'' && !inD) { inS = !inS; continue; }
//            if (ch == '"' && !inS) { inD = !inD; continue; }
//
//            if (!inS && !inD && Character.isWhitespace(ch)) {
//                if (start < i) {
//                    out.add(new Token(line.substring(start, i), start, i));
//                    start = i + 1;
//                }
//            }
//        }

        if (start < line.length()) {
            out.add(new Token(line.substring(start), start, line.length()));
        } else if (line.endsWith(" ")) {
            out.add(new Token("", line.length(), line.length()));
        }

        return out;
    }

    public static Token currentToken(int cursor, List<Token> tokens) {
        if (tokens.isEmpty()) return new Token("", cursor, cursor);
        return tokens.stream()
                .filter(t -> t.begin() <= cursor && cursor < t.end())
                .findFirst()
                .orElseGet(() -> tokens.get(tokens.size() - 1));
    }

    public static int indexOfTokenAtCursor(int cursor, List<Token> tokens) {
        if (tokens.isEmpty()) return -1;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.begin() <= cursor && cursor < token.end()) {
                return i;
            }
        }
        return tokens.size() - 1;
    }

    public record Token(String text, int begin, int end) {}
}
