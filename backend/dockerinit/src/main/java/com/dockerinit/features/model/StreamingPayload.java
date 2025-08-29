package com.dockerinit.features.model;

import java.io.InputStream;
import java.util.function.Supplier;

public record StreamingPayload(
        Supplier<InputStream> supplier,
        long contentLength
) implements PackagePayload {
}
