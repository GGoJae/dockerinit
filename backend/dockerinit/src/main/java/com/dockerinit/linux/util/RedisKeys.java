package com.dockerinit.linux.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisKeys {

    public static String autoCompleteCommand() {
        return "ac:cmd";
    }

    public static String authCompleteOption(String cmd) {
        return "ac:opt:" + cmd;
    }

    public static String autoCompleteArgument(String cmd, String flag) {
        return "ac:arg:" + cmd + ":" + flag;
    }

    public static String hotCommand() {
        return "hot:cmd";
    }

    public static String hotOption(String cmd) {
        return "hot:opt:" + cmd;
    }
}
