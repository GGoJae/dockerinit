package com.dockerinit.features.dockercompose.dto.request;

import com.dockerinit.features.dockercompose.dto.spec.NetworkDTO;
import com.dockerinit.features.dockercompose.dto.spec.ServiceSpecDTO;
import com.dockerinit.features.dockercompose.dto.spec.VolumeDTO;
import com.dockerinit.features.model.FileType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record ComposeAssemblyRequest(
        String projectName,
        List<Selection> selections,
        Map<String, NetworkDTO> networks,
        Map<String, VolumeDTO> volumes,
        Set<FileType> targets
) {
    public record Selection(
        String presetSlug,
        String serviceName,
        ServiceSpecDTO overrides
    ) {}
}
