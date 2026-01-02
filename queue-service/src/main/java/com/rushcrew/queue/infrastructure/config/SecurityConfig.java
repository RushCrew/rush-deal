package com.rushcrew.queue.infrastructure.config;

import com.rushcrew.queue.infrastructure.filter.AuthorizationFilter;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final AuthorizationFilter authorizationFilter;

    public SecurityConfig(AuthorizationFilter authorizationFilter) {
        this.authorizationFilter = authorizationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화 (API 서버이므로 필요 없음)
            .formLogin(AbstractHttpConfigurer::disable) // Form Login, Basic Http 비활성화 (Gateway가 인증을 하므로)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안 함 (Stateless 설정)
            .logout(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Actuator 엔드포인트를 경로와 상관없이 모두 허용
                // 엔드포인트 요청(health, info 등)을 자동으로 인식하여 허용합니다.
                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                // 경로별 권한 설정
                // 보통 @PreAuthorize로 처리하므로 모두 허용하거나 authenticated로 설정
                // Actuator나 헬스 체크 경로는 열어두는 것이 좋음
                .requestMatchers("/actuator/**", "/health").permitAll()
                .requestMatchers("/api/v1/internal/queues/**").permitAll()
                .requestMatchers("/api/v1/queues/**").permitAll()
                .requestMatchers("/api/v1/queue/policies/**").permitAll()
                .requestMatchers("/api/v1/test/scheduler/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
