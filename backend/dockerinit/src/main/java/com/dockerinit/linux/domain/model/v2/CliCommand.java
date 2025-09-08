package com.dockerinit.linux.domain.model.v2;

import com.dockerinit.linux.domain.syntax.v2.Ecosystem;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.domain.syntax.Synopsis;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import com.dockerinit.linux.domain.syntax.v2.Example;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document("cli_commands")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CliCommand {
    @Id
    private String id;

    private Ecosystem ecosystem;          // LINUX/DOCKER/GIT/...
    private String binary;                // "docker", "git", "ls", "kubectl"
    private List<String> subcommandPath;  // ["run"] / ["commit"] / []
    private String command;               // 표시용: "docker run", "git commit", "ls"

    private String description;
    private Synopsis synopsis;            // 네가 만든 타입 유지
    private Map<String, Option> options;  // 기존 타입 유지
    private List<String> arguments;       // 간단 라벨이면 유지

    private List<Example> examples;       // 구조화 추천 (아래 참고)
    private List<String> aliases;

    private String packageName;           // coreutils/util-linux/iproute2 등
    private String manSection;            // "1","8"...
    private String docUrl;
    private Map<String,String> install;   // pm별 설치 힌트

    private Map<Integer,String> exitCodes;
    private Map<String,String> envVars;
    private List<String> seeAlsoSlugs;

    private String dangerLevel;           // SAFE/CAUTION/DANGEROUS
    private String category;
    private List<String> tags;

    private boolean verified;
    private boolean optionRequired;

    private Long searchCount;
    private Instant lastSearchedAt;

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}

