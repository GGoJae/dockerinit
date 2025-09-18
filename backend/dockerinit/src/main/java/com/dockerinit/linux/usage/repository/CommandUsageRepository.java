package com.dockerinit.linux.usage.repository;

import com.dockerinit.linux.usage.domain.CommandUsage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommandUsageRepository extends MongoRepository<CommandUsage, String> {
    List<CommandUsage> findAllByOrderByCountDesc(Pageable pageable);
}
