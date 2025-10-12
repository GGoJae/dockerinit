package com.dockerinit.features.application.presetV2.shared.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ETagUtil {

    public static String strong(String... parts) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (String p : parts) {
                if (p == null) continue;
                md.update(p.getBytes(StandardCharsets.UTF_8));
                md.update((byte) 0);
            }
            byte[] hash = md.digest();
            String hex = HexFormat.of().formatHex(hash);
            return "\"" + hex + "\"";
        } catch (Exception e) {
            String joined = String.join("|", parts);
            return "\"" + joined + "\"";
        }
    }
}
