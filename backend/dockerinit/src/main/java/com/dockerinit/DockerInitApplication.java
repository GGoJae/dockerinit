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
	 * - 도커파일, 컴포즈 등 프리셋 mongodb 에 저장
	 * - 프리셋 겟 하는 api
	 * - api 캐시 처리
	 * - 레디스에 저장해놓은 검색횟수 db 에 반영하는 스케쥴 로직
	 * - == null -> Objects.isNull 로 일관되게 고치기
	 * - 전체 로그찍기 AOP ?
	 * - man 페이지 크롤링 -> 번역 api -> db에 저장하는 로직 장성
	 * - 스프링 시큐리티 설정
	 * - jwt 토큰 설정??
	 * - request 에 내부 record 로 처리할지 spec 으로 뺼지 일관성있게 정하기
	 *
	 * ============== 고려 사항 ==============
	 * - 회원 정보 만들건지 고려해보기 있으면 자주 사용하는 명령어, 내가 사용한 명령어
	 * - 확장 리눅스 rpg
	 */
}
