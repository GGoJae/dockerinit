package com.dockerinit.service;

import com.dockerinit.constant.ErrorMessage;
import com.dockerinit.dto.dockerCompose.DockerComposePreset;
import com.dockerinit.dto.dockerCompose.DockerComposeRequest;
import com.dockerinit.exception.CustomException.InternalErrorCustomException;
import com.dockerinit.exception.CustomException.NotFoundCustomException;
import com.dockerinit.util.DockerComposeGenerator;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DockerComposeService {

    private final Map<String, DockerComposePreset> presetMap = new HashMap<>();

    public DockerComposeService() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = this.getClass().getResourceAsStream("/data/docker-compose-presets.json");
            TypeReference<Map<String, DockerComposePreset>> typeRef = new TypeReference<>() {};
            presetMap.putAll(mapper.readValue(is, typeRef));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<DockerComposePreset> getAllPresets() {
        return presetMap.values().stream().toList();
    }

    public DockerComposePreset getPreset(String name) {
        return Optional.ofNullable(presetMap.get(name))
                .orElseThrow(() -> new NotFoundCustomException(
                        ErrorMessage.PRESET_NOT_FOUND,
                        Map.of("presetName", name)
                ));
    }

    public Resource getPresetAsYml(String name) {
        DockerComposePreset preset = Optional.ofNullable(presetMap.get(name))
                .orElseThrow(() -> new NotFoundCustomException(
                        ErrorMessage.PRESET_NOT_FOUND,
                        Map.of("presetName", name)
                ));

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile(name + "-preset", "yml");
            Files.write(tempFile, preset.ymlContent().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("docker-compose.yml"));
            zos.write(preset.ymlContent().getBytes());
            zos.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ByteArrayResource(baos.toByteArray());
    }

    public String generateCustomComposeYml(DockerComposeRequest request) {
        return DockerComposeGenerator.generateYml(request);
    }

    public Resource generateCustomComposeAsZip(DockerComposeRequest request) {
        String ymlContent = DockerComposeGenerator.generateYml(request);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("docker-compose.yml"));
            zos.write(ymlContent.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            return new ByteArrayResource(baos.toByteArray());

        } catch (IOException e) {
            throw new InternalErrorCustomException(ErrorMessage.FAILED_TO_CREATE_ZIP, e);
        }

    }
}
