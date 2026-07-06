package com.socialmediablog.platform.infra.gateway.config;

import com.socialmediablog.platform.common.security.JwtProperties;
import com.socialmediablog.platform.common.security.JwtSupport;
import com.socialmediablog.platform.common.security.error.ReactiveSecurityErrorResponseWriter;
import com.socialmediablog.platform.common.web.correlation.CorrelationHeaders;
import com.socialmediablog.platform.common.web.error.ErrorCode;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class GatewaySecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((exchange, exceptionCause) ->
                                ReactiveSecurityErrorResponseWriter.write(
                                        exchange,
                                        HttpStatus.UNAUTHORIZED,
                                        ErrorCode.UNAUTHORIZED,
                                        "Authentication is required"
                                ))
                        .accessDeniedHandler((exchange, exceptionCause) ->
                                ReactiveSecurityErrorResponseWriter.write(
                                        exchange,
                                        HttpStatus.FORBIDDEN,
                                        ErrorCode.FORBIDDEN,
                                        "Access is denied"
                                ))
                )
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                        .pathMatchers("/api/v1/users/me", "/api/v1/users/me/**").authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/v1/users/*", "/api/v1/users/by-username/*", "/api/v1/users/public").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/articles", "/api/v1/articles/featured", "/api/v1/articles/editor-picks", "/api/v1/articles/trending", "/api/v1/articles/slug/**", "/api/v1/articles/status").permitAll()
                        .pathMatchers("/ws/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/articles/*/comments").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/comments/*/replies").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/articles/*/views").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/presence/**").permitAll()
                        .pathMatchers("/api/v1/**").authenticated()
                        .anyExchange().permitAll()
                )
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .authenticationEntryPoint((exchange, exceptionCause) ->
                                ReactiveSecurityErrorResponseWriter.write(
                                        exchange,
                                        HttpStatus.UNAUTHORIZED,
                                        ErrorCode.UNAUTHORIZED,
                                        "Authentication is required"
                                ))
                        .accessDeniedHandler((exchange, exceptionCause) ->
                                ReactiveSecurityErrorResponseWriter.write(
                                        exchange,
                                        HttpStatus.FORBIDDEN,
                                        ErrorCode.FORBIDDEN,
                                        "Access is denied"
                                ))
                        .jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    ReactiveJwtDecoder reactiveJwtDecoder(JwtProperties jwtProperties) {
        return NimbusReactiveJwtDecoder.withPublicKey(JwtSupport.rsaPublicKey(jwtProperties))
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
    }

    @Bean
    CorsWebFilter corsWebFilter(@Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(splitCsv(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                CorrelationHeaders.CORRELATION_ID
        ));
        configuration.setExposedHeaders(List.of("Authorization", CorrelationHeaders.CORRELATION_ID));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsWebFilter(source);
    }

    private List<String> splitCsv(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }
}
