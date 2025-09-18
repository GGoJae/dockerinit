package com.dockerinit.linux.usage.materialize;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface JobStateRepository extends MongoRepository<JobState, String> {
}
