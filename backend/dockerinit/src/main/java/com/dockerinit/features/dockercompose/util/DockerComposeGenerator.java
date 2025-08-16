package com.dockerinit.features.dockercompose.util;

import com.dockerinit.features.dockercompose.dto.DockerComposeRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DockerComposeGenerator {

    public static String generateYml(DockerComposeRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("version: '3.8'\n");
        sb.append("services:\n");

        switch (request.language()) {
            case "java17" -> {
                sb.append("  app:\n")
                        .append("    image: openjdk:17\n")
                        .append("    volumes:\n")
                        .append("      - .:/app\n")
                        .append("    working_dir: /app\n")
                        .append("    command: [\"java\", \"-jar\", \"app.jar\"]\n")
                        .append("    ports:\n")
                        .append("      - \"8080:8080\"\n")
                        .append("    depends_on:\n");
            }
            case "node18" -> {
                sb.append("  app:\n")
                        .append("    image: node:18\n")
                        .append("    volumes:\n")
                        .append("      - .:/app\n")
                        .append("    working_dir: /app\n")
                        .append("    command: [\"npm\", \"start\"]\n")
                        .append("    ports:\n")
                        .append("      - \"3000:3000\"\n")
                        .append("    depends_on:\n");
            }
            case "python311" -> {
                sb.append("  app:\n")
                        .append("    image: python:3.11\n")
                        .append("    volumes:\n")
                        .append("      - .:/app\n")
                        .append("    working_dir: /app\n")
                        .append("    command: [\"python\", \"main.py\"]\n")
                        .append("    ports:\n")
                        .append("      - \"8000:8000\"\n")
                        .append("    depends_on:\n");
            }
        }

        // 종속 서비스들 리스트
        if (request.database() != null) sb.append("      - ").append(request.database()).append("\n");
        if (request.cache() != null) sb.append("      - ").append(request.cache()).append("\n");
        if (request.messageQueue() != null) sb.append("      - ").append(request.messageQueue()).append("\n");

        sb.append("\n");

        // 데이터베이스
        if ("postgres".equals(request.database())) {
            sb.append("  postgres:\n")
                    .append("    image: postgres:15\n")
                    .append("    environment:\n")
                    .append("      POSTGRES_USER: user\n")
                    .append("      POSTGRES_PASSWORD: password\n")
                    .append("    ports:\n")
                    .append("      - \"5432:5432\"\n");
        } else if ("mysql".equals(request.database())) {
            sb.append("  mysql:\n")
                    .append("    image: mysql:8.0\n")
                    .append("    environment:\n")
                    .append("      MYSQL_ROOT_PASSWORD: password\n")
                    .append("    ports:\n")
                    .append("      - \"3306:3306\"\n");
        } else if ("mongodb".equals(request.database())) {
            sb.append("  mongodb:\n")
                    .append("    image: mongo\n")
                    .append("    ports:\n")
                    .append("      - \"27017:27017\"\n");
        }

        // 캐시
        if ("redis".equals(request.cache())) {
            sb.append("  redis:\n")
                    .append("    image: redis\n")
                    .append("    ports:\n")
                    .append("      - \"6379:6379\"\n");
        }

        // MQ
        if ("kafka".equals(request.messageQueue())) {
            sb.append("  kafka:\n")
                    .append("    image: bitnami/kafka:latest\n")
                    .append("    environment:\n")
                    .append("      KAFKA_BROKER_ID: 1\n")
                    .append("      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181\n")
                    .append("    ports:\n")
                    .append("      - \"9092:9092\"\n")
                    .append("  zookeeper:\n")
                    .append("    image: bitnami/zookeeper:latest\n")
                    .append("    ports:\n")
                    .append("      - \"2181:2181\"\n");
        }

        return sb.toString();
    }
}
