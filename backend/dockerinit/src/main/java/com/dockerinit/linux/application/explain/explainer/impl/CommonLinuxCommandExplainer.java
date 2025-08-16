package com.dockerinit.linux.application.explain.explainer.impl;

import com.dockerinit.linux.application.autocomplete.model.CommandView;
import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
import com.dockerinit.linux.application.explain.explainer.CommandExplainer;
import com.dockerinit.linux.application.explain.model.Invocation;
import com.dockerinit.linux.application.explain.util.InvocationFactory;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.dto.response.ExplainResponse;
import com.dockerinit.linux.dto.response.explainV1.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.dockerinit.global.constants.AppInfo.CURRENT_EXPLAIN_VERSION;
import static java.util.List.of;

@Component
@RequiredArgsConstructor
public class CommonLinuxCommandExplainer implements CommandExplainer {

    @Override
    public ExplainResponse explain(ParseResult result, Locale locale) {
        CommandView view = result.command();
        if (Objects.isNull(view)) {
            return new ExplainResponse(
                    CURRENT_EXPLAIN_VERSION,
                    new Header("", "해당 명령를 찾을 수 없습니다.", of()),
                    new Details(of(), of(), of(
                            new Note(NoteLevel.INFO, "알 수 없는 명령입니다.")
                    )),
                    of(), false
            );
        }

        Invocation inv = InvocationFactory.from(result, ShellTokenizer.tokenize(result.line()), locale);

        String summary = Objects.isNull(view.description()) ? "설명 없음" : view.description();
        List<String> tags = Objects.isNull(view.tags()) ? of() : view.tags();

        Header header = new Header(view.command(), summary, tags);

        ArrayList<OptionUse> optionUses = new ArrayList<>();
        Map<String, Option> meta = Objects.isNull(view.options()) ? Map.<String, Option>of() : view.options();
        inv.opts().forEach((flag, val) -> {
            Option o = meta.get(flag);
            String argName = Objects.isNull(o) ? null : o.argName();
            boolean argRequired = Objects.nonNull(o) && o.argRequired();
            String description = Objects.isNull(o) ? "" : Objects.isNull(o.description()) ? "" : o.description();
            optionUses.add(new OptionUse(
                    flag,
                    argName,
                    val,
                    argRequired,
                    description
            ));
        });

        List<Operand> operands = inv.args().stream().map(
                s -> new Operand(s, guessType(s), null)
        ).toList();

        ArrayList<Note> notes = new ArrayList<>();
        if ("rm".equals(view.command()) && inv.opts().containsKey("-")) {
            notes.add(new Note(NoteLevel.DANGER, "주의: 재귀 삭제(-r)는 되돌릴 수 없습니다."));
        }

        Details details = new Details(optionUses, operands, notes);

        List<ExampleItem> examples = Objects.isNull(view.examples()) ? List.of() : view.examples().stream().map(
                e -> new ExampleItem(e, null)
        ).toList();

        return new ExplainResponse(
                CURRENT_EXPLAIN_VERSION,
                header,
                details,
                examples,
                false);
    }

    private OperandType guessType(String v) {
        if (Objects.isNull(v)) return OperandType.RAW;
        if (v.contains("/") || v.startsWith("./") || v.startsWith("../")) return OperandType.PATH;
        if (v.endsWith("/")) return OperandType.DIRECTORY;
        if (v.contains(".")) return OperandType.HOST;   // TODO Host 조건 더 생각해보기....
        return OperandType.RAW;
    }
}
