package com.dockerinit.linux.application.explain.util;

import com.dockerinit.linux.application.autocomplete.model.CommandView;
import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.application.explain.model.Invocation;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
import com.dockerinit.linux.domain.syntax.Option;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

class InvocationFactoryTest {

    private ParseResult mockParseResult(String cmd, CommandView view) {
        ParseResult mock = Mockito.mock(ParseResult.class);
        Mockito.when(mock.baseCommand()).thenReturn(cmd);
        Mockito.when(mock.command()).thenReturn(view);
        return mock;
    }

    private CommandView mockCommandViewWithOptions(Map<String, Option> options) {
        CommandView mock = Mockito.mock(CommandView.class);
        Mockito.when(mock.options()).thenReturn(options);
        return mock;
    }

    @Test
    @DisplayName("long 옵션: --count=3 형태 파싱하나")
    void longOptionWithEqualsEquals() {
        String line = "ping --count=3 example.com";
        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(line);

        HashMap<String, Option> meta = new HashMap<>();
        meta.put("--count", new Option("count", true, "int", null, "count of ping"));

        CommandView view = mockCommandViewWithOptions(meta);
        ParseResult pr = mockParseResult("ping", view);

        Invocation inv = InvocationFactory.from(pr, tokens, Locale.KOREA);

        assertThat(inv.command()).isEqualTo("ping");
        assertThat(inv.opts()).containsEntry("--count", "3");
        assertThat(inv.args()).containsExactly("example.com");
    }

    @Test
    @DisplayName("long 옵션: --count 5 형태에서 다음 토큰을 값으로 소비하나")
    void longOptionConsumesNextToken() {
        String line = "ping --count 5 example.com";
        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(line);

        Map<String, Option> meta = Map.of(
                "--count", new Option("count", true, "int", null, "count of ping")
        );

        CommandView view = mockCommandViewWithOptions(meta);
        ParseResult pr = mockParseResult("ping", view);

        Invocation inv = InvocationFactory.from(pr, tokens, Locale.KOREA);

        assertThat(inv.opts()).containsEntry("--count", "5");
        assertThat(inv.args()).containsExactly("example.com");
    }

    @Test
    @DisplayName("단축 옵션 클러스터: -xvf 에서 -f가 인자를 소비하면 다음 토큰을 값으로 사용")
    void shortClusterWithFinalArgOptionConsumesNext() {
        String line = "tar -xvf archive.tar";
        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(line);

        Map<String, Option> meta = new LinkedHashMap<>();
        meta.put("-x", new Option(null, false, null, null, "extract"));
        meta.put("-v", new Option(null, false, null, null, "verbose"));
        meta.put("-f", new Option("file", true, "path", null, "archive file"));

        CommandView view = mockCommandViewWithOptions(meta);
        ParseResult pr = mockParseResult("tar", view);

        Invocation inv = InvocationFactory.from(pr, tokens, Locale.KOREA);

        assertThat(inv.opts())
                .containsEntry("-x", "")
                .containsEntry("-v", "")
                .containsEntry("-f", "archive.tar");
        assertThat(inv.args()).isEmpty();
    }

    @Test
    @DisplayName("단축 옵션 붙여쓰기: -n5 (argRequired=true이면 같은 토큰의 나머지를 값으로)")
    void shortOptionAttachedValue() {
        String line = "cmd -n5 target";
        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(line);

        Map<String, Option> meta = Map.of(
                "-n", new Option("number", true, "int", null, "number value")
        );
        CommandView view = mockCommandViewWithOptions(meta);
        ParseResult pr = mockParseResult("cmd", view);

        Invocation inv = InvocationFactory.from(pr, tokens, Locale.KOREA);

        assertThat(inv.opts()).containsEntry("-n", "5");
        assertThat(inv.args()).containsExactly("target");
    }

    @Test
    @DisplayName("`--` 이후는 전부 오퍼랜드로 취급 (현재 구현 버그 검출 케이스)")
    void endOfOptionsMarkerAllFollowingAreOperands() {
        String line = "grep -n -- -foo bar";
        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(line);

        Map<String, Option> meta = Map.of(
                "-n", new Option(null, false, null, null, "line numbers")
        );
        CommandView view = mockCommandViewWithOptions(meta);
        ParseResult pr = mockParseResult("grep", view);

        Invocation inv = InvocationFactory.from(pr, tokens, Locale.KOREA);

        assertThat(inv.opts()).containsEntry("-n", "");
        assertThat(inv.args()).containsExactly("-foo", "bar"); // <-- 여기서 실패하면 버그 존재
    }
}