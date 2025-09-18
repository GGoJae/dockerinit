package com.dockerinit.linux.usage.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("linux_command_usage")
public class CommandUsage {
    @Id
    private String id;

    @Indexed(unique = true)
    private String commandNorm;   // 소문자 정규화
    private long count;           // 누적 사용량
    private Instant updatedAt;

    protected CommandUsage() {}
    public CommandUsage(String commandNorm, long count, Instant updatedAt) {
        this.commandNorm = commandNorm;
        this.count = count;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getCommandNorm() { return commandNorm; }
    public long getCount() { return count; }
    public Instant getUpdatedAt() { return updatedAt; }
}
