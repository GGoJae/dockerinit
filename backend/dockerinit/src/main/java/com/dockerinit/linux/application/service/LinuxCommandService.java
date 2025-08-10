package com.dockerinit.linux.application.service;

import com.dockerinit.global.exception.NotFoundCustomException;
import com.dockerinit.linux.application.autoComplete.model.ParseResult;
import com.dockerinit.linux.application.autoComplete.strategies.autoCompleteStrategies.AutoCompleteCommandStrategy;
import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.dto.request.LinuxAutoCompleteRequest;
import com.dockerinit.linux.dto.request.LinuxCommandGenerateRequest;
import com.dockerinit.linux.dto.request.LinuxCommandRequest;
import com.dockerinit.linux.dto.response.LinuxAutoCompleteResponse;
import com.dockerinit.linux.dto.response.LinuxCommandResponse;
import com.dockerinit.linux.dto.response.v2.*;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.util.ShellTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.dockerinit.global.constants.ErrorMessage.LINUX_COMMAND_ID_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LinuxCommandService {

    private final LinuxCommandRepository repository;
    private final List<AutoCompleteCommandStrategy> strategies;


    /* ─────────────────────────────── CRUD API ─────────────────────────────── */

    public LinuxCommandResponse getById(String id) {
        return repository.findById(id)
                .map(LinuxCommandResponse::of)
                .orElseThrow(() -> new NotFoundCustomException(LINUX_COMMAND_ID_NOT_FOUND, id));
    }

    public List<LinuxCommandResponse> getAll() {
        return repository.findAll().stream().map(LinuxCommandResponse::of).toList();
    }

    public LinuxCommandResponse createCommand(LinuxCommandRequest req) {
        LinuxCommand saved = repository.save(req.toEntity());
        return LinuxCommandResponse.of(saved);
    }

    /* ────────────────────────────── 리눅스 커맨드 분석 API ───────────────────────────── */

    public LinuxCommandResponse generate(LinuxCommandGenerateRequest request) {
        return null; // TODO 명령어 분석 하는 로직 작성
    }

    /* ────────────────────────────── 자동완성 API ───────────────────────────── */

    public LinuxAutoCompleteResponse autocompleteCommand(LinuxAutoCompleteRequest req) {
        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(req.line());
        String baseCmd = tokens.isEmpty() ? "" : tokens.get(0).text();

        AutoCompleteCommandStrategy commandStrategy = strategies.stream()
                .filter(s -> s.supports(baseCmd))
                .findFirst()
                .orElseThrow();     // TODO 전략 못 찾았을때는 추후 fallback 비슷한 명령어 추천해주는 전략이면 좋을듯

//        ParseCtx ctx = commandStrategy.parse(req.line(), req.cursor(), tokens);
//        List<Suggestion> suggest = commandStrategy.suggest(ctx);
//
//        return new LinuxAutoCompleteResponse(ctx.phase(), ctx.baseCommand(), ctx.currentToken(), suggest);

        return null; // TODO V2 로 리턴시키기
    }

 public LinuxAutoCompleteResponseV2 autocompleteCommandV2(LinuxAutoCompleteRequest req) {
        List<ShellTokenizer.Token> tokens = ShellTokenizer.tokenize(req.line());
        String baseCmd = tokens.isEmpty() ? "" : tokens.get(0).text();

        AutoCompleteCommandStrategy strategy = strategies.stream()
                .filter(s -> s.supports(baseCmd))
                .findFirst()
                .orElseThrow();     // TODO 전략 못 찾았을때는 추후 fallback 비슷한 명령어 추천해주는 전략이면 좋을듯

     ParseResult parsed = strategy.parse(req.line(), req.cursor(), tokens);

     List<SuggestionV2> suggest = strategy.suggest(parsed);

     List<SuggestionGroupDTO> groups = groupSuggestions(suggest);

     LinuxCommand command = parsed.command();
     Optional<LinuxCommand> cmdOpt = Optional.ofNullable(command);

     BaseInfo baseInfo = cmdOpt
             .map(cmd -> new BaseInfo(cmd.getCommand(),
                     cmd.getCategory(), cmd.getDescription(), cmd.isVerified(), cmd.getTags(), false))
             .orElseGet(() -> new BaseInfo(parsed.baseCommand(),
                     "", "", false, List.of(), true));

     CursorInfo cursorInfo = new CursorInfo(
             req.line(), req.cursor(), parsed.currentToken(), parsed.tokenIndex(), parsed.prevFlag()
     );

     List<ExpectedTokenDTO> expected = parsed.expected().stream()
             .sorted()
             .map(exp -> new ExpectedTokenDTO(exp.type(), exp.priority(), exp.confidence(), exp.meta()))
             .toList();

     SynopsisDTO synopsisDTO = buildSynopsisDTO(command, parsed.position());

     Map<String, OptionDTO> options = cmdOpt.map(cmd -> cmd.getOptions())
             .map(opt -> opt.entrySet().stream().collect(Collectors.toMap(
                     Map.Entry::getKey,
                     e ->
                             new OptionDTO(e.getValue().argName(), e.getValue().argRequired(),
                                     e.getValue().typeHint(), e.getValue().defaultValue(), e.getValue().description())
             ))).orElseGet(() -> Map.of());

     List<String> examples = cmdOpt.map(cmd -> cmd.getExamples())
             .orElseGet(() -> List.of());

     SuggestionsBlockDTO suggestions = new SuggestionsBlockDTO(groups, 15);

     return new LinuxAutoCompleteResponseV2(
             "autocomplete.v2",
             baseInfo,
             cursorInfo,
             expected,
             synopsisDTO,
             options,
             examples,
             suggestions
     );
    }

    private List<SuggestionGroupDTO> groupSuggestions(List<SuggestionV2> items) {
        var byType = items.stream().collect(Collectors.groupingBy(SuggestionV2::type, LinkedHashMap::new, Collectors.toList()));
        var out = new ArrayList<SuggestionGroupDTO>();
        // 보여줄 순서 정의(원하면 조정)
        List<com.dockerinit.linux.dto.response.SuggestionType> order = List.of(
                com.dockerinit.linux.dto.response.SuggestionType.ARGUMENT,
                com.dockerinit.linux.dto.response.SuggestionType.OPTION,
                com.dockerinit.linux.dto.response.SuggestionType.TARGET,
                com.dockerinit.linux.dto.response.SuggestionType.COMMAND
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

    private SynopsisDTO buildSynopsisDTO(LinuxCommand cmd, int position) {
        if (cmd == null || cmd.getSynopsis() == null) {
            return new SynopsisDTO(null, position, List.of());
        }
        var patterns = new ArrayList<SynopsisPatternDTO>();
        int id = 0;
        for (var p : cmd.getSynopsis().patterns()) {
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
