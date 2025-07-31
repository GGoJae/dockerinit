package com.dockerinit.repository;

import com.dockerinit.domain.LinuxCommand;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinuxCommandRepository extends MongoRepository<LinuxCommand, String> {
}
