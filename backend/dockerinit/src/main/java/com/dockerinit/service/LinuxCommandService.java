package com.dockerinit.service;

import com.dockerinit.dto.linuxCommand.LinuxCommandRequest;
import com.dockerinit.dto.linuxCommand.LinuxCommandResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class LinuxCommandService {

    private final Map<String, CommandHelp> explanationMap;

    public LinuxCommandService() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, CommandHelp>> typeRef = new TypeReference<>() {};

        explanationMap = mapper.readValue(
                 new ClassPathResource("data/linux-option-explanations.json").getInputStream(),
                typeRef
        );
    }

    public LinuxCommandResponse generate(LinuxCommandRequest request) {
        String commandName = request.getCommand().toLowerCase();
        CommandHelp help = explanationMap.get(commandName);
        String cmdDescription = help != null ? help.description : "";

        Map<String, String> options = (help != null && help.options != null) ? help.options : Map.of();

        StringBuilder cmd = new StringBuilder(commandName);
        ArrayList<LinuxCommandResponse.Explanation> exps = new ArrayList<>();

        if (request.getArgs() != null) {
            for (String arg : request.getArgs()) {
                cmd.append(" ").append(arg);
                if (options.containsKey(arg)) {
                    exps.add(new LinuxCommandResponse.Explanation(arg, options.get(arg)));
                }
            }
        }

        if (request.getTarget() != null && !request.getTarget().isEmpty()) {
            cmd.append(" ").append(request.getTarget());
        }

        return new LinuxCommandResponse(cmd.toString().trim(), cmdDescription, exps);
    }


    public List<String> autocompleteCommand(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return Collections.emptyList();
        }

        String prefixLowerCase = prefix.toLowerCase();

        return explanationMap.keySet()
                .stream()
                .filter(cmd -> cmd.toLowerCase().contains(prefixLowerCase))
                .sorted(
                        Comparator.comparingInt(cmd ->
                        {
                            int index = popularCommands.indexOf(cmd);
                            return index == -1 ? 999 : index;
                        })
                )
                .toList();
    }

    public List<String> autocompleteOptions(String command, String prefix) {
        CommandHelp help = explanationMap.get(command.toLowerCase());
        if (help == null || help.options == null) return List.of();

        return help.options.keySet().stream()
                .filter(opt -> opt.contains(prefix))
                .sorted()
                .toList();
    }

    record CommandHelp(String description, Map<String, String> options) {
    }

    private static final List<String> popularCommands = List.of(
            "ls", "cd", "cat", "chmod", "chown", "find", "grep", "tar", "ping"
    );

}
