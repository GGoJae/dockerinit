package com.dockerinit.linux.mapper;

import com.dockerinit.global.exception.InvalidInputCustomException;
import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.domain.syntax.Option;
import com.dockerinit.linux.domain.syntax.Synopsis;
import com.dockerinit.linux.domain.syntax.SynopsisPattern;
import com.dockerinit.linux.domain.syntax.TokenDescriptor;
import com.dockerinit.linux.dto.request.CreateLinuxCommandRequest;
import com.dockerinit.linux.dto.request.spec.OptionSpecDTO;
import com.dockerinit.linux.dto.request.spec.SynopsisPatternSpecDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;

import static com.dockerinit.global.constants.ErrorMessage.LINUX_COMMAND_DUPLICATE_FLAG;
import static com.dockerinit.global.constants.ErrorMessage.LINUX_COMMAND_REQUIRED_OPTION;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LinuxCommandMapper {

    public static LinuxCommand requestToDomain(CreateLinuxCommandRequest request) {

        Map<String, Option> optionInfoMap = dtoToOptionMap(request.options(), request.optionRequired());
        Synopsis synopsis = dtoToSynopsis(request.synopsis());

        return  LinuxCommand.createForManual(
                request.category(),
                request.command(),
                request.aliases(),
                request.description(),
                synopsis,
                request.arguments(),
                request.examples(),
                request.verified(),
                request.optionRequired(),
                optionInfoMap,
                request.tags()
        );
    }

    private static Synopsis dtoToSynopsis(List<SynopsisPatternSpecDTO> dto) {
        List<SynopsisPattern> linuxTokenDescriptions = new ArrayList<>();

        dto.stream()
                .map(spd -> {
                    ArrayList<TokenDescriptor> tokenDescriptors = new ArrayList<>();
                    spd.tokens().forEach(td -> {
                        tokenDescriptors.add(new TokenDescriptor(td.tokenType(), td.repeat(), td.optional(), td.description()));
                    });
                    return new SynopsisPattern(tokenDescriptors);
                }).forEach(linuxTokenDescriptions::add);

        return new Synopsis(linuxTokenDescriptions);
    }

    private static Map<String, Option> dtoToOptionMap(List<OptionSpecDTO> dto, boolean optionRequired) {
        List<OptionSpecDTO> optionList = dto == null ? List.of() : dto;

        if (optionRequired && optionList.isEmpty()) {
            throw new InvalidInputCustomException(LINUX_COMMAND_REQUIRED_OPTION,
                    Map.of("optionRequired", optionRequired, "options", optionList));
        }

        Map<String, Option> optionInfoMap = new LinkedHashMap<>();
        for (OptionSpecDTO o : optionList) {
            Option prev = optionInfoMap.putIfAbsent(
                    o.flag(),
                    new Option(o.argName(), o.argRequired(), o.typeHint(), o.defaultValue(), o.description())
            );
            if (prev != null) {
                throw new InvalidInputCustomException(LINUX_COMMAND_DUPLICATE_FLAG, Map.of("flag", o.flag()));
            }
        }
        return optionInfoMap;
    }
}
