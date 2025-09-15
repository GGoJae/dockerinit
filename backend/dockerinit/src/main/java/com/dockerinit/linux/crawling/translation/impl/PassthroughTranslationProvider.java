package com.dockerinit.linux.crawling.translation.impl;

import com.dockerinit.linux.crawling.translation.TranslationProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class PassthroughTranslationProvider implements TranslationProvider {
    @Override
    public String translate(String text, Locale from, Locale to) {
        return text;
    }

    @Override
    public List<String> translateBatch(List<String> texts, Locale from, Locale to) {
        return texts;
    }
}
