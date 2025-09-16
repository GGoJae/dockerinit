package com.dockerinit.linux.usage.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptionUsageKeys {

    public static String zRank(String cmd) {
        return "linux:opt:rank:" + cmd;
    }

    public static String hDelta(String cmd) {
        return "linux:opt:delta:" + cmd;
    }

    public static String dirtySet() {
        return "linux:opt:dirty";
    }
}
