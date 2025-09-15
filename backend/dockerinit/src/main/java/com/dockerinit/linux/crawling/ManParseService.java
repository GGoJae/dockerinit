package com.dockerinit.linux.crawling;

import org.springframework.stereotype.Service;

@Service
public class ManParseService {
    private static final java.util.regex.Pattern HDR = java.util.regex.Pattern.compile("^(NAME|SYNOPSIS|DESCRIPTION|OPTIONS|EXAMPLES|SEE ALSO)\\s*$");
    private static final java.util.regex.Pattern OPT = java.util.regex.Pattern.compile("^\\s*(-[\\w?],?\\s*)?(--[\\w-]+)?(?:\\s+(\\w+))?\\s*-\\s*(.+)$");

    public ParsedEn parse(String command, String raw) {
        var sections = splitSections(raw);
        String synopsisText = join(sections.get("SYNOPSIS"));
        String description = join(sections.get("DESCRIPTION"));
        java.util.List<ParsedOption> options = new java.util.ArrayList<>();
        for (String line : sections.getOrDefault("OPTIONS", java.util.List.of())) {
            var m = OPT.matcher(line);
            if (m.find()) {
                java.util.List<String> flags = new java.util.ArrayList<>();
                if (m.group(1) != null) flags.add(m.group(1).trim().replaceAll(",\\s*$",""));
                if (m.group(2) != null) flags.add(m.group(2).trim());
                String arg = m.group(3);
                String desc = m.group(4);
                options.add(new ParsedOption(flags, primaryFlag(flags), arg, /*argRequired*/ arg != null, null, null, desc));
            }
        }
        return new ParsedEn(synopsisText, description, java.util.List.of(), java.util.List.of(), options);
    }

    private java.util.Map<String, java.util.List<String>> splitSections(String raw) {
        var map = new java.util.LinkedHashMap<String, java.util.List<String>>();
        String current = "DESCRIPTION";
        map.put(current, new java.util.ArrayList<>());
        for (String line : raw.split("\\R")) {
            var m = HDR.matcher(line.trim());
            if (m.matches()) {
                current = m.group(1);
                map.putIfAbsent(current, new java.util.ArrayList<>());
            } else {
                map.get(current).add(line);
            }
        }
        map.replaceAll((k, v) -> v.stream().map(String::trim).filter(s -> !s.isBlank()).toList());
        return map;
    }
    private String join(java.util.List<String> lines){ return (lines==null||lines.isEmpty())?null:String.join("\n",lines);}
    private String primaryFlag(java.util.List<String> flags){ return flags.isEmpty()?null:flags.get(flags.size()-1); }

    // 내부 DTO
    public record ParsedEn(String synopsisAsText, String description,
                           java.util.List<String> arguments, java.util.List<String> examples,
                           java.util.List<ParsedOption> options) {}
    public record ParsedOption(java.util.List<String> flags, String primaryFlag, String argName,
                               boolean argRequired, String typeHint, String defaultValue,
                               String description) {}
}
