package com.dockerinit.linux.infrastructure.redis;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;
import java.util.Objects;

import static com.dockerinit.global.constants.AppInfo.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisKeys {

    private static final String APP = PROJECT_NAME;
    private static final String V = CURRENT_APP_VERSION;

     /*
     Autocomplete ZSet
      */
    public static String acCmdZSet(String module) {
        return APP + ":" + V + ":ac:" + module + ":cmd";
    }

    public static String acOptZSet(String module, String cmd) {
        return APP + ":" + V + ":ac:" + module + ":opt:" + norm(cmd);
    }

    public static String acArgZSet(String module, String cmd, String flag) {
        return APP + ":" + V + ":ac:" + module + ":arg:" + norm(cmd) + ":" + norm(flag);
    }

    /*
     Hot ranking ZSet
      */

    public static String hotCmdZSet(String module) {
        return APP + ":" + V + ":hot:" + module + ":cmd";
    }

    public static String hotOption(String module, String cmd) {
        return APP + ":" + V + ":hot:" + module + ":opt:" + norm(cmd);
    }

    /*
    entity 캐시
     */

    public static String cmdCache(String module, String cmd) {
        return APP + ":" + V + ":" + module + ":cmd:" + norm(cmd);
    }

    public static String cmdMiss(String module, String cmd) {
        return APP + ":" + V + ":" + module + ":miss:cmd:" + norm(cmd);
    }

    /*
    helpers
     */
    private static String norm(String s) {
        return (s == null) ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    public static String explainKey(String module, String cmd, Locale loc, String sigHash) {
        String locale = Objects.isNull(loc) ? "ko_KR" : loc.toString();
        return APP + ":" + V + ":explain:" + module + ":" + cmd.toLowerCase() + ":" + locale + ":" + sigHash;
    }
}
