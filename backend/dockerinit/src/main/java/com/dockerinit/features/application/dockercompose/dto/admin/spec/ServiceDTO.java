package com.dockerinit.features.application.dockercompose.dto.admin.spec;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record ServiceDTO(
        @NotBlank
        String name,           // compose 내부 서비스 이름 (ex: app)

        @NotBlank
        String image,          // ex) eclipse-temurin:17-jre

        String containerName,

        List<String> command,  // ["java","-jar","/app/app.jar"]

        List<String> entrypoint,
        Map<String, String> environment,
        Set<String> networks,
/**
         * "8080:8080" 같은 문자열, 혹은 별도 PortDTO로 확장 가능
         */
        Set<String> ports,
/**
         * "app:/app" 같은 문자열, 혹은 VolumeDTO로 확장 가능
         */
        Set<String> volumes,
        Set<String> dependsOn,
        String restart // ex) "unless-stopped"
) {
}


