package com.dockerinit.service;

import com.dockerinit.linux.application.service.AutocompleteService;
import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.domain.syntax.*;
import com.dockerinit.linux.dto.request.CommandAutocompleteRequest;
import com.dockerinit.linux.dto.response.autocompleteV1.LinuxAutocompleteResponse;
import com.dockerinit.linux.dto.response.autocompleteV1.SuggestionType;
import com.dockerinit.linux.dto.response.autocompleteV1.*;
import com.dockerinit.linux.infrastructure.repository.LinuxCommandRepository;
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
    AutocompleteService service;
    @Autowired LinuxCommandRepository repository;
    @Autowired StringRedisTemplate redis;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        repository.saveAll(sampleCommands());
    }

    @Test
    @DisplayName("prefix로 pi를 치면 ping과 replace range가_나오나?")
    void prefix가_pi_일때_ping을_제안하고_replace_range_나오는가() {
        String line ="pi";
        Integer cursor = line.length();

        LinuxAutocompleteResponse res = getAutocompleteRes(line, cursor);

        assertThat(res.base().unknown()).isTrue();

        log.info("response is {}",res);
        SuggestionGroupDTO cmdGroup = res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.COMMAND.name()))
                .findFirst().orElseThrow();

        assertThat(cmdGroup.items().stream().map(
                Suggestion::value
        )).contains("ping");

        Suggestion first = cmdGroup.items().get(0);
        assertThat(first.replaceStart()).isEqualTo(0);
        assertThat(first.replaceEnd()).isEqualTo(2);

    }

    @Test
    @DisplayName("커서를 입력 안했을때도 위와 같이 동작하는가")
    void cursor가_null_이여도_똑같이_동작하나() {
        String line ="pi";
        Integer cursor = null;

        LinuxAutocompleteResponse res = getAutocompleteRes(line, cursor);

        assertThat(res.base().unknown()).isTrue();

        SuggestionGroupDTO cmdGroup = res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.COMMAND.name()))
                .findFirst().orElseThrow();

        assertThat(cmdGroup.items().stream().map(
                Suggestion::value
        )).contains("ping");

        Suggestion first = cmdGroup.items().get(0);
        assertThat(first.replaceStart()).isEqualTo(0);
        assertThat(first.replaceEnd()).isEqualTo(2);

    }


    @Test
    @DisplayName("'ping -c'입력 시 option만 보이고 -c 토큰 범위를 치환하나")
    void ping_dash_c까지_입력시_option만보이고_replace_token보이나() {
        String line ="ping -c";
        Integer cursor = line.length();

        LinuxAutocompleteResponse res = getAutocompleteRes(line, cursor);

        SuggestionGroupDTO opt = res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.OPTION.name()))
                .findFirst().orElseThrow();

        boolean hasArg = res.suggestions().groups().stream()
                .anyMatch(g -> g.group().equals(SuggestionType.ARGUMENT.name()));

        assertThat(hasArg).isFalse();

        Suggestion o0 = opt.items().get(0);
        assertThat(line.substring(o0.replaceStart(), o0.replaceEnd())).isEqualTo("-c");

    }

    @Test
    @DisplayName("ping -까지 치면 option 과 placeholder 가 나오나? ")
    void ping_dash_까지_치면_OPTION_PLACEHOLDER_나오나() {
        String line = "ping -";
        Integer cursor = line.length();

        LinuxAutocompleteResponse res = getAutocompleteRes(line, cursor);

        assertThat(res.base().unknown()).isFalse();
        SuggestionGroupDTO optGroup = res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.OPTION.name()))
                .findFirst().orElseThrow();

        Suggestion sug = optGroup.items().stream().filter(s -> s.value().equals("-c"))
                .findFirst().orElseThrow();
        assertThat(sug.display()).contains("-c");

    }

    @Test
    @DisplayName("'ping -c '까지 치면 argument 가 제안되나?")
    void ping_dash_c_space_까지_치면_argument_를_제안하나() {
        String line ="ping -c ";
        Integer cursor = line.length();

        LinuxAutocompleteResponse res = getAutocompleteRes(line, cursor);

        SuggestionGroupDTO argGroup = res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.ARGUMENT.name()))
                .findFirst().orElseThrow();

        assertThat(res.suggestions().groups().get(0).group()).isEqualTo(SuggestionType.ARGUMENT.name());

        Suggestion sug = argGroup.items().get(0);
        assertThat(sug.value()).contains("<count>");
    }

    @Test
    @DisplayName("ping -c 까지 입력 커서가 -c 에 있으면 option이 제안되나?")
    void ping_dash_c_space_까지_입력_cursor_c에_있다면_option_를_제안하나() {
        String line ="ping -c ";
        Integer cursor = 6;

        LinuxAutocompleteResponse res = getAutocompleteRes(line, cursor);

        SuggestionGroupDTO optGroup = res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.OPTION.name()))
                .findFirst().orElseThrow();

        assertThat(optGroup).isNotNull();

        Suggestion sug = optGroup.items().stream().filter(s -> s.value().equals("-c"))
                .findFirst().orElseThrow();
        assertThat(sug.display()).contains("-c");
    }


    @Test
    @DisplayName("mv a.txt 를 치면 target 을 예상하는가?")
    void mv_와_하나의_소스를_입력하면_타켓이_예상되는가() {
        String line ="mv a.txt ";
        Integer cursor = line.length();

        LinuxAutocompleteResponse res = getAutocompleteRes(line, cursor);

        SynopsisDTO syn = res.synopsis();
        assertThat(syn.position()).isEqualTo(1);
        assertThat(syn.patterns()).isNotEmpty();
        SynopsisPatternDTO pat = syn.patterns().get(0);
        assertThat(pat.progress().filledCount()).isGreaterThanOrEqualTo(1);
        assertThat(pat.progress().requiredRemaining()).isGreaterThanOrEqualTo(1);

        SuggestionGroupDTO tarGroup = res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.TARGET.name()))
                .findFirst().orElseThrow();

        assertThat(tarGroup).isNotNull();
    }

    @Test
    @DisplayName("ls 를 입력하면 -l이 제안되는가?")
    void ls_space_입력하면_l_제안되나() {
        String line ="ls ";
        Integer cursor = line.length();

        LinuxAutocompleteResponse res = getAutocompleteRes(line, cursor);

        SuggestionGroupDTO optGroup = res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.OPTION.name()))
                .findFirst().orElseThrow();

        assertThat(optGroup).isNotNull();
        assertThat(optGroup.items().stream().map(Suggestion::value)).contains("-l");
        assertThat(optGroup.items().stream().map(Suggestion::display)).contains("-l");
    }

    @Test
    @DisplayName("tar 시놉시스에 반복 가능한 option 토큰이 존재하나?")
    void tar_synopsis_옵션_반복_나오나() {
        String line ="tar ";
        Integer cursor = line.length();

        LinuxAutocompleteResponse res = getAutocompleteRes(line, cursor);

        assertThat(res.synopsis().patterns()).isNotEmpty();
        SynopsisPatternDTO p0 = res.synopsis().patterns().get(0);
        assertThat(p0.tokens()).isNotEmpty();
        TokenChipDTO t0 = p0.tokens().get(0);

        assertThat(t0.type().name()).isEqualTo("OPTION");
        assertThat(t0.optional()).isTrue();
        assertThat(t0.repeat()).isTrue();
    }

    @Test
    @DisplayName("'tar -' 입력 시 -f 제안 display에 <file> 붙는가?")
    void tar_dash_입력시_f_제안_file_placeholder() {
        String line = "tar -";
        Integer cursor = line.length();

        LinuxAutocompleteResponse res = getAutocompleteRes(line, cursor);

        SuggestionGroupDTO opt = res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.OPTION.name()))
                .findFirst().orElseThrow();

        Suggestion sug = opt.items().stream()
                .filter(s -> s.value().equals("-f"))
                .findFirst().orElseThrow();

        assertThat(sug.display()).isEqualTo("-f <file>");
        assertThat((sug.desc())).isEqualTo("파일 지정");

    }

    @Test
    @DisplayName("ping -c - : 하이픈 시작 인자여도 argRequired true 면 ARGUMENT가 최우선으로 제안되는가?")
    void ping_dash_c_dash_까지_입력_ARGUMENT_제안() {
        String line = "ping -c -";
        Integer cursor = line.length();

        LinuxAutocompleteResponse res = getAutocompleteRes(line, cursor);

        SuggestionGroupDTO argGroup = res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.ARGUMENT.name()))
                .findFirst().orElseThrow();

        assertThat(argGroup.items().get(0).value()).contains("<count>");

        res.suggestions().groups().stream()
                .filter(g -> g.group().equals(SuggestionType.OPTION.name()))
                .findFirst()
                .ifPresent(opt -> {
                    Suggestion s = opt.items().get(0);
                    assertThat(line.substring(s.replaceStart(), s.replaceEnd())).isEqualTo("-");
                });
    }

// =========================== 샘플 데이터 넣기 ================================================ //

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
        return LinuxCommand.createForManual(
                "네트워크", "ping", null,
                "네트워크 상태 확인",
                syn,
                List.of("HOST"),
                List.of("ping -c 3 google.com", "ping -i 0.5 8.8.8.8"),
                false, false,
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
        return LinuxCommand.createForManual(
                "파일", "mv", null,"파일/디렉터리 이동 또는 이름 변경",
                syn,
                List.of("SOURCE","DEST"),
                List.of("mv a.txt b.txt","mv *.log /var/logs/"),
                false, false,
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
        return LinuxCommand.createForManual(
                "파일", "ls", null, "디렉터리 내용 나열",
                syn,
                List.of("PATH"),
                List.of("ls -l /var/log"),
                false, false,
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
        return LinuxCommand.createForManual(
                "압축", "tar", null, "테이프 아카이브 유틸리티",
                syn,
                List.of("PATH"),
                List.of("tar -xvf file.tar", "tar -czf out.tar.gz dir/"),
                false, false,
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
        return LinuxCommand.createForManual(
                "텍스트", "grep", null, "패턴 검색",
                syn,
                List.of("PATTERN","PATH"),
                List.of("grep -i error app.log"),
                false, false,
                Map.of("-i", new Option(null, false, null, null, "대소문자 무시"),
                        "-E", new Option(null, false, null, null, "확장 정규식")),
                List.of("search","regex")
        );
    }
    private static TokenDescriptor td(TokenType t, boolean repeat, boolean optional, String desc) {
        return TokenDescriptor.of(t, repeat, optional, desc);
    }

    private LinuxAutocompleteResponse getAutocompleteRes(String line, Integer cursor) {
        CommandAutocompleteRequest req = new CommandAutocompleteRequest(line, cursor);

        return service.autocompleteCommand(req);
    }

}
