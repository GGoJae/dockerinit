package com.dockerinit.features.packager;

import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.PackageResult;

import java.util.List;

public interface Packager {
    PackageResult packageFiles(List<GeneratedFile> files, String packageName);
}
