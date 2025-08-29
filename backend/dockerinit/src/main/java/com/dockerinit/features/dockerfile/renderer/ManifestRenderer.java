package com.dockerinit.features.dockerfile.renderer;

import com.dockerinit.features.dockerfile.domain.DockerfilePlan;
import com.dockerinit.features.dockerfile.dto.request.DockerfileRequest;
import com.dockerinit.features.model.ContentType;
import com.dockerinit.features.dockerfile.domain.DockerFileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.RenderContext;
import com.dockerinit.features.renderer.ArtifactRenderer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@Order(999) //  가장 마지막
public class ManifestRenderer implements ArtifactRenderer<DockerfileRequest, DockerfilePlan, DockerFileType> {

    private static final String FILE_NAME = "manifest.json";

    @Override
    public DockerFileType fileType() {
        return DockerFileType.MANIFEST;
    }

    @Override
    public boolean supports(RenderContext<DockerfileRequest, DockerfilePlan, DockerFileType> ctx) {
        return ctx.targets().contains(DockerFileType.MANIFEST);
    }

    @Override
    public List<GeneratedFile> render(RenderContext<DockerfileRequest, DockerfilePlan, DockerFileType> ctx, List<String> warnings) {
        List<GeneratedFile> files = new ArrayList<>(ctx.untilNowArtifacts());

        files.sort(Comparator.comparing(GeneratedFile::filename));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < files.size(); i++) {
            var f = files.get(i);
            String sha = sha256Hex(f.content());
            sb.append("{")
                    .append("\"name\":\"").append(escapeJson(f.filename())).append("\",")
                    .append("\"size\":").append(f.content().length).append(",")
                    .append("\"sha256\":\"").append(sha).append("\",")
                    .append("\"contentType\":\"").append(escapeJson(
                            f.contentType() != null ? f.contentType().toString() : "application/octet-stream")).append("\",")
                    .append("\"fileType\":\"").append(f.fileType().name()).append("\",")
                    .append("\"sensitive\":").append(f.sensitive())
                    .append("}");
            if (i < files.size() - 1) sb.append(",");
        }
        sb.append("]}");

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        GeneratedFile gf = new GeneratedFile(FILE_NAME, bytes, ContentType.JSON, false, DockerFileType.MANIFEST);

        return List.of(gf);
    }

    private static String sha256Hex(byte[] data) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            var d = md.digest(data);
            var sb = new StringBuilder(d.length * 2);
            for (byte b : d) sb.append(Character.forDigit((b >>> 4) & 0xF, 16))
                    .append(Character.forDigit(b & 0xF, 16));
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\","\\\\").replace("\"","\\\"");
    }
}
