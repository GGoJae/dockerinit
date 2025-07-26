package com.dockerinit.util;

import com.dockerinit.dto.makeDockerfile.DockerfileRequest;

import java.util.List;

public class DockerfileGenerator {

    public static String generate(DockerfileRequest req) {
        StringBuilder sb = new StringBuilder();

        // FROM
        sb.append("FROM ").append(req.getBaseImage()).append("\n");

        // ARG
        if (req.getArgs() != null) {
            req.getArgs().forEach((key, val) ->
                    sb.append("ARG ").append(key).append(val != null ? "=" + val : "").append("\n")
            );
        }

        // ENV (with comment)
        if ("prod".equalsIgnoreCase(req.getEnvMode()) && req.getEnvVars() != null) {
            sb.append("# ENV 설정은 외부에서 주입하세요:\n");
            req.getEnvVars().forEach((key, val) -> {
                sb.append("# export ").append(key).append("=").append(val).append("\n");
                sb.append("ENV ").append(key).append("=${").append(key).append("}\n");
            });
        }

        // WORKDIR
        if (req.getWorkdir() != null)
            sb.append("WORKDIR ").append(req.getWorkdir()).append("\n");

        // ADD
        if (req.getAdd() != null)
            for (DockerfileRequest.CopyDirective d : req.getAdd())
                sb.append("ADD ").append(d.source()).append(" ").append(d.target()).append("\n");

        // COPY
        if (req.getCopy() != null)
            for (DockerfileRequest.CopyDirective d : req.getCopy())
                sb.append("COPY ").append(d.source()).append(" ").append(d.target()).append("\n");

        // RUN
        if (req.getRun() != null)
            for (String r : req.getRun())
                sb.append("RUN ").append(r).append("\n");

        // USER
        if (req.getUser() != null)
            sb.append("USER ").append(req.getUser()).append("\n");

        // VOLUME
        if (req.getVolume() != null && !req.getVolume().isEmpty()) {
            String joined = quoteJoin(req.getVolume());
            sb.append("VOLUME [").append(joined).append("]\n");
        }

        // EXPOSE
        if (req.getExpose() != null)
            for (Integer port : req.getExpose())
                sb.append("EXPOSE ").append(port).append("\n");

        // HEALTHCHECK
        if (req.getHealthcheck() != null)
            sb.append("HEALTHCHECK CMD ").append(req.getHealthcheck()).append("\n");

        // ENTRYPOINT
        if (req.getEntrypoint() != null)
            sb.append("ENTRYPOINT ").append(jsonArray(req.getEntrypoint())).append("\n");

        // CMD
        if (req.getCmd() != null)
            sb.append("CMD ").append(jsonArray(req.getCmd())).append("\n");

        return sb.toString().trim();
    }

    private static String quoteJoin(List<String> list) {
        return String.join(", ", list.stream().map(s -> "\"" + s + "\"").toList());
    }

    private static String jsonArray(List<String> parts) {
        return "[" + quoteJoin(parts) + "]";
    }
}
