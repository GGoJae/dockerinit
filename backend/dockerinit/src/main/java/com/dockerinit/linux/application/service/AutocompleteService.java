package com.dockerinit.linux.application.service;

import com.dockerinit.linux.application.autocomplete.model.CommandView;
import com.dockerinit.linux.application.autocomplete.model.ParseResult;
import com.dockerinit.linux.application.autocomplete.strategy.autocompleteStrategies.AutocompleteStrategy;
import com.dockerinit.linux.application.shared.tokenizer.ShellTokenizer;
import com.dockerinit.linux.application.shared.model.ModuleType;
import com.dockerinit.linux.application.shared.model.ModuleTypeMapper;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.dto.request.CommandAutocompleteRequest;
import com.dockerinit.linux.dto.response.LinuxAutocompleteResponse;
import com.dockerinit.linux.dto.response.common.SuggestionType;
import com.dockerinit.linux.dto.response.autocompleteV1.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.dockerinit.global.constants.AppInfo.CURRENT_AUTOCOMPLETE_VERSION;
import static com.dockerinit.global.constants.AutoCompleteSuggest.MAX_SUGGEST;

@Service
public class AutocompleteService {

    private final RedisTemplate<String, String> redis;
    private final Map<ModuleType, AutocompleteStrategy> strategyMap;

    public AutocompleteService(RedisTemplate<String, String> redis, List<AutocompleteStrategy> strategies) {
        this.redis = redis;
        this.strategyMap = strategies.stream().collect(Collectors.toMap(
                AutocompleteStrategy::moduleType,
                s -> s,
                (a, b) -> a,
                () -> new EnumMap<>(ModuleType.class)
        ));
    }

    public LinuxAutocompleteResponse autocompleteCommand(CommandAutocompleteRequest req) {
        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(req.line());
        String base = tokens.isEmpty() ? "" : tokens.get(0).text().toLowerCase(Locale.ROOT);

        ModuleType moduleType = ModuleTypeMapper.fromCommand(base);

        AutocompleteStrategy strategy = strategyMap.get(moduleType);

        ParseResult parsed = strategy.parse(req.line(), req.cursor(), tokens);

        List<Suggestion> suggest = strategy.suggest(parsed, tokens);

        List<SuggestionGroupDTO> groups = groupSuggestions(suggest);

        CommandView cmd = parsed.command();
        BaseInfo baseInfo = (cmd != null) ?
                new BaseInfo(cmd.command(), cmd.category(), cmd.description(), cmd.verified(), cmd.tags(), false)
                : new BaseInfo(parsed.baseCommand(), "", "", false, List.of(), true);

        CursorInfo cursorInfo = new CursorInfo(
                req.line(), req.cursor(), parsed.currentToken(), parsed.tokenIndex(), parsed.prevFlag()
        );

        List<ExpectedTokenDTO> expected = parsed.expected().stream()
                .sorted()
                .map(exp -> new ExpectedTokenDTO(exp.type(), exp.priority(), exp.confidence(), exp.meta()))
                .toList();

        SynopsisDTO synopsisDTO = buildSynopsisDTO(cmd, parsed.position());

        Map<String, OptionDTO> options = (cmd != null && !cmd.options().isEmpty()) ?
                cmd.options().entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e ->
                        {
                            Option value = e.getValue();
                            return new OptionDTO(value.argName(), value.argRequired(), value.typeHint(), value.defaultValue(), value.description());
                        }
                )) : Map.of();

        List<String> examples = (cmd != null) ? cmd.examples() : List.of();

        SuggestionsBlockDTO suggestions = new SuggestionsBlockDTO(groups, MAX_SUGGEST);

        return new LinuxAutocompleteResponse(
                CURRENT_AUTOCOMPLETE_VERSION,
                baseInfo,
                cursorInfo,
                expected,
                synopsisDTO,
                options,
                examples,
                suggestions
        );
    }

    private List<SuggestionGroupDTO> groupSuggestions(List<Suggestion> items) {
        var byType = items.stream().collect(Collectors.groupingBy(Suggestion::type, LinkedHashMap::new, Collectors.toList()));
        var out = new ArrayList<SuggestionGroupDTO>();
        // 보여줄 순서 정의(원하면 조정)
        List<SuggestionType> order = List.of(
                SuggestionType.ARGUMENT,
                SuggestionType.OPTION,
                SuggestionType.TARGET,
                SuggestionType.COMMAND
        );
        for (var t : order) {
            var list = byType.getOrDefault(t, List.of());
            if (!list.isEmpty()) out.add(new SuggestionGroupDTO(t.name(), list));
        }
        // 혹시 남은 타입이 있으면 뒤에
        byType.forEach((t, list) -> {
            if (order.contains(t) || list.isEmpty()) return;
            out.add(new SuggestionGroupDTO(t.name(), list));
        });
        return out;
    }

    private SynopsisDTO buildSynopsisDTO(CommandView cmd, int position) {
        if (cmd == null || cmd.synopsis() == null) {
            return new SynopsisDTO(null, position, List.of());
        }
        var patterns = new ArrayList<SynopsisPatternDTO>();
        int id = 0;
        for (var p : cmd.synopsis().patterns()) {
            var chips = p.tokens().stream()
                    .map(td -> new TokenChipDTO(td.tokenType(), td.optional(), td.repeat(), td.description()))
                    .toList();
            // 간단한 progress 계산(필요하면 고도화)
            int filled = Math.min(position, p.size());
            int requiredRemaining = (int) p.tokens().stream().skip(filled).filter(td -> !td.optional()).count();
            int optionalRemaining = (int) p.tokens().stream().skip(filled).filter(td -> td.optional()).count();
            var progress = new SynopsisProgressDTO(filled, requiredRemaining, optionalRemaining);
            patterns.add(new SynopsisPatternDTO(id++, "기본", chips, progress));
        }
        return new SynopsisDTO(null, position, patterns);
    }
}
