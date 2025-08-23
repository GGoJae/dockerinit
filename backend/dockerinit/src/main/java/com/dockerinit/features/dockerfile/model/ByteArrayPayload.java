package com.dockerinit.features.dockerfile.model;

public record ByteArrayPayload(
        byte[] bytes
) implements PackagePayload{
}
