package com.dockerinit.features.dockerfile.packager;

import com.dockerinit.features.dockerfile.model.GeneratedFile;
import com.dockerinit.features.dockerfile.model.PackageResult;

import java.util.List;

public interface Packager {
    PackageResult packageFiles(List<GeneratedFile> files, String packageName);
}
