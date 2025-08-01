package com.dockerinit.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisKeys {

    public static String acCmd() {
        return "ac:cmd";
    }

    public static String acOpt(String cmd) {
        return "ac:opt:" + cmd;
    }

    public static String acArg(String cmd, String flag) {
        return "ac:arg:" + cmd + ":" + flag;
    }

    public static String hotCmd() {
        return "hot:cmd";
    }

    public static String hotOpt(String cmd) {
        return "hot:opt:" + cmd;
    }
}
