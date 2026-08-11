package kr.co.gymleco.security.config;

import jakarta.servlet.DispatcherType;
import kr.co.gymleco.config.GymlecoProperties;
import kr.co.gymleco.security.jwt.CookieBearerTokenResolver;
import kr.co.gymleco.security.jwt.JwtTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * ── 1순위: 관리자 API ──────────────────────────────────
     */
    @Bean
    @Order(1)
    SecurityFilterChain adminApiChain(
        HttpSecurity http,
        JwtTokenService jwtTokenService,
        CookieBearerTokenResolver bearerTokenResolver
    ) throws Exception {

        http
            .securityMatcher("/api/admin/**")

            .authorizeHttpRequests(auth -> auth
                // 로그인 자체는 열려 있어야 한다. 그 외는 전부 ADMIN.
                .requestMatchers("/api/admin/auth/login",
                    "/api/admin/auth/refresh").permitAll()
                .anyRequest().hasRole("ADMIN"))

            /*
             * 쿠키 인증으로 가는 순간 CSRF 대상이 된다.
             * SameSite=Strict 가 대부분 막아주지만 그건 브라우저 정책
             * 하나에만 의존하는 1겹이다. 토큰을 함께 쓴다.
             * withHttpOnlyFalse: SPA 가 쿠키를 읽어 헤더로 되돌려 보내야
             * 하므로 이 쿠키만은 JS 가 읽을 수 있어야 한다.
             */
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/admin/auth/login"))
            // JWT 를 쓰므로 서버 세션을 만들지 않는다
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(oauth2 -> oauth2
                .bearerTokenResolver(bearerTokenResolver)
                .jwt(jwt -> jwt
                    .decoder(jwtTokenService.decoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())))
            /*
             * 인증 실패에 401 만 돌려준다.
             * 기본 동작은 로그인 페이지로 리다이렉트인데, API 에서는
             * SPA 가 302 를 받고 엉뚱하게 해석한다.
             */
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

            .headers(headers -> headers
                .frameOptions(frame -> frame.deny()));

        return http.build();
    }

    /**
     * ── 2순위: 공개 API ────────────────────────────────────
     *
     * 인증이 없다. 대신 rate limit 과 honeypot 이 남용을 막는다
     * (별도 필터로 추가한다).
     */
    @Bean
    @Order(2)
    SecurityFilterChain publicApiChain(HttpSecurity http) throws Exception {

        http
            .securityMatcher("/api/public/**")

            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll())
            /*
             * 공개 API 는 브라우저 쿠키를 쓰지 않는다.
             * 문의 폼도 Next.js Route Handler 가 서버 간 호출로 전달하므로
             * CSRF 토큰을 요구할 대상이 없다.
             */
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .cors(Customizer.withDefaults());

        return http.build();
    }
    /**
     * ── 액추에이터 ──────────────────────────────────────────
     *
     * 8081 내부 전용 포트에 있지만 Spring Security 는 포트를 구분하지
     * 않으므로 아래 denyAll 에 걸린다. 헬스체크만 열고 나머지는 계속 막는다.
     *
     * 노출 자체는 application.yml 의 exposure.include 로 health 만 켜져 있다.
     * 여기서 한 겹 더 좁힌다.
     */
    @Bean
    @Order(0)
    SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .anyRequest().denyAll())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    /**
     * ── 마지막: 그 외 모든 경로를 막는다 ────────────────────
     *
     * 기본을 "차단"으로 둔다. 새 엔드포인트를 만들면서 체인 등록을
     * 잊어도 열리지 않는다.
     *
     * ★ 단, 프레임워크 내부 디스패치는 예외로 둬야 한다.
     *
     *   예외가 발생하면 Spring 은 /error 로 포워드하는데, 그 포워드가
     *   필터 체인을 다시 탄다. denyAll 이 그것까지 막으면 400 이든 500 이든
     *   전부 403 으로 덮여 나온다.
     *
     *   실제로 문의 접수의 검증 실패가 403 으로 바뀌어 원인 파악을
     *   가로막았다. 오류를 감추는 보안 설정은 보안이 아니라 장애다.
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    SecurityFilterChain denyRestChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD)
                    .permitAll()
                .anyRequest().denyAll())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthorityPrefix("ROLE_");
        authorities.setAuthoritiesClaimName("role");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    /**
     * BCrypt. strength 12 는 로그인 1회에 약 0.3초가 걸리는 수준이다.
     * 관리자가 1~2명이므로 이 비용은 문제가 되지 않고,
     * 무차별 대입 비용은 그만큼 올라간다.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource(GymlecoProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(properties.cors().allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/public/**", config);
        return source;
    }
}
