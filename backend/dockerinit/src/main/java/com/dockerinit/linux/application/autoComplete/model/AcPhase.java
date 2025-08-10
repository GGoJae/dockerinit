package com.dockerinit.linux.application.autoComplete.model;

public enum AcPhase {
    COMMAND,            // 초기 명령어 입력
    OPTION,             // 옵션 플래그
    ARGUMENT,           // 옵션에 필요한 인자
    OPTION_OR_ARGUMENT, // ambiguous 상태 ("-o -" 같이 옵션인지 인자인지 불분명)
    TARGET              // 명령의 대상이 될 파일/디렉토리 등 (예: rm file.txt)
}

