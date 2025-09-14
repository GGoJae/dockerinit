package com.dockerinit.linux.domain.model;

import com.dockerinit.global.exception.InvalidInputCustomException;
import com.dockerinit.global.validation.ValidationErrors;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.domain.syntax.Synopsis;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Document(collection = "linux_commands")
@Getter
@CompoundIndex( name = "cmd_prefix_rank", def = "{'commandNorm': 1, 'searchCount': -1}")
@CompoundIndex(name = "alias_prefix_rank", def = "{'aliasesNorm': 1, 'searchCount': -1}")

public class LinuxCommand {

    @Id
    private final String id;

    private final String category; // 카테고리 (예: 파일, 네트워크 등)
    private final String command; // 명령어 이름 (예: ls, ping)

    @Indexed(direction = IndexDirection.ASCENDING, unique = true)
    private final String commandNorm; // 정규화 버전

    private final List<String> aliases;

    private final List<String> aliasesNorm;

    private final String description; // 명령어 설명 (man의 DESCRIPTION)

    private final Synopsis synopsis; // 사용 형식 (man의 SYNOPSIS)

    private final List<String> arguments; // 명령어 주요 인자들 (예: FILE, HOST)

    private final List<String> examples; // 여러 예시들 man, 수작업 or redis 의 zset 이용해서 주기 적으로 바꿔주면 좋을듯

    private final boolean verified; // 검증 여부 (수동 검수 여부)

    private final boolean optionRequired; // 옵션 필수 여부 (ex. -c 옵션 필수인가?)

    private final Map<String, Option> options; // 옵션 정보 map (옵션 이름 -> 정보)

    private final List<String> tags; // 관련 태그

    @Indexed(direction = IndexDirection.DESCENDING)
    private final Long searchCount; // 검색된 횟수

    private LinuxCommand(String id,
                 String category,
                 String command,
                 List<String> aliases,
                 String description,
                 Synopsis synopsis,
                 List<String> arguments,
                 List<String> examples,
                 boolean verified,
                 boolean optionRequired,
                 Map<String, Option> options,
                 List<String> tags,
                 Long searchCount) {

        String cmd = Objects.requireNonNull(command, "command").trim();

        ValidationErrors.create()
                .notBlank("command", cmd, "command 가 비어있습니다.")
                .judge();

        String commandNorm = cmd.toLowerCase(Locale.ROOT);
        List<String> aliasesSafe = emptyOrCopyList(aliases);
        List<String> aliasesNorm = aliasesSafe.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        this.id = id;
        this.category = category;
        this.command = cmd;
        this.commandNorm = commandNorm; // 계산하지 않음 (저장값 그대로)
        this.aliases = aliasesSafe;
        this.aliasesNorm = aliasesNorm;
        this.description = description;
        this.synopsis = synopsis;
        this.arguments = arguments == null ? List.of() : List.copyOf(arguments);
        this.examples = examples == null ? List.of() : List.copyOf(examples);
        this.verified = verified;
        this.optionRequired = optionRequired;
        this.options = options == null ? Map.of() : Map.copyOf(options);
        this.tags = tags == null ? List.of() : List.copyOf(tags);
        this.searchCount = (searchCount == null) ? 0L : searchCount;
    }


    public static LinuxCommand createForManual(String category,
                                                String command,
                                                List<String> aliases,
                                                String description,
                                                Synopsis synopsis,
                                                List<String> arguments,
                                                List<String> examples,
                                                boolean verified,
                                                boolean optionRequired,
                                                Map<String, Option> options,
                                                List<String> tags) {


        return new LinuxCommand(
                null,
                category,
                command,
                aliases,
                description,
                synopsis,
                emptyOrCopyList(arguments),
                emptyOrCopyList(examples),
                verified,
                optionRequired,
                emptyOrCopyMap(options),
                emptyOrCopyList(tags),
                0L);
    }

    public static LinuxCommand createForCrawling(String category,
                                                String command,
                                                List<String> aliases,
                                                String description,
                                                Synopsis synopsis,
                                                List<String> arguments,
                                                List<String> examples,
                                                boolean optionRequired,
                                                Map<String, Option> options,
                                                List<String> tags) {


        return new LinuxCommand(
                null,
                category,
                command,
                aliases,
                description,
                synopsis,
                emptyOrCopyList(arguments),
                emptyOrCopyList(examples),
                false,
                optionRequired,
                emptyOrCopyMap(options),
                emptyOrCopyList(tags),
                0L);
    }


    private static <T> List<T> emptyOrCopyList(List<T> listOrNull) {
        return listOrNull == null ? List.of() : List.copyOf(listOrNull);
    }

    private static <K, V> Map<K, V> emptyOrCopyMap(Map<K, V> mapOrNull) {
        return mapOrNull == null ? Map.of() : Map.copyOf(mapOrNull);
    }

    public Updating updater() {
        return new Updating(id, category, command, aliases, description, synopsis, arguments, examples, verified, optionRequired, options,
                tags, searchCount);
    }

    static class Updating {
        private final String id;
        private  String category; // 카테고리 (예: 파일, 네트워크 등)
        private  String command; // 명령어 이름 (예: ls, ping)
        private  List<String> aliases;
        private  String description; // 명령어 설명 (man의 DESCRIPTION)
        private Synopsis synopsis; // 사용 형식 (man의 SYNOPSIS)
        private List<String> arguments; // 명령어 주요 인자들 (예: FILE, HOST)
        private  List<String> examples; // 여러 예시들 man, 수작업 or redis 의 zset 이용해서 주기 적으로 바꿔주면 좋을듯
        private boolean verified; // 검증 여부 (수동 검수 여부)
        private  boolean optionRequired; // 옵션 필수 여부 (ex. -c 옵션 필수인가?)
        private  Map<String, Option> options; // 옵션 정보 map (옵션 이름 -> 정보)
        private List<String> tags; // 관련 태그
        private final Long searchCount; // 검색된 횟수

        public Updating(String id, String category, String command, List<String> aliases, String description, Synopsis synopsis, List<String> arguments, List<String> examples, boolean verified, boolean optionRequired, Map<String, Option> options, List<String> tags, Long searchCount) {
            this.id = id;
            this.category = category;
            this.command = command;
            this.aliases = aliases;
            this.description = description;
            this.synopsis = synopsis;
            this.arguments = arguments;
            this.examples = examples;
            this.verified = verified;
            this.optionRequired = optionRequired;
            this.options = options;
            this.tags = tags;
            this.searchCount = searchCount;
        }

        public LinuxCommand update() {
            return new LinuxCommand(
                    id,
                    category,
                    command,
                    aliases,
                    description,
                    synopsis,
                    arguments,
                    examples,
                    verified,
                    optionRequired,
                    options,
                    tags,
                    searchCount
            );
        }

        public Updating category(String category) {
            this.category = category;
            return this;
        }

        public Updating command(String command) {
            this.command = command;
            return this;
        }

        public Updating aliases(List<String> aliases) {
            this.aliases = aliases;
            return this;
        }

        public Updating description(String description) {
            this.description = description;
            return this;
        }

        public Updating synopsis(Synopsis synopsis) {
            this.synopsis = synopsis;
            return this;
        }

        public Updating arguments(List<String> arguments) {
            this.arguments = arguments;
            return this;
        }

        public Updating examples(List<String> examples) {
            this.examples = examples;
            return this;
        }

        public Updating verified(boolean verified) {
            this.verified = verified;
            return this;
        }

        public Updating optionRequired(boolean optionRequired) {
            this.optionRequired = optionRequired;
            return this;
        }

        public Updating options(Map<String, Option> options) {
            this.options = options;
            return this;
        }

        public Updating tags(List<String> tags) {
            this.tags = tags;
            return this;
        }
    }

}


