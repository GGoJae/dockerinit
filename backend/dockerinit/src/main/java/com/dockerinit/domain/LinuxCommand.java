package com.dockerinit.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "linux_commands")
@Getter
public class LinuxCommand {
    @Id
    private String id;
    private String category;
    private String command;
    private String description;
    private String usage;
    private String example;
    private boolean verified;
    private Map<String, OptionInfo> options;
    private List<String> tags;
    private Integer searchCount;

    public record OptionInfo(
            String argName,      // count, interval …
            boolean required,
            String typeHint,     // "int", "duration", "string" …
            String defaultValue, // null 가능
            String description
    ) {}
}


