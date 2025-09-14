package com.dockerinit.global.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Slug {

    private static final Pattern P = Pattern.compile("^[a-z0-9]([a-z0-9-]*[a-z0-9])?$");
    private static final Pattern WS_OR_UNDERS = Pattern.compile("[\\s_]+");
    private static final Pattern MULTI_DASH = Pattern.compile("-{2,}");
    private static final Pattern EDGE_DASH = Pattern.compile("(^-+)|(-+$)");


    public static Optional<String> normalizeToken(String raw) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        String s = core(raw);
        if (s.isBlank() || !P.matcher(s).matches()) return Optional.empty();

        return Optional.of(s);
    }

    public static String normalizeRequired(String raw) {
        ValidationCollector.throwNowIf(raw == null || raw.isBlank(),
                "slug", "slug가 비어있습니다.", raw);

        String s = core(raw);
        ValidationCollector.create()
                .required("slug", s, "slug가 비었습니다.")
                .matches("slug", s, P, "올바른 형식이 아닙니다.")
                .throwIfInvalid();

        return s;
    }

    private static String core(String raw) {
        String s = raw.trim().toLowerCase(Locale.ROOT);
        s = WS_OR_UNDERS.matcher(s).replaceAll("-");
        s = MULTI_DASH.matcher(s).replaceAll("-");
        s = EDGE_DASH.matcher(s).replaceAll("");
        return s;
    }

}
