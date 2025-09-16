package com.dockerinit.linux.usage.repository;

import com.dockerinit.linux.usage.domain.OptionUsage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OptionUsageRepository extends MongoRepository<OptionUsage, String> {
    List<OptionUsage> findByCommandNormOrderByCountDesc(String commandNorm, Pageable pageable);
}
