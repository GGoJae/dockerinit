package com.dockerinit.global.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppInfo {
    public static final String PROJECT_NAME = "dockerinit";
    public static final String CURRENT_APP_VERSION = "v1";
    public static final String CURRENT_AUTOCOMPLETE_VERSION = "autocomplete.v1";
    public static final String CURRENT_EXPLAIN_VERSION = "explain.v1";
}
