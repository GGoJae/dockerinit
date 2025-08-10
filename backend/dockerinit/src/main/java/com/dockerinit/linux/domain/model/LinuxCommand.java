package com.dockerinit.linux.domain.model;

import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.domain.syntax.Synopsis;
import com.dockerinit.linux.domain.syntax.TokenDescriptor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "linux_commands")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinuxCommand {

    @Id
    private String id;

    private String category; // 카테고리 (예: 파일, 네트워크 등)
    private String command; // 명령어 이름 (예: ls, ping)

    private String description; // 명령어 설명 (man의 DESCRIPTION)

    private Synopsis synopsis; // 사용 형식 (man의 SYNOPSIS)

    private List<String> arguments; // 명령어 주요 인자들 (예: FILE, HOST)

    private List<String> examples; // 여러 예시들 man, 수작업 or redis 의 zset 이용해서 주기 적으로 바꿔주면 좋을듯

    private boolean verified; // 검증 여부 (수동 검수 여부)

    private boolean optionRequired; // 옵션 필수 여부 (ex. -c 옵션 필수인가?)

    private Map<String, Option> options; // 옵션 정보 map (옵션 이름 -> 정보)

    private List<String> tags; // 관련 태그

    private Long searchCount; // 검색된 횟수

    public LinuxCommand(String category, String command, String description, Synopsis synopsis, List<String> arguments, List<String> examples, boolean optionRequired, Map<String, Option> options, List<String> tags) {
        this.category = category;
        this.command = command;
        this.description = description;
        this.synopsis = synopsis;
        this.verified = false;
        this.arguments = arguments;
        this.examples = examples;
        this.optionRequired = optionRequired;
        this.options = options;
        this.tags = tags;
        this.searchCount = 0L;
    }


}


