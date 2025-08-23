package com.dockerinit.features.dockerfile.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.InputStream;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PackageResult {
    private final String filename;
    private final ContentType contentType;
    private final String eTag;
    private final boolean sensitive;       // 민감 파일 포함 시 캐시해더 제어
    private final PackagePayload payload;

    public String getFilename() {
        return filename;
    }

    public String getETag() {
        return eTag;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public static PackageResult ofByteArray(String filename, ContentType contentType, byte[] content,
                                            String eTag, boolean sensitive) {
        return new PackageResult(filename, contentType, eTag, sensitive, new ByteArrayPayload(content));
    }

    public static PackageResult ofStreaming(String filename, ContentType contentType, Supplier<InputStream> supplier,
                                            long contentLength, String eTag, boolean sensitive) {
        return new PackageResult(filename, contentType, eTag, sensitive, new StreamingPayload(supplier, contentLength));
    }

    public long contentLength() {
        if (payload instanceof ByteArrayPayload b) return b.bytes().length;

        if (payload instanceof StreamingPayload s) return s.contentLength();

        return -1;
    }

    public <T> T fold(Function<byte[], T> onBytes, BiFunction<Supplier<InputStream>, Long, T> onStream) {
        if (payload instanceof ByteArrayPayload b) {
            return onBytes.apply(b.bytes());
        }
        StreamingPayload s = (StreamingPayload) payload;
        return onStream.apply(s.supplier(), s.contentLength());
    }

}
