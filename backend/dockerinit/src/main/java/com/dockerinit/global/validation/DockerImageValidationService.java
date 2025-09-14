package com.dockerinit.global.validation;

import com.dockerinit.global.exception.InternalErrorCustomException;
import com.dockerinit.global.exception.RateLimitExceededCustomException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.dockerinit.global.constants.UrlInfo.DOCKER_HUB_API;

@Slf4j
@Service
public class DockerImageValidationService {

    private final WebClient webClient;
    private final Map<String, Boolean> imageCache = new ConcurrentHashMap<>();

    public DockerImageValidationService(HttpClient sharedHttpClient) {
        this.webClient = WebClient.builder()
                .baseUrl(DOCKER_HUB_API)
                .clientConnector(new ReactorClientHttpConnector(sharedHttpClient))
                .build();

    }

    public boolean existsInDockerHub(String imageWithTag) {
        return imageCache.computeIfAbsent(imageWithTag, this::checkImageExists);
    }

    private boolean checkImageExists(String imageWithTag) {
        try {
            String[] parts = imageWithTag.split(":");
            String image = parts[0];
            String tag = parts.length > 1 ? parts[1] : "latest";

            String[] imageParts = image.split("/");
            String namespace = imageParts.length == 1 ? "library" : imageParts[0];
            String repo = imageParts.length == 1 ? image : imageParts[1];

            String url = String.format("/%s/%s/tags/%s", namespace, repo, tag);

            webClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError,
                            res -> Mono.error(new InternalErrorCustomException("도커 허브 서버 에러")))
                    .onStatus(status -> status == HttpStatus.TOO_MANY_REQUESTS,
                            res -> Mono.error(new RateLimitExceededCustomException("도커 허브 요청 제한 초과")))
                    .bodyToMono(String.class)
                    .block();

            return true;
        } catch (Exception ex) {
            log.warn("Docker Hub 에서 이미지를 찾을 수 없음: {}", imageWithTag);
            return false;
        }

    }
}
