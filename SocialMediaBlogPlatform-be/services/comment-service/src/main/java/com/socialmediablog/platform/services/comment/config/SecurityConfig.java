package com.socialmediablog.platform.services.comment.config;

import com.socialmediablog.platform.common.security.GatewayHeaderAuthenticationFilter;
import com.socialmediablog.platform.common.web.correlation.CorrelationIdFilter;
import java.time.Clock;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
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
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/internal/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/articles/*/comments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/articles/*/comments/count").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/comments/*/replies").permitAll()
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
