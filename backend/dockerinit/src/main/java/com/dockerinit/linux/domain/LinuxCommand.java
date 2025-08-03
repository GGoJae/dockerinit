package com.dockerinit.linux.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "linux_commands")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinuxCommand {
    @Id
    private String id;
    private String category;
    private String command;
    private String description;
    private String usage;
    private String example;
    private boolean verified;
    private boolean optionRequired;
    private Map<String, OptionInfo> options;
    private List<String> tags;
    private Long searchCount;

    public LinuxCommand(String category, String command, String description, String usage, String example, boolean verified, boolean optionRequired, Map<String, OptionInfo> options, List<String> tags) {
        this.category = category;
        this.command = command;
        this.description = description;
        this.usage = usage;
        this.example = example;
        this.verified = verified;
        this.optionRequired = optionRequired;
        this.options = options;
        this.tags = tags == null ? List.of() : tags;
        this.searchCount = 0L;
    }

    public record OptionInfo(
            String argName,      // count, interval …
            boolean argRequired,    // args 가 필수인지? 예) ping -c 8 <----
            String typeHint,     // "int", "duration", "string" …
            String defaultValue, // null 가능
            String description
    ) {}
}


