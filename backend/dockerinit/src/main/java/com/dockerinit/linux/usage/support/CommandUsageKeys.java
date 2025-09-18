package com.dockerinit.linux.usage.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandUsageKeys {

    public static String zRank() {
        return "linux:cmd:rank";
    }
    public static String hDelta() {
        return "linux:cmd:delta";
    }
    public static String dirtyKey() {
        return "linux:cmd:dirty";
    }

}
