package com.dockerinit.linux.service.strategy.linuxCommandStrategy.linuxCommandStrategyImpl.commonLinuxCommandStrategy;

import com.dockerinit.linux.application.shared.parser.CommonLinuxCommandParser;
import com.dockerinit.linux.infrastructure.repository.LinuxCommandRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CommonLinuxCommandParserTest {

    @Mock
    LinuxCommandRepository repository;

    @InjectMocks
    CommonLinuxCommandParser parser;
// TODO V2 버전 나오면 새로 작성
//    @Test
//    void 명령어만_입력하면_COMMAND() {
//        String line = "ping";
//        int cursor = line.length();
//        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(line);
//
//        ParseCtx parseCtx = parser.parse(line, cursor, tokens);
//
//        assertThat(parseCtx.phase()).isEqualTo(AcPhase.COMMAND);
//        assertThat(parseCtx.baseCommand()).isEqualTo("ping");
//    }
//
//    @Test
//    void 옵션입력중이면_OPTION() {
//        String line = "ping -c";
//        int cursor = line.length();
//        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(line);
//
//        ParseCtx parseCtx = parser.parse(line, cursor, tokens);
//
//        assertThat(parseCtx.phase()).isEqualTo(AcPhase.OPTION);
//        assertThat(parseCtx.baseCommand()).isEqualTo("ping");
//        assertThat(parseCtx.currentToken()).isEqualTo("-c");
//    }
//
//    @Test
//    void 필수인자_옵션다음_빈칸이면_ARGUMENT() {
//        String line = "ping -c ";
//        int cursor = line.length();
//        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(line);
//
//        given(repository.findByCommand("ping"))
//                .willReturn(Optional.of(new LinuxCommand(
//                        "기본", "ping", "", "", null, null, true,
//                        Map.of("-c", new LinuxCommand.OptionInfo("count", true, null, null, "")
//                        ), List.of("ping")
//                )));
//
//        ParseCtx parseCtx = parser.parse(line, cursor, tokens);
//
//        assertThat(parseCtx.phase()).isEqualTo(AcPhase.ARGUMENT);
//        assertThat(parseCtx.baseCommand()).isEqualTo("ping");
//        assertThat(parseCtx.prevFlag()).isEqualTo("-c");
//    }
//
//    @Test
//    void 필수인자X_옵션다음_빈칸이면_OPTION_OR_ARGUMENT() {
//        String line = "ping -i ";
//        int cursor = line.length();
//        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(line);
//
//        given(repository.findByCommand("ping"))
//                .willReturn(Optional.of(new LinuxCommand(
//                        "기본", "ping", "", "", null, null, true,
//                        Map.of("-i", new LinuxCommand.OptionInfo("interval", false, null, null, "")
//                        ), List.of("ping")
//                )));
//
//        ParseCtx parseCtx = parser.parse(line, cursor, tokens);
//
//        assertThat(parseCtx.phase()).isEqualTo(AcPhase.OPTION_OR_ARGUMENT);
//        assertThat(parseCtx.baseCommand()).isEqualTo("ping");
//        assertThat(parseCtx.prevFlag()).isEqualTo("-i");
//        assertThat(parseCtx.currentToken()).isEqualTo("");
//    }
//
//    @Test
//    void 이상한명령어면_prevFlag_빈문자() {
//        String line = "wrong -x";
//        int cursor = line.length();
//        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(line);
//
//        given(repository.findByCommand("wrong"))
//                .willReturn(Optional.empty());
//
//        ParseCtx parseCtx = parser.parse(line, cursor, tokens);
//
//        assertThat(parseCtx.prevFlag()).isEqualTo("");
//        assertThat(parseCtx.baseCommand()).isEqualTo("wrong");
//        assertThat(parseCtx.currentToken()).isEqualTo("-x");
//        assertThat(parseCtx.phase()).isEqualTo(AcPhase.OPTION);
//    }

}