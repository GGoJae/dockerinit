package com.dockerinit.dockercommand.service;

import com.dockerinit.dockercommand.dto.DockerRunRequest;
import org.springframework.stereotype.Service;

import java.util.StringJoiner;

@Service
public class DockerCommandService {

    public String generateDockerRunCommand(DockerRunRequest request) {
        StringJoiner cmd = new StringJoiner(" ");
        cmd.add("docker run");

        if (request.getDetach()) {
            cmd.add("-d");
        }

        if (request.getName() != null && !request.getName().isEmpty()) {
            cmd.add("--name " + request.getName());
        }

        if (request.getPorts() != null) {
            request.getPorts().forEach(port -> cmd.add("-p " + port));
        }

        if (request.getVolumes() != null) {
            request.getVolumes().forEach(vol -> cmd.add("-v " + vol));
        }

        if (request.getNetwork() != null && !request.getNetwork().isEmpty()) {
            cmd.add("--network " + request.getNetwork());
        }

        cmd.add(request.getImage());

        return cmd.toString();
    }
}
