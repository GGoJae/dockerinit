package com.dockerinit.features.dockerfile.model;

public sealed interface PackagePayload permits ByteArrayPayload, StreamingPayload {
}
