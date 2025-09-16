package com.dockerinit.linux.usage.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("linux_option_usage")
@CompoundIndex(name = "cmd_flag_unique", def = "{'commandNorm':1, 'flag':1}", unique = true)
@Getter
public class OptionUsage {

    @Id
    private String id;
    private String commandNorm;
    private String flag;
    private long count;
    private Instant updatedAt;

    protected OptionUsage() {}

    public OptionUsage(String commandNorm, String flag, long count, Instant updatedAt) {
        this.commandNorm = commandNorm;
        this.flag = flag;
        this.count = count;
        this.updatedAt = updatedAt;
    }
}
