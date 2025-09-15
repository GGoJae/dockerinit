package com.dockerinit.linux.mapper;

import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.domain.model.ManMeta;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.domain.syntax.Synopsis;
import com.dockerinit.linux.domain.syntax.TokenDescriptor;
import com.dockerinit.linux.dto.response.doc.LinuxCommandDocResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LinuxCommandDocMapper {
    public static LinuxCommandDocResponse toDoc(LinuxCommand c) {
        if (c == null) return null;

        return new LinuxCommandDocResponse(
                c.getCommand(),
                c.getCategory(),
                safeList(c.getAliases()),
                safeList(c.getTags()),
                c.getDescription(),
                SynopsisRenderer.renderOneLine(c.getSynopsis()),
                toSynopsisDTO(c.getSynopsis()),
                c.isVerified(),
                c.isOptionRequired(),
                safeList(c.getArguments()),
                safeList(c.getExamples()),
                toOptionDTOs(c.getOptions()),
                c.getSearchCount(),
                toMeta(c.getManMeta())
        );
    }

    private static LinuxCommandDocResponse.SourceMeta toMeta(ManMeta m) {
        if (m == null) return null;
        return new LinuxCommandDocResponse.SourceMeta(
                m.getSection(), m.getDistro(), m.getSource(), m.getSourceHash(), m.getImportedAt()
        );
    }

    private static LinuxCommandDocResponse.SynopsisDTO toSynopsisDTO(Synopsis syn) {
        if (syn == null || syn.patterns() == null || syn.patterns().isEmpty()) return null;
        List<LinuxCommandDocResponse.SynopsisDTO.PatternDTO> patterns = new ArrayList<>();
        for (var p : syn.patterns()) {
            List<LinuxCommandDocResponse.SynopsisDTO.TokenDTO> tokens = new ArrayList<>();
            for (TokenDescriptor td : p.tokens()) {
                tokens.add(new LinuxCommandDocResponse.SynopsisDTO.TokenDTO(
                        td.tokenType().name(),
                        td.literal(),
                        td.optional(),
                        td.repeat(),
                        td.description()
                ));
            }
            patterns.add(new LinuxCommandDocResponse.SynopsisDTO.PatternDTO(tokens));
        }
        return new LinuxCommandDocResponse.SynopsisDTO(patterns);
    }

    private static List<LinuxCommandDocResponse.OptionDTO> toOptionDTOs(Map<String, Option> options) {
        if (options == null || options.isEmpty()) return List.of();
        // Map key(대표 플래그) 기준 정렬
        return options.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .map(e -> {
                    Option o = e.getValue();
                    return new LinuxCommandDocResponse.OptionDTO(
                            e.getKey(),
                            o.argName(),
                            o.argRequired(),
                            o.typeHint(),
                            o.defaultValue(),
                            o.description()
                    );
                })
                .toList();
    }

    private static <T> List<T> safeList(List<T> in) {
        return (in == null) ? List.of() : List.copyOf(in);
    }
}
