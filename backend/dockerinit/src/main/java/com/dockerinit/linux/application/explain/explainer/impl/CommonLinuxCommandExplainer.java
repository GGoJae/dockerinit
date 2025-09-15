package com.dockerinit.linux.application.explain.explainer.impl;

import com.dockerinit.linux.application.autocomplete.model.CommandView;
import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
import com.dockerinit.linux.application.explain.explainer.CommandExplainer;
import com.dockerinit.linux.application.explain.model.Invocation;
import com.dockerinit.linux.application.explain.util.InvocationFactory;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.dto.response.explainV1.ExplainResponse;
import com.dockerinit.linux.dto.response.explainV1.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.dockerinit.global.constants.AppInfo.CURRENT_EXPLAIN_VERSION;

@Component
@RequiredArgsConstructor
public class CommonLinuxCommandExplainer implements CommandExplainer {

    @Override
    public ExplainResponse explain(ParseResult result, Locale locale) {
        CommandView view = result.command();
        if (view == null) {
            return new ExplainResponse(
                    CURRENT_EXPLAIN_VERSION,
                    Header.empty(),
                    new Details(List.of(), List.of(), List.of(
                            new Note(NoteLevel.INFO, "알 수 없는 명령입니다.")
                    )),
                    List.of(), false
            );
        }

        Invocation inv = InvocationFactory.from(result, ShellTokenizer.tokenize(result.line()), locale);

        String summary = (view.description() == null) ? "설명 없음" : view.description();
        List<String> tags = (view.tags() == null) ? List.of() : view.tags();

        Header header = new Header(view.command(), summary, tags);

        ArrayList<OptionUse> optionUses = new ArrayList<>();
        Map<String, Option> meta = (view.options() == null) ? Map.<String, Option>of() : view.options();
        inv.opts().forEach((flag, val) -> {
            Option o = meta.get(flag);
            String argName = (o == null) ? null : o.argName();
            boolean argRequired = (o != null) && o.argRequired();
            String description = (o == null) ? "" : (o.description() == null) ? "" : o.description();
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

        List<ExampleItem> examples = (view.examples() == null) ? List.of() : view.examples().stream().map(
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
        if (v == null) return OperandType.RAW;
        if (v.contains("/") || v.startsWith("./") || v.startsWith("../")) return OperandType.PATH;
        if (v.endsWith("/")) return OperandType.DIRECTORY;
        if (v.contains(".")) return OperandType.HOST;   // TODO Host 조건 더 생각해보기....
        return OperandType.RAW;
    }
}
