package com.example.spin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI spinOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SPIN API")
                        .version("v1")
                        .description("""
                                대전 동네 나들이 루트 추천 서비스 API.

                                인증: 세션 쿠키(JSESSIONID) 기반. POST /auth/login 응답으로 세션 쿠키가 내려오고, \
                                이후 요청은 쿠키만 실어 보내면 됩니다.

                                CSRF: /auth/signup, /auth/login을 제외한 모든 POST/PUT/DELETE 요청은 \
                                XSRF-TOKEN 쿠키 값을 X-XSRF-TOKEN 헤더에 그대로 실어 보내야 합니다.

                                로그아웃: POST /auth/logout (별도 컨트롤러 없이 Spring Security가 처리하는 엔드포인트라 \
                                아래 목록에는 안 보입니다)
                                """))
                .components(new Components()
                        .addSecuritySchemes("session", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")));
    }
}
