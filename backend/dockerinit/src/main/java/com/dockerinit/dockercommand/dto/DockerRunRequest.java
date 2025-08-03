package com.dockerinit.dockercommand.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class DockerRunRequest {

    private List<String> volumes;

    private String image; // 필수
    private List<String> ports;
    private List<String> env;
    private String network;
    private Boolean detach;
    private String name;

    public Boolean getDetach() { return detach != null && detach; }

}
