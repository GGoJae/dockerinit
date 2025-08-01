package com.dockerinit.repository;

import com.dockerinit.domain.LinuxCommand;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinuxCommandRepository extends MongoRepository<LinuxCommand, String> {

    Optional<LinuxCommand> findByCommand(String command);
}
