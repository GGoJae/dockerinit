package com.dockerinit.linux.application.autocomplete.replace;

import com.dockerinit.linux.application.autocomplete.tokenizer.ShellTokenizer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Replace {

    public record Range(int start, int end) {
    }

    public static Range forCurrentToken(int cursor, List<ShellTokenizer.Token> tokens) {
        int idx = ShellTokenizer.indexOfTokenAtCursor(cursor, tokens);
        if (idx < 0) return new Range(cursor, cursor);
        ShellTokenizer.Token token = tokens.get(idx);
        return new Range(token.begin(), token.end());
    }

    private static Range forValueAfterEquals(int cursor, List<ShellTokenizer.Token> tokens, String line) {
        int idx = ShellTokenizer.indexOfTokenAtCursor(cursor, tokens);
        if (idx < 0) return new Range(cursor, cursor);
        ShellTokenizer.Token token = tokens.get(idx);
        int eq = indexOfUnescapedEquals(line, token.begin(), token.end());
        if (eq >= 0 && cursor > eq) {
            return new Range(Math.min(eq + 1, token.end()), token.end());
        }
        return new Range(token.begin(), token.end());
    }

    private static int indexOfUnescapedEquals(String s, int from, int to) {
        boolean esc = false, inS = false, inD = false;
        for (int i = from; i < to; i++) {
            char c = s.charAt(i);
            if (esc) { esc = false; continue; }
            if (c == '\\') { esc = true; continue; }
            if (c == '\'' && !inD) { inS = !inS; continue; }
            if (c == '"'  && !inS) { inD = !inD; continue; }
            if (!inS && !inD && c == '=') return i;
        }
        return -1;
    }
}
