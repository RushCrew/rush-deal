package com.rushcrew.payment_service.global.security.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안 함 (Stateless 설정)
                .authorizeHttpRequests(auth -> auth
                    // Actuator 엔드포인트를 경로와 상관없이 모두 허용
                    // 엔드포인트 요청(health, info 등)을 자동으로 인식하여 허용합니다.
                    .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                    // 경로별 권한 설정
                    // 보통 @PreAuthorize로 처리하므로 모두 허용하거나 authenticated로 설정
                    // Actuator나 헬스 체크 경로는 열어두는 것이 좋음
                    .requestMatchers("/actuator/**", "/health").permitAll()
                    .requestMatchers("/api/v1/payments/**").permitAll()
                    .anyRequest().authenticated()
                );
        return http.build();
    }
}
