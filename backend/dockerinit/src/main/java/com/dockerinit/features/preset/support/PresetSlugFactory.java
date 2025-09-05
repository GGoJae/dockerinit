package com.dockerinit.features.preset.support;

import com.dockerinit.features.preset.dto.spec.PresetKindDTO;
import com.dockerinit.features.support.validation.Slug;
import com.dockerinit.global.exception.InternalErrorCustomException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PresetSlugFactory {

    private static final int MAX_LEN = 80;

    public static String build(PresetKindDTO kind, List<String> baseTokens, Integer schemaVersion) {

        List<String> tokens = baseTokens.stream()
                .filter(Objects::nonNull)
                .map(Slug::normalize)
                .filter(s -> !s.isBlank())
                .toList();

        String prefix = kind.code() + "--";
        String version = (schemaVersion != null) ? ("-v" + schemaVersion) : "";
        String base = String.join("-", tokens);

        int budgetForBase = MAX_LEN - prefix.length() - version.length();
        if (budgetForBase <= 0) budgetForBase = 16;

        if (base.length() <= budgetForBase) return prefix + base + version;

        String hash = hash10(base);
        int trunkLen = Math.max(1, budgetForBase - (1 + hash.length()));
        String trunk = base.substring(0, trunkLen);

        return prefix + trunk + "-" + hash + version;
    }

    private static String hash10(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (int i = 0; i < d.length && sb.length() < 10; i++) {
                sb.append(Character.forDigit((d[i] >>> 4) & 0xF, 16));
                sb.append(Character.forDigit(d[i] & 0xF, 16));
            }
            return sb.substring(0, 10);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalErrorCustomException("Hash 만드는 도중 에러", e);
        }
    }

    public static String withNumericSuffix(String baseSlug, int n) {
        String suffix = "-" + n;
        return baseSlug + suffix;
    }

}
