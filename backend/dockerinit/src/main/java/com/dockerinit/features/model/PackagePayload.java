package com.dockerinit.features.model;

public sealed interface PackagePayload permits ByteArrayPayload, StreamingPayload {
}
