package com.dockerinit.service;

import com.dockerinit.linux.application.service.LinuxCommandService;
import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.domain.syntax.*;
import com.dockerinit.linux.dto.request.LinuxAutoCompleteRequest;
import com.dockerinit.linux.dto.response.SuggestionType;
import com.dockerinit.linux.dto.response.v2.*;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class LinuxCommandServiceIT {

    @Autowired
    LinuxCommandService service;
    @Autowired LinuxCommandRepository repository;
    @Autowired StringRedisTemplate redis;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        repository.saveAll(sampleCommands());
    }

    @Test
    @DisplayName("prefix로 pi를 치면 ping과 replace range가_나오나?")
    void prefix_로_pi를_치면_ping과_replace_range가_나오나() {
        LinuxAutoCompleteRequest req = new LinuxAutoCompleteRequest("pi", 2);

        LinuxAutoCompleteResponseV2 res = service.autocompleteCommand(req);

        assertThat(res.base().unknown()).isTrue();

        log.info("response is {}",res);
        SuggestionGroupDTO cmdGroup = res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.COMMAND.name()))
                .findFirst().orElseThrow();

        assertThat(cmdGroup.items().stream().map(
                SuggestionV2::value
        )).contains("ping");

        SuggestionV2 first = cmdGroup.items().get(0);
        assertThat(first.replaceStart()).isEqualTo(0);
        assertThat(first.replaceEnd()).isEqualTo(2);

    }

    @Test
    @DisplayName("ping -까지 치면 option 과 placeholder 가 나오나? ")
    void ping_dash_까지_치면_OPTION_PLACEHOLDER_나오나() {
        LinuxAutoCompleteRequest req = new LinuxAutoCompleteRequest("ping -", "ping -".length());

        LinuxAutoCompleteResponseV2 res = service.autocompleteCommand(req);

        assertThat(res.base().unknown()).isFalse();
        SuggestionGroupDTO optGroup = res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.OPTION.name()))
                .findFirst().orElseThrow();

        SuggestionV2 sug = optGroup.items().stream().filter(s -> s.value().equals("-c"))
                .findFirst().orElseThrow();
        assertThat(sug.display()).contains("-c");

    }

    @Test
    @DisplayName("ping -c 까지 치면 argument 가 제안되나?")
    void ping_dash_c_space_까지_치면_argument_를_제안하나() {
        LinuxAutoCompleteRequest req = new LinuxAutoCompleteRequest("ping -c ", "ping -c ".length());

        LinuxAutoCompleteResponseV2 res = service.autocompleteCommand(req);

        SuggestionGroupDTO argGroup = res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.ARGUMENT.name()))
                .findFirst().orElseThrow();

        SuggestionV2 sug = argGroup.items().get(0);
        assertThat(sug.value()).contains("<count>");
    }

    @Test
    @DisplayName("mv a.txt 를 치면 target 을 예상하는가?")
    void mv_와_하나의_소스를_입력하면_타켓이_예상되는가() {
        LinuxAutoCompleteRequest req = new LinuxAutoCompleteRequest("mv a.txt ", "mv a.txt ".length());

        LinuxAutoCompleteResponseV2 res = service.autocompleteCommand(req);

        SynopsisDTO syn = res.synopsis();
        assertThat(syn.position()).isEqualTo(1);
        assertThat(syn.patterns()).isNotEmpty();
        SynopsisPatternDTO pat = syn.patterns().get(0);
        assertThat(pat.progress().filledCount()).isGreaterThanOrEqualTo(1);
        assertThat(pat.progress().requiredRemaining()).isGreaterThanOrEqualTo(1);

        SuggestionGroupDTO targ = res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.TARGET.name()))
                .findFirst().orElseThrow();

        assertThat(targ).isNotNull();
    }

    @Test
    @DisplayName("ls 를 입력하면 -l이 제안되는가?")
    void ls_space_입력하면_l_제안되나() {
        LinuxAutoCompleteRequest req = new LinuxAutoCompleteRequest("ls ", "ls ".length());

        LinuxAutoCompleteResponseV2 res = service.autocompleteCommand(req);

        SuggestionGroupDTO optGroup = res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.OPTION.name()))
                .findFirst().orElseThrow();

        assertThat(optGroup).isNotNull();
        assertThat(optGroup.items().stream().map(SuggestionV2::value)).contains("-l");
    }

    private static List<LinuxCommand> sampleCommands() {
        return List.of(
                ping(),
                mv(),
                ls(),
                tar(),
                grep()
        );
    }

    private static LinuxCommand ping() {
        // synopsis: [OPTION*] [HOST]
        var syn = new Synopsis(List.of(
                new SynopsisPattern(List.of(
                        td(TokenType.OPTION, true,  true,  "옵션"),
                        td(TokenType.ARGUMENT, false, false, "HOST")
                ))
        ));
        Map<String, Option> opts = Map.of(
                "-c", new Option("count", true, "int", null, "보낼 ping 횟수"),
                "-i", new Option("interval", true, "float", "1", "요청 간격(초)")
        );
        return new LinuxCommand(
                "네트워크", "ping", "네트워크 상태 확인",
                syn,
                List.of("HOST"),
                List.of("ping -c 3 google.com", "ping -i 0.5 8.8.8.8"),
                false,
                opts,
                List.of("icmp","연결확인")
        );
    }

    private static LinuxCommand mv() {
        var syn = new Synopsis(List.of(
                // 파일 → 대상
                new SynopsisPattern(List.of(
                        td(TokenType.FILE, false, false, "SOURCE"),
                        td(TokenType.DESTINATION, false, false, "DEST")
                )),
                // 여러 파일 → 디렉터리
                new SynopsisPattern(List.of(
                        td(TokenType.FILE, true, false, "SOURCES..."),
                        td(TokenType.DIRECTORY, false, false, "DEST DIR")
                ))
        ));
        return new LinuxCommand(
                "파일", "mv", "파일/디렉터리 이동 또는 이름 변경",
                syn,
                List.of("SOURCE","DEST"),
                List.of("mv a.txt b.txt","mv *.log /var/logs/"),
                false,
                Map.of("-f", new Option(null, false, null, null, "질문 없이 덮어쓰기")),
                List.of("move","rename")
        );
    }

    private static LinuxCommand ls() {
        var syn = new Synopsis(List.of(
                new SynopsisPattern(List.of(
                        td(TokenType.OPTION, true, true, "옵션"),
                        td(TokenType.PATH, false, true, "PATH")
                ))
        ));
        return new LinuxCommand(
                "파일", "ls", "디렉터리 내용 나열",
                syn,
                List.of("PATH"),
                List.of("ls -l /var/log"),
                false,
                Map.of("-l", new Option(null, false, null, null, "긴 형식으로 표시")),
                List.of("list")
        );
    }

    private static LinuxCommand tar() {
        var syn = new Synopsis(List.of(
                new SynopsisPattern(List.of(
                        td(TokenType.OPTION, true, true, "옵션"),
                        td(TokenType.PATH, false, true, "PATH")
                ))
        ));
        return new LinuxCommand(
                "압축", "tar", "테이프 아카이브 유틸리티",
                syn,
                List.of("PATH"),
                List.of("tar -xvf file.tar", "tar -czf out.tar.gz dir/"),
                false,
                Map.of("-x", new Option(null, false, null, null, "해제"),
                        "-f", new Option("file", true, "path", null, "파일 지정")),
                List.of("archive")
        );
    }

    private static LinuxCommand grep() {
        var syn = new Synopsis(List.of(
                new SynopsisPattern(List.of(
                        td(TokenType.OPTION, true, true, "옵션"),
                        td(TokenType.ARGUMENT, false, false, "PATTERN"),
                        td(TokenType.PATH, false, true, "PATH")
                ))
        ));
        return new LinuxCommand(
                "텍스트", "grep", "패턴 검색",
                syn,
                List.of("PATTERN","PATH"),
                List.of("grep -i error app.log"),
                false,
                Map.of("-i", new Option(null, false, null, null, "대소문자 무시"),
                        "-E", new Option(null, false, null, null, "확장 정규식")),
                List.of("search","regex")
        );
    }
    private static TokenDescriptor td(TokenType t, boolean repeat, boolean optional, String desc) {
        return new TokenDescriptor(t, repeat, optional, desc);
    }

//
//    @BeforeEach
//    void seed() {
//        repo.deleteAll();
//
//        repo.saveAll(List.of(
//                cmd("ls",  Map.of("-l", opt("format","false"),
//                        "-a", opt("all","false"))),
//                cmd("ping",Map.of("-c", opt("count","true"),
//                        "-i", opt("interval","false"))),
//                cmd("grep",Map.of("-i", opt("ignore-case","false"))),
//                cmd("cat",  Map.of()),
//                cmd("tar",  Map.of("-xzf", opt("archive","true")))
//        ));
//
//        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
//    }
//
//    /* ---------- 실제 테스트 ---------- */
//
//    @Test
//    @DisplayName("리눅스 자동완성 레디스에 캐시한 탑 매치 잘 나와?")
//    void commandPhase_returnsTopMatches() {
//        LinuxAutoCompleteResponse res =
//                service.autocompleteCommand(new LinuxAutoCompleteRequest("p", null));
//        log.debug("response is {}", res);
//        assertThat(res.phase()).isEqualTo(AcPhase.COMMAND);
//        assertThat(res.suggestions())
//                .extracting(s -> s.value())
//                .containsExactly("ping");
//    }
//
//    @Test
//    @DisplayName("리눅스 자동완성 ping -까지 쳤을때 옵션들 잘 나와?")
//    void optionPhase_returnsFlagList() {
//        LinuxAutoCompleteResponse res =
//                service.autocompleteCommand(new LinuxAutoCompleteRequest("ping -", null));
//
//        log.debug("response is {}", res);
//        assertThat(res.phase()).isEqualTo(AcPhase.OPTION);
//        assertThat(res.suggestions())
//                .extracting(s -> s.value())
//                .containsExactlyInAnyOrder("-c", "-i");
//    }
//
//    @Test
//    @DisplayName("리눅스 명령어에 옵션이 인수가 필수일떄 플레이스 홀더 잘 나오나?")
//    void argumentPhase_returnsPlaceholder() {
//        LinuxAutoCompleteResponse res =
//                service.autocompleteCommand(new LinuxAutoCompleteRequest("ping -c ", null));
//        log.info("응답 내용은 이것 {}", res);
//
//        assertThat(res.phase()).isEqualTo(AcPhase.ARGUMENT);
//        assertThat(res.suggestions().get(0).value()).isEqualTo("<count>");
//    }
//
//    @Test
//    @DisplayName("이상한 명령어를 치면 빈값 나오나?")
//    void wrongCommand_throwException() {
//        LinuxAutoCompleteResponse res = service.autocompleteCommand(new LinuxAutoCompleteRequest("wrongString", null));
//        log.debug("response is {}", res);
//        assertThat(res.suggestions()).isEmpty();
//    }
//
//
//    private static LinuxCommand cmd(String command, Map<String, LinuxCommand.OptionInfo> opts) {
//        return new LinuxCommand(
//                "기본", command, command + " 설명", command + " [옵션]",
//                null, null, !opts.isEmpty(), opts, List.of(command));
//    }
//    private static LinuxCommand.OptionInfo opt(String arg, String required) {
//        return new LinuxCommand.OptionInfo(arg, Boolean.parseBoolean(required),
//                null, null, arg + " 설명");
//    }
}
