package com.dockerinit.linux.infrastructure.repository;

import com.dockerinit.linux.domain.model.LinuxCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@RequiredArgsConstructor
public class LinuxCommandRepositoryImpl implements LinuxCommandRepositoryCustom{

    private final MongoTemplate mongo;
    @Override
    public void increaseSearchCount(String commandNorm, int delta) {
        mongo.updateFirst(
                query(where("commandNorm").is(commandNorm)),
                new Update().inc("searchCount", delta),
                LinuxCommand.class
        );
    }
}
