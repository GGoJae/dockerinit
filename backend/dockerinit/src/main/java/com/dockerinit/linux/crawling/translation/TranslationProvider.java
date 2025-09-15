package com.dockerinit.linux.crawling.translation;

import java.util.List;
import java.util.Locale;

public interface TranslationProvider {

    String translate(String text, Locale from, Locale to);

    List<String> translateBatch(List<String> texts, Locale from, Locale to);
}
