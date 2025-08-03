package com.dockerinit.global.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorMessage {
    public static final String PRESET_NOT_FOUND = "요청한 프리셋이 존재하지 않습니다.";
    public static final String FAILED_TO_CREATE_ZIP = "Zip 생성 중 오류 발생";
    public static final String FAILED_TO_LOAD_PRESETS = "프리셋 파일 로딩 중 오류가 발생했습니다.";
    public static final String INVALID_INPUT = "입력값 검증 실패";
    public static final String INVALID_DOCKER_IMAGE = "유효하지 않은 Docker 이미지입니다.";
    public static final String LINUX_COMMAND_NOT_FOUND = "리눅스 커맨드를 찾을 수 없습니다.";
    public static final String LINUX_COMMAND_ID_NOT_FOUND = "리눅스 커맨드를 db에서 찾을 수 없습니다.";
    public static final String LINUX_COMMAND_DUPLICATE_FLAG = "리눅스 커맨드의 옵션 플래그가 중복입니다.";
    public static final String LINUX_COMMAND_REQUIRED_OPTION = "리눅스 커맨드의 옵션이 필수인데 누락됐습니다";
    public static final String LINUX_CURSOR_OUT_OF_RANGE = "커서의 범위를 벗어났습니다.";
}
