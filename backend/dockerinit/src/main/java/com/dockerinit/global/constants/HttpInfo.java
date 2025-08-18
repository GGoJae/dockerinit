package com.dockerinit.global.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpInfo {

    public static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    public static final String NOSNIFF = "nosniff";
    public static final String APPLICATION_ZIP_VALUE = "application/zip";
    public static final String NO_CACHE = "no-cache";
}
