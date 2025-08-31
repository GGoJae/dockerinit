package com.dockerinit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.util.Optional;

@EnableMongoAuditing
@Configuration
public class MongoConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of("system");
    }
    // TODO 나중에 스프링 시큐리티 사용하면 아래처럼 구현/ 교체

//    public class SecurityAuditorAware implements AuditorAware<String> {
//        @Override
//        public Optional<String> getCurrentAuditor() {
//            var ctx = SecurityContextHolder.getContext();
//            if (ctx == null) return Optional.of("system");
//            var auth = ctx.getAuthentication();
//            if (auth == null || !auth.isAuthenticated()
//                    || auth instanceof AnonymousAuthenticationToken) {
//                return Optional.of("system");
//            }
//            return Optional.ofNullable(auth.getName());
//        }
//    }

}
