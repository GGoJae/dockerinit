package com.dockerinit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DockerInitApplication {

	public static void main(String[] args) {
		SpringApplication.run(DockerInitApplication.class, args);
	}
	/**
	 * TODO
	 * - 프리셋 mongodb 에 저장
	 * - 전체 로그찍기 AOP ?
	 * - man 페이지 크롤링 -> 번역 api -> db에 저장하는 로직 장성
	 * - 스프링 시큐리티 도입 및 설정
	 * - jwt 토큰 설정??
	 * ============== 고려 사항 ==============
	 * - 회원 정보 만들건지 고려해보기 있으면 자주 사용하는 명령어, 내가 사용한 명령어
	 * - 확장 리눅스 rpg
	 */
}
