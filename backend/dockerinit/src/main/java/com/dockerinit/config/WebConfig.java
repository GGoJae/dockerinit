package com.dockerinit.config;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebConfig {

    @Bean
    public HttpClient sharedHttpClient() {
        return HttpClient.create()
                .responseTimeout(Duration.ofSeconds(3))                 // 응답 타임 아웃
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 200);     // 연결 타임 아웃
    }
}
