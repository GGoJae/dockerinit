package com.dockerinit.features.dockerfile.model;

import com.dockerinit.global.exception.InternalErrorCustomException;

import java.util.Objects;
import java.util.regex.Pattern;

public record ContentType(String value) {
    private static final Pattern P = Pattern.compile("^[\\w!#$&^_.+-]+/[\\w!#$&^_.+-]+(?:;.*)?$");
    public ContentType {
        Objects.requireNonNull(value);
        if (!P.matcher(value).matches()) throw new InternalErrorCustomException("Invalid content-type: " + value);
    }

    public static final ContentType ZIP = new ContentType("application/zip");
    public static final ContentType OCTET = new ContentType("applcation/octet-stream");
    public static final ContentType TEXT = new ContentType("text/plain; charset=utf-8");
    public static final ContentType MD = new ContentType("text/markdown; charset=utf-8");
    public static final ContentType JSON = new ContentType("application/json");

}
