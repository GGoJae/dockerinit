package com.dockerinit.linux.application.autocomplete.suggester;

import com.dockerinit.linux.application.autocomplete.model.ExpectedToken;
import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.application.autocomplete.model.SuggestionMapping;
import com.dockerinit.linux.application.autocomplete.replace.Replace;
import com.dockerinit.linux.application.autocomplete.suggester.types.TypeSuggester;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
import com.dockerinit.linux.domain.syntax.TokenType;
import com.dockerinit.linux.dto.response.common.SuggestionType;
import com.dockerinit.linux.dto.response.autocompleteV1.Suggestion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.dockerinit.global.constants.AutoCompleteSuggest.MAX_SUGGEST;

@Slf4j
@Component
public class CommonLinuxCommandSuggester implements AutocompleteSuggester {

    private final Map<SuggestionType, TypeSuggester> registry;

    public CommonLinuxCommandSuggester(List<TypeSuggester> delegates) {
        this.registry = delegates.stream().collect(Collectors.toMap(
                d -> d.type(),
                d -> d,
                (a, b) -> a,
                () -> new EnumMap<>(SuggestionType.class)
        ));
    }

    @Override
    public List<Suggestion> suggest(ParseResult result, List<ShellTokenizer.Token> tokens) {
        Objects.requireNonNull(result, "Parse Result is Non null");

        List<ExpectedToken> slots = result.expected().stream().sorted().toList();
        LinkedHashMap<DedupKey, Suggestion> out = new LinkedHashMap<>();

        if (slots.isEmpty() && result.command() == null && result.tokenIndex() <= 0) {
            log.debug("슬롯이 비어있어서 COMMAND 를 주입했습니다. slot for prefix='{}'", result.currentToken());
            slots = List.of(new ExpectedToken(TokenType.COMMAND, 0, 1.0, Map.of()));
        }

        Replace.Range range = Replace.forCurrentToken(result.cursor(), tokens);

        outer:
        for (ExpectedToken slot : slots) {
            SuggestionType st = SuggestionMapping.fromTokenType(slot.type());
            TypeSuggester typeSuggester = registry.get(st);

            if (typeSuggester == null) continue;

            List<Suggestion> collect = typeSuggester.collect(result, tokens, slot, range, MAX_SUGGEST - out.size());
            for (Suggestion s : collect) {
                out.putIfAbsent(new DedupKey(s.type(), s.value()), s);
                if (out.size() >= MAX_SUGGEST) {
                    break outer;
                }
            }
        }
        return new ArrayList<>(out.values());
    }

    private record DedupKey(SuggestionType type, String value) {
    }
}
