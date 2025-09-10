package com.dockerinit.config;

import com.dockerinit.features.application.dockercompose.dto.spec.CategoryDTO;
import com.dockerinit.features.application.preset.dto.spec.PresetKindDTO;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean("composePresetListKeyGen")
    public KeyGenerator composePresetListKeyGen() {
        return (target, method, params) -> {
            CategoryDTO ct = (CategoryDTO) params[0];
            @SuppressWarnings("unchecked")
            Set<String> rawTags = (Set<String>) params[1];
            Pageable pageable = (Pageable) params[2];

            Set<String> normTags = (rawTags == null) ? Set.of() :
                    rawTags.stream().filter(Objects::nonNull)
                            .map(s -> s.trim().toLowerCase(Locale.ROOT))
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toCollection(TreeSet::new));

            return String.join("|",
                    ct == null ? "-" : ct.name(),
                    normTags.isEmpty() ? "-" : String.join(",", normTags),
                    String.valueOf(pageable.getPageNumber()),
                    String.valueOf(pageable.getPageSize()),
                    String.valueOf(pageable.getSort()));
        };
    }

    @Bean("presetListKeyGen")
    public KeyGenerator presetListKeyGen() {
        return (target, method, params) -> {
            PresetKindDTO kind = (PresetKindDTO) params[0];
            @SuppressWarnings("unchecked")
            Set<String> rawTags = (Set<String>) params[1];
            Pageable pageable = (Pageable) params[2];

            Set<String> normTags = (rawTags == null) ? Set.of() :
                    rawTags.stream()
                            .filter(Objects::nonNull)
                            .map(s -> s.trim().toLowerCase(Locale.ROOT))
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toCollection(TreeSet::new));

            return String.join("|",
                    (kind == null ? "-" : kind.name()),
                    normTags.isEmpty() ? "-" : String.join(",", normTags),
                    String.valueOf(pageable.getPageNumber()),
                    String.valueOf(pageable.getPageSize()),
                    String.valueOf(pageable.getSort()));
        };
    }

}
