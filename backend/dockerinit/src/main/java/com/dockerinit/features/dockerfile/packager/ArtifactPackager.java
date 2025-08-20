package com.dockerinit.features.dockerfile.packager;

import com.dockerinit.features.dockerfile.model.GeneratedFile;

import java.util.List;

public interface ArtifactPackager {
    byte[] toZip(String dockerfile, List<GeneratedFile> extras);
}
