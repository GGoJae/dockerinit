package com.dockerinit.linux.usage.materialize;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("sys_job_state")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class JobState {
    @Id
    private String id;
    @Setter
    private Instant lastSyncedAt;

}
