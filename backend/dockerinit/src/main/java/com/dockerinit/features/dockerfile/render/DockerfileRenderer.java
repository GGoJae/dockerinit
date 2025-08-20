package com.dockerinit.features.dockerfile.render;

import com.dockerinit.features.dockerfile.model.DockerfileSpec;

public interface DockerfileRenderer {
    String render(DockerfileSpec spec);
}
