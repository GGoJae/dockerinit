package com.dockerinit.features.model;

public record ByteArrayPayload(
        byte[] bytes
) implements PackagePayload {
}
