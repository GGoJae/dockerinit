package com.dockerinit.service;

import com.dockerinit.linux.domain.LinuxCommand;
import com.dockerinit.linux.dto.LinuxAutoCompleteRequest;
import com.dockerinit.linux.dto.LinuxAutoCompleteResponse;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.service.LinuxCommandService;
import com.dockerinit.linux.model.AcPhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class LinuxCommandServiceIT {

    @Autowired
    LinuxCommandService service;
    @Autowired LinuxCommandRepository repo;
    @Autowired StringRedisTemplate redisTemplate;

    @BeforeEach
    void seed() {
        repo.deleteAll();

        repo.saveAll(List.of(
                cmd("ls",  Map.of("-l", opt("format","false"),
                        "-a", opt("all","false"))),
                cmd("ping",Map.of("-c", opt("count","true"),
                        "-i", opt("interval","false"))),
                cmd("grep",Map.of("-i", opt("ignore-case","false"))),
                cmd("cat",  Map.of()),
                cmd("tar",  Map.of("-xzf", opt("archive","true")))
        ));

        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    /* ---------- 실제 테스트 ---------- */

    @Test
    @DisplayName("리눅스 자동완성 레디스에 캐시한 탑 매치 잘나와?")
    void commandPhase_returnsTopMatches() {
        LinuxAutoCompleteResponse res =
                service.autocompleteCommand(new LinuxAutoCompleteRequest("p", null));

        assertThat(res.phase()).isEqualTo(AcPhase.COMMAND);
        assertThat(res.suggestions())
                .extracting(s -> s.value())
                .containsExactly("ping");
    }

    @Test
    @DisplayName("리눅스 자동완성 ping -까지 쳤을때 옵션들 잘 나와?")
    void optionPhase_returnsFlagList() {
        LinuxAutoCompleteResponse res =
                service.autocompleteCommand(new LinuxAutoCompleteRequest("ping -", null));

        System.out.println("res = " + res);

        assertThat(res.phase()).isEqualTo(AcPhase.OPTION);
        assertThat(res.suggestions())
                .extracting(s -> s.value())
                .containsExactlyInAnyOrder("-c", "-i");
    }

    @Test
    @DisplayName("리눅스 명령어에 옵션이 인수가 필수일떄 플레이스 홀더 잘나와?")
    void argumentPhase_returnsPlaceholder() {
        LinuxAutoCompleteResponse res =
                service.autocompleteCommand(new LinuxAutoCompleteRequest("ping -c ", null));

        assertThat(res.phase()).isEqualTo(AcPhase.ARGUMENT);
        assertThat(res.suggestions().get(0).value()).isEqualTo("<count>");
    }

    @Test
    @DisplayName("이상한 명령어를 치면 빈값 나오나요?")
    void wrongCommand_throwException() {
        LinuxAutoCompleteResponse res = service.autocompleteCommand(new LinuxAutoCompleteRequest("wrongString", null));
        assertThat(res.suggestions()).isEmpty();
    }


    private static LinuxCommand cmd(String command, Map<String, LinuxCommand.OptionInfo> opts) {
        return new LinuxCommand(
                "기본", command, command + " 설명", command + " [옵션]",
                null, false, !opts.isEmpty(), opts, List.of(command));
    }
    private static LinuxCommand.OptionInfo opt(String arg, String required) {
        return new LinuxCommand.OptionInfo(arg, Boolean.parseBoolean(required),
                null, null, arg + " 설명");
    }
}
