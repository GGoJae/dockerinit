package com.dockerinit.global.warmer;

import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.repository.LinuxCommandRepository;
import com.dockerinit.linux.util.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.dockerinit.global.constants.AutoCompleteSuggest.REDIS_PREFIX_LENGTH_LIMIT;

@Component
@RequiredArgsConstructor
public class CommandAutocompleteWarmer implements ApplicationRunner {

    private final RedisTemplate<String, String> redis;
    private final LinuxCommandRepository repository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> cmds = repository.findAll().stream().map(LinuxCommand::getCommand).toList();

        for (String cmd : cmds) {
            for (int i = 1; i <= Math.min(cmd.length(), REDIS_PREFIX_LENGTH_LIMIT); i++) {
                String prefix = cmd.substring(0, i);
                String key = RedisKeys.autoCompleteCommand(prefix);
                redis.opsForZSet().add(key, cmd, 0.0);
            }
        }

    }
}
