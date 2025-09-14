package com.dockerinit.global.validation;

import com.dockerinit.global.exception.InvalidInputCustomException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Slug {

    private static final Pattern p = Pattern.compile("^[a-z0-9]([a-z0-9-]*[a-z0-9])?$");
    private static final Pattern WS_OR_UNDERS = Pattern.compile("[\\s_]+");
    private static final Pattern MULTI_DASH = Pattern.compile("-{2,}");

    public static String normalize(String raw) {

        ValidationErrors.throwNowIf(raw == null || raw.isBlank(),
                "slug", "slug가 비어있습니다.", raw);


        String s = raw.trim().toLowerCase(Locale.ROOT);
        s = WS_OR_UNDERS.matcher(s).replaceAll("-");
        s = MULTI_DASH.matcher(s).replaceAll("-");
        s = s.replaceAll("(^-+)|(-+$)", "");

        ValidationErrors.create()
                .required("slug", s, "slug가 비어있습니다.")
                .matches("slug", s, p, "올바른 형식이 아닙니다.")
                .lengthBetween("slug", s, 1, 64, "slug 길이는 1~64자여야 합니다.") // TODO 제약 생각해보기
                .judge();

        return s;
    }

}
