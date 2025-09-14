package com.dockerinit.features.application.dockercompose.domain.model;

import com.dockerinit.global.exception.InvalidInputCustomException;
import com.dockerinit.global.validation.ValidationErrors;

public record PortMapping(
        Integer containerPort,
        Integer hostPort,
        PortProtocol protocol,
        String hostIp
) {
    public PortMapping {
        ValidationErrors.create()
                .range("containerPort", containerPort, 1, 65535, "port는 1~ 65535 사이여야합니다.")
                .judge();

        if (hostPort == null || hostPort < 1 || hostPort > 65535) {
            hostPort = containerPort;
        }
        protocol = (protocol == null) ? PortProtocol.TCP : protocol;
        hostIp = (hostIp == null || hostIp.isBlank()) ? null : hostIp;
    }
}
