package com.dockerinit.global.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AutoCompleteSuggest {
    public static final int MAX_SUGGEST = 15;
    public static final String PLACE_HOLDER = "<arg>";
    public static final String PATH_DESC = "path";
    public static final int REDIS_PREFIX_LENGTH_LIMIT = 5;
}
