package com.dockerinit.linux.domain.model;

import com.dockerinit.global.validation.ValidationCollector;
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
@CompoundIndex(name = "cmd_prefix_rank", def = "{'commandNorm': 1, 'searchCount': -1}")
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

    private final ManMeta manMeta;

    private LinuxCommand(
            String id,
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
            Long searchCount,
            ManMeta manMeta
    ) {

        String cmd = Objects.requireNonNull(command, "command").trim();
        ValidationCollector.create()
                .notBlank("command", cmd, "command 가 비어있습니다.")
                .throwIfInvalid();

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
        this.manMeta = manMeta;
    }


    public static LinuxCommand createForManual(
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
                0L,
                null
        );
    }

    public static LinuxCommand createForCrawling(
            String category,
            String command,
            List<String> aliases,
            String description,
            Synopsis synopsis,
            List<String> arguments,
            List<String> examples,
            boolean optionRequired,
            Map<String, Option> options,
            List<String> tags,
            ManMeta manMeta) {

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
                0L,
                manMeta);
    }


    private static <T> List<T> emptyOrCopyList(List<T> listOrNull) {
        return listOrNull == null ? List.of() : List.copyOf(listOrNull);
    }

    private static <K, V> Map<K, V> emptyOrCopyMap(Map<K, V> mapOrNull) {
        return mapOrNull == null ? Map.of() : Map.copyOf(mapOrNull);
    }

    public Updater updater() {
        return new Updater(id, category, command, aliases, description, synopsis, arguments, examples, verified, optionRequired, options,
                tags, searchCount, manMeta);
    }

    public static class Updater {
        private final String id;
        private String category; // 카테고리 (예: 파일, 네트워크 등)
        private String command; // 명령어 이름 (예: ls, ping)
        private List<String> aliases;
        private String description; // 명령어 설명 (man의 DESCRIPTION)
        private Synopsis synopsis; // 사용 형식 (man의 SYNOPSIS)
        private List<String> arguments; // 명령어 주요 인자들 (예: FILE, HOST)
        private List<String> examples; // 여러 예시들 man, 수작업 or redis 의 zset 이용해서 주기 적으로 바꿔주면 좋을듯
        private boolean verified; // 검증 여부 (수동 검수 여부)
        private boolean optionRequired; // 옵션 필수 여부 (ex. -c 옵션 필수인가?)
        private Map<String, Option> options; // 옵션 정보 map (옵션 이름 -> 정보)
        private List<String> tags; // 관련 태그
        private final Long searchCount; // 검색된 횟수
        private ManMeta manMeta;

        private Updater(
                String id, String category, String command,
                List<String> aliases, String description, Synopsis synopsis,
                List<String> arguments, List<String> examples,
                boolean verified, boolean optionRequired, Map<String, Option> options,
                List<String> tags, Long searchCount, ManMeta manMeta) {
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
            this.manMeta = manMeta;
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
                    searchCount,
                    manMeta
            );
        }

        public Updater category(String category) {
            this.category = category;
            return this;
        }

        public Updater command(String command) {
            this.command = command;
            return this;
        }

        public Updater aliases(List<String> aliases) {
            this.aliases = aliases;
            return this;
        }

        public Updater description(String description) {
            this.description = description;
            return this;
        }

        public Updater synopsis(Synopsis synopsis) {
            this.synopsis = synopsis;
            return this;
        }

        public Updater arguments(List<String> arguments) {
            this.arguments = arguments;
            return this;
        }

        public Updater examples(List<String> examples) {
            this.examples = examples;
            return this;
        }

        public Updater verified(boolean verified) {
            this.verified = verified;
            return this;
        }

        public Updater optionRequired(boolean optionRequired) {
            this.optionRequired = optionRequired;
            return this;
        }

        public Updater options(Map<String, Option> options) {
            this.options = options;
            return this;
        }

        public Updater tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Updater manMeta(ManMeta manMeta) {
            this.manMeta = manMeta;
            return this;
        }
    }

}


