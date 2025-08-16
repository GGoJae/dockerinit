package com.dockerinit.global.support.warmer;

import com.dockerinit.linux.domain.model.LinuxCommand;
import com.dockerinit.linux.infrastructure.repository.LinuxCommandRepository;
import com.dockerinit.linux.infrastructure.redis.RedisKeys;
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
                String key = RedisKeys.acCmdZSet(prefix);       //TODO prefix 가 아니라 모듈리스트나.. 예) linux, docker 리스트 넣기
                redis.opsForZSet().add(key, cmd, 0.0);
            }
        }

    }
}
