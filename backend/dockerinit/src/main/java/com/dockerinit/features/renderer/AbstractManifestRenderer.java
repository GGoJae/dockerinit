package com.dockerinit.features.renderer;

import com.dockerinit.features.model.ContentType;
import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.RenderContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractManifestRenderer<RQ, PL> implements ArtifactRenderer<RQ, PL>{

    private static final String FILE_NAME = "manifest.json";

    @Override
    public FileType fileType() {
        return FileType.MANIFEST;
    }

    @Override
    public boolean supports(RenderContext<RQ, PL> ctx) {
        return ctx.targets().contains(FileType.MANIFEST);
    }

    @Override
    public int order() {
        return 999;     // 항상 마지막
    }

    @Override
    public List<GeneratedFile> render(RenderContext<RQ, PL> ctx, List<String> warnings) {
        List<GeneratedFile> files = new ArrayList<>(ctx.untilNowArtifacts());
        files.sort(Comparator.comparing(GeneratedFile::filename));

        StringBuilder sb = new StringBuilder(256 + files.size() * 128);
        sb.append("{\"files\":[");
        for (int i = 0; i < files.size(); i++) {
            GeneratedFile f = files.get(i);
            String sha = sha256Hex(f.content());
            sb.append("{")
                    .append("\"name\":\"").append(escapeJson(f.filename())).append("\",")
                    .append("\"size\":").append(f.content().length).append(",")
                    .append("\"sha256\":\"").append(sha).append("\",")
                    .append("\"contentType\":\"").append(escapeJson(
                            f.contentType() != null ? f.contentType().value() : "application/octet-stream")).append("\",")
                    .append("\"fileType\":\"").append(f.fileType().name()).append("\",")
                    .append("\"sensitive\":").append(f.sensitive())
                    .append("}");
            if (i < files.size() - 1) sb.append(",");
        }
        sb.append("]}");

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        GeneratedFile file = new GeneratedFile(FILE_NAME, bytes, ContentType.JSON, false, FileType.MANIFEST);

        return List.of(file);
    }

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(data);
            StringBuilder sb = new StringBuilder(d.length * 2);
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
