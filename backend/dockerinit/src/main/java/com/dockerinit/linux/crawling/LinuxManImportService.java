package com.dockerinit.linux.crawling;

import com.dockerinit.global.support.hash.Hashes;
import com.dockerinit.linux.crawling.translation.TranslationProvider;
import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.domain.model.ManMeta;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.domain.syntax.Synopsis;
import com.dockerinit.linux.dto.response.batchResult.BatchResult;
import com.dockerinit.linux.dto.response.doc.LinuxCommandDocResponse;
import com.dockerinit.linux.infrastructure.repository.LinuxCommandRepository;
import com.dockerinit.linux.mapper.LinuxCommandDocMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LinuxManImportService {

    private final ManFetchService fetcher;
    private final ManParseService parser;
    private final TranslationProvider translator;
    private final LinuxCommandRepository repository;

    public LinuxCommandDocResponse importKo(String command) {
        ManFetchService.ManRaw raw = fetcher.fetch(command);
        String hash = Hashes.sha256(raw.text());

        String norm = command.trim().toLowerCase(Locale.ROOT);
        LinuxCommand existing = repository.findByCommandNorm(norm).orElse(null);
        if (existing != null && existing.getManMeta() != null
                && hash.equals(existing.getManMeta().getSourceHash())) {
            return LinuxCommandDocMapper.toDoc(existing);
        }

        ManParseService.ParsedEn en = parser.parse(command, raw.text());
        String koDescription = translateMaybe(en.description());
        String koSynopsis = translateMaybe(en.synopsisAsText());

        Map<String, Option> optionMap = new LinkedHashMap<>();
        for (var po : en.options()) {
            String dko = translateMaybe(po.description());
            String key = (po.primaryFlag() != null) ? po.primaryFlag() : (po.flags().isEmpty() ? "unknown" : po.flags().get(0));
            optionMap.put(key, new Option(
                    po.argName(), po.argRequired(), po.typeHint(), po.defaultValue(), dko
            ));
        }

        LinuxCommand next = (existing == null)
                ? LinuxCommand.createForCrawling(
                null,
                command,
                List.of(),
                koDescription,
                (koSynopsis == null ? null : Synopsis.fromText(koSynopsis)),
                List.of(),
                List.of(),
                false,
                optionMap,
                List.of(),
                null
        )
                : existing.updater()
                .description(koDescription)
                .synopsis(koSynopsis == null ? null : Synopsis.fromText(koSynopsis))
                .options(optionMap)
                .verified(false)
                .update();

        ManMeta meta = ManMeta.of(raw.section(), raw.distro(), raw.source(), hash, Instant.now());

        next = next.updater().manMeta(meta).update();

        LinuxCommand saved = repository.save(next);

        return LinuxCommandDocMapper.toDoc(saved);
    }

    public BatchResult importKoBatch(List<String> commands) {
        var collector = BatchResult.collector();
        for (int i = 0; i < commands.size(); i++) {
            String c = commands.get(i);
            try {
                importKo(c);
                collector.success(i, c);
            } catch (Exception e) {
                collector.fail(i, c, e);
            }
        }
        return collector.completed();
    }

    private String translateMaybe(String s) {
        if (s == null || s.isBlank()) return s;
        return translator.translate(s, Locale.ENGLISH, Locale.KOREAN);
    }
}
