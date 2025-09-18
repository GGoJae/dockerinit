package com.dockerinit.linux.infrastructure.repository;

import com.dockerinit.linux.domain.model.LinuxCommand;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LinuxCommandRepository extends MongoRepository<LinuxCommand, String>, LinuxCommandRepositoryCustom {

    Optional<LinuxCommand> findByCommand(String command);

    List<LinuxCommand> findTop15ByCommandNormOrderBySearchCountDesc();

    List<LinuxCommand> findAllByCommandNormStartingWithOrderBySearchCountDesc(String prefixLower, Pageable pageable);

    List<LinuxCommand> findAllByCommandStartingWith(String prefix, Pageable pageable);

    Optional<LinuxCommand> findByCommandNorm(String commandNorm);

}
