package com.dockerinit.features.support;

import com.dockerinit.global.exception.InternalErrorCustomException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Hash {
    public static final String SHA_256 = "SHA-256";

    public static String sha256Hex(byte[] data){
        try {
            MessageDigest md = MessageDigest.getInstance(SHA_256);
            return HexFormat.of().formatHex(md.digest(data));
        } catch (Exception e) {
            log.warn("eTag 해시 계산 중 예외", e);
            throw new InternalErrorCustomException("eTag 해시 계산 중 예외", e);
        }
    }
}
