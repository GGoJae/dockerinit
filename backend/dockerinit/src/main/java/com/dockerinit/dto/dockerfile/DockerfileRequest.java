package com.dockerinit.dto.dockerfile;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DockerfileRequest {
    private String baseImage;
    private String workdir;
    private List<CopyDirective> copy;
    private String envMode; // "test" or "prod"
    private Map<String, String> envVars;
    private List<Integer> expose;
    private List<String> cmd;
    private List<String> run;
    private List<String> entrypoint;
    private Map<String, String> label;
    private String user;
    private Map<String, String> args;
    private List<CopyDirective> add;
    private String healthcheck;
    private List<String> volume;

    public record CopyDirective(
            String source, String target
    ) {
    }
}
