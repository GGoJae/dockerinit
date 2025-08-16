package com.dockerinit.linux.application.explain.util;

import com.dockerinit.linux.application.autocomplete.model.CommandView;
import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
import com.dockerinit.linux.application.explain.model.Invocation;
import com.dockerinit.linux.domain.syntax.Option;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InvocationFactory {

    public static Invocation from(ParseResult parseResult, List<ShellTokenizer.Token> tokens, Locale locale) {
        String cmd = parseResult.baseCommand().toLowerCase(Locale.ROOT);
        CommandView view = parseResult.command();

        Map<String, Option> meta = Objects.isNull(view) ? Map.of() : view.options();
        Map<String, String> opts = new LinkedHashMap<>();
        List<String> args = new ArrayList<>();

        for (int i = 1; i < tokens.size(); i++) {
            String t = tokens.get(i).text();
            if (t.equals("--")) {
                for (int j = i + 1; j < tokens.size(); j++) {
                    args.add(tokens.get(j).text());
                }
                break;
            }
            if (t.startsWith("--")) {
                int eq = t.indexOf("=");
                String flag = (eq >= 0) ? t.substring(0, eq) : t;
                String val = (eq >= 0) ? t.substring(eq + 1) : "";
                Option o = meta.get(flag);
                if (Objects.nonNull(o) && o.argRequired() && val.isBlank() && i + 1 < tokens.size()) {
                    val = tokens.get(++i).text();
                }
                opts.put(flag, val);
                continue;
            }

            if (t.startsWith("-") && t.length() > 1) {
                String letters = t.substring(1);
                boolean consumed = false;
                for (int p = 0; p < letters.length(); p++) {
                    String flag = "-" + letters.charAt(p);
                    Option o = meta.get(flag);
                    String val = "";
                    if (Objects.nonNull(o) && o.argRequired()) {
                        boolean hasRemainder = (p < letters.length() - 1);
                        if (hasRemainder) {
                            val = letters.substring(p + 1);
                            consumed = true;
                        } else if (i + 1 < tokens.size()) {
                            val = tokens.get(++i).text();
                        }
                        opts.put(flag, val);
                        break;
                    } else {
                        opts.put(flag, "");
                    }
                }
                if (consumed) {

                }
                continue;
            }
            args.add(t);
        }


        return new Invocation(cmd, opts, args, locale);
    }
}
