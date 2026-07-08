package com.socialmediablog.platform.services.article.config;

import com.socialmediablog.platform.common.security.GatewayHeaderAuthenticationFilter;
import com.socialmediablog.platform.common.security.error.ServletSecurityErrorResponseWriter;
import com.socialmediablog.platform.common.web.correlation.CorrelationIdFilter;
import com.socialmediablog.platform.common.web.error.ErrorCode;
import java.time.Clock;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter
    ) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                ServletSecurityErrorResponseWriter.write(
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        ErrorCode.UNAUTHORIZED,
                                        "Authentication is required"
                                ))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                ServletSecurityErrorResponseWriter.write(
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        ErrorCode.FORBIDDEN,
                                        "Access is denied"
                                ))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info", "/error").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/internal/articles/*/comment-policy").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/articles", "/api/v1/articles/featured", "/api/v1/articles/editor-picks", "/api/v1/articles/trending", "/api/v1/articles/slug/**", "/api/v1/articles/id/**", "/api/v1/articles/status").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/articles/*/views").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter() {
        return new GatewayHeaderAuthenticationFilter();
    }

    @Bean
    FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>(new CorrelationIdFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
