package com.dockerinit.features.support.validation;

import com.dockerinit.global.exception.InvalidInputCustomException;

import java.util.Locale;
import java.util.regex.Pattern;

public final class Slug {
    private final String value;

    private static final Pattern p = Pattern.compile("^[a-z0-9]([a-z0-9-]*[a-z0-9])?$");
    private static final Pattern WS_OR_UNDERS = Pattern.compile("[\\s_]+");
    private static final Pattern MULTI_DASH = Pattern.compile("-{2,}");

    public Slug(String value) {
        this.value = value;
    }

    public static Slug of(String raw) {
        if (raw == null) throw new InvalidInputCustomException("slug가 비어있습니다.");
        String s = normalize(raw);
        if (!s.matches(p.pattern())) {
            throw new InvalidInputCustomException("invalid slug: " + s);
        }
        return new Slug(s);
    }

    public static String normalize(String raw) {
        String s = raw.trim().toLowerCase(Locale.ROOT);
        s = WS_OR_UNDERS.matcher(s).replaceAll("-");
        s = MULTI_DASH.matcher(s).replaceAll("-");
        s = s.replaceAll("(^-+)|(-+$)", "");
        return s;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
