package com.dockerinit.features.dockerfile.render;

import com.dockerinit.features.dockerfile.model.DockerfileSpec;
import com.dockerinit.features.dockerfile.model.RenderResult;

public interface DockerfileRenderer {
    RenderResult render(DockerfileSpec spec);
}
