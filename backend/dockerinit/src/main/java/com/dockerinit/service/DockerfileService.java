package com.dockerinit.service;

import com.dockerinit.dto.dockerfile.DockerfilePreset;
import com.dockerinit.dto.dockerfile.DockerfileRequest;
import com.dockerinit.util.DockerfileGenerator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DockerfileService {

    public String generateDockerfile(DockerfileRequest req) {
        StringBuilder sb = new StringBuilder();

        // 1. FROM
        sb.append("FROM ").append(req.getBaseImage()).append("\n");

        // 2. LABEL
        if (req.getLabel() != null) {
            req.getLabel().forEach((k, v) ->
                    sb.append("LABEL ").append(k).append("=\"").append(v).append("\"\n")
            );
        }

        // 3. ARG
        if (req.getArgs() != null) {
            req.getArgs().forEach((k, v) ->
                    sb.append("ARG ").append(k).append("=").append(v).append("\n")
            );
        }

        // 4. ENV
        if (req.getEnvVars() != null && !req.getEnvVars().isEmpty()) {
            String mode = req.getEnvMode() != null ? req.getEnvMode().toLowerCase() : "prod";

            if (mode.equals("test")) {
                req.getEnvVars().forEach((key, val) ->
                        sb.append("ENV ").append(key).append("=").append(val).append("\n")
                );
            } else {
                sb.append("# ENV 설정은 외부에서 주입하세요:\n");
                req.getEnvVars().forEach((key, val) -> {
                    sb.append("# export ").append(key).append("=").append(val).append("\n");
                    sb.append("ENV ").append(key).append("=${").append(key).append("}\n");
                });
            }
        }


        // 5. WORKDIR
        if (req.getWorkdir() != null && !req.getWorkdir().isEmpty()) {
            sb.append("WORKDIR ").append(req.getWorkdir()).append("\n");
        }

        // 6. ADD
        if (req.getAdd() != null) {
            for (DockerfileRequest.CopyDirective a : req.getAdd()) {
                sb.append("ADD ").append(a.source()).append(" ").append(a.target()).append("\n");
            }
        }

        // 7. COPY
        if (req.getCopy() != null) {
            for (DockerfileRequest.CopyDirective c : req.getCopy()) {
                sb.append("COPY ").append(c.source()).append(" ").append(c.target()).append("\n");
            }
        }

        // 8. RUN
        if (req.getRun() != null) {
            req.getRun().forEach(runCmd ->
                    sb.append("RUN ").append(runCmd).append("\n")
            );
        }

        // 9. USER
        if (req.getUser() != null) {
            sb.append("USER ").append(req.getUser()).append("\n");
        }

        // 10. VOLUME
        if (req.getVolume() != null) {
            req.getVolume().forEach(path ->
                    sb.append("VOLUME [\"").append(path).append("\"]\n")
            );
        }

        // 11. EXPOSE
        if (req.getExpose() != null) {
            req.getExpose().forEach(port ->
                    sb.append("EXPOSE ").append(port).append("\n")
            );
        }

        // 12. HEALTHCHECK
        if (req.getHealthcheck() != null && !req.getHealthcheck().isEmpty()) {
            sb.append("HEALTHCHECK CMD ").append(req.getHealthcheck()).append("\n");
        }

        // 13. ENTRYPOINT
        if (req.getEntrypoint() != null && !req.getEntrypoint().isEmpty()) {
            sb.append("ENTRYPOINT [");
            sb.append(req.getEntrypoint().stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));
            sb.append("]\n");
        }

        // 14. CMD
        if (req.getCmd() != null && !req.getCmd().isEmpty()) {
            sb.append("CMD [");
            sb.append(req.getCmd().stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));
            sb.append("]\n");
        }

        return sb.toString();
    }

    public List<DockerfilePreset> getAllPresets() {
        return List.of(
                springBootJar(),
                nodeJsExpress(),
                pythonFlask()
        );
    }

    public Optional<DockerfilePreset> getPresentByName(String name) {
        return getAllPresets().stream()
                .filter(p -> p.name().equalsIgnoreCase(name))
                .findFirst();
    }

    private DockerfilePreset springBootJar() {
        DockerfileRequest req = new DockerfileRequest();
        req.setBaseImage("openjdk:17");
        req.setWorkdir("/app");
        req.setCopy(List.of(new DockerfileRequest.CopyDirective(".", ".")));
        req.setEnvMode("prod");
        req.setEnvVars(Map.of("SPRING_PROFILES_ACTIVE", "prod"));
        req.setExpose(List.of(8080));
        req.setCmd(List.of("java", "-jar", "app.jar"));
        return new DockerfilePreset("Spring Boot JAR", DockerfileGenerator.generate(req));
    }

    private DockerfilePreset nodeJsExpress() {
        DockerfileRequest req = new DockerfileRequest();
        req.setBaseImage("node:20");
        req.setWorkdir("/app");
        req.setCopy(List.of(new DockerfileRequest.CopyDirective(".", ".")));
        req.setRun(List.of("npm install"));
        req.setEnvMode("prod");
        req.setEnvVars(Map.of("NODE_ENV", "production"));
        req.setExpose(List.of(3000));
        req.setCmd(List.of("node", "app.js"));
        return new DockerfilePreset("Node.js Express", DockerfileGenerator.generate(req));
    }

    private DockerfilePreset pythonFlask() {
        DockerfileRequest req = new DockerfileRequest();
        req.setBaseImage("python:3.11");
        req.setWorkdir("/app");
        req.setAdd(List.of(new DockerfileRequest.CopyDirective("requirements.txt", ".")));
        req.setCopy(List.of(new DockerfileRequest.CopyDirective(".", ".")));
        req.setRun(List.of("pip install -r requirements.txt"));
        req.setEnvMode("prod");
        req.setEnvVars(Map.of("FLASK_ENV", "production"));
        req.setExpose(List.of(5000));
        req.setCmd(List.of("python", "app.py"));
        return new DockerfilePreset("Python Flask", DockerfileGenerator.generate(req));
    }


    public byte[] downloadDockerfile(DockerfileRequest request) {
        String dockerfileContent = DockerfileGenerator.generate(request);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("Dockerfile");
            zos.putNextEntry(entry);
            zos.write(dockerfileContent.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return baos.toByteArray();
    }
}
