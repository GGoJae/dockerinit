package com.dockerinit.linux.application.explain.model;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public record Invocation(
        String command,
        Map<String, String> opts,
        List<String> args,
        Locale locale
) {

    public String signatureString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd=").append(command).append("|opts=");
        opts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(
                        e -> sb.append(e.getKey())
                                .append("=").append(e.getValue() == null ? "" : e.getValue())
                                .append(";"));
        sb.append("|args=");

        for (String a : args) {
            sb.append(a).append(",");
        }

        return sb.toString();
    }
}
