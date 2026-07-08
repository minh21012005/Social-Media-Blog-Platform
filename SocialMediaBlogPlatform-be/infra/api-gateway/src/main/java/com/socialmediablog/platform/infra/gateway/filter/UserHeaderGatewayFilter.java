package com.socialmediablog.platform.infra.gateway.filter;

import com.socialmediablog.platform.common.security.GatewayUserHeaders;
import com.socialmediablog.platform.common.security.JwtSupport;
import org.springframework.cloud.gateway.filter.NettyRoutingFilter;
import java.util.Set;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class UserHeaderGatewayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .filter(Authentication::isAuthenticated)
                .filter(authentication -> authentication.getPrincipal() instanceof Jwt)
                .map(authentication -> withUserHeaders(exchange, (Jwt) authentication.getPrincipal()))
                .defaultIfEmpty(stripUserHeaders(exchange))
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        // Must run before the routing filter so mutated headers are forwarded downstream.
        return NettyRoutingFilter.ORDER - 1;
    }

    private ServerWebExchange withUserHeaders(ServerWebExchange exchange, Jwt jwt) {
        Set<String> roles = JwtSupport.rolesFrom(jwt);
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(HttpHeaders.AUTHORIZATION);
                    headers.remove(GatewayUserHeaders.USER_ID);
                    headers.remove(GatewayUserHeaders.USERNAME);
                    headers.remove(GatewayUserHeaders.ROLES);
                    headers.add(GatewayUserHeaders.USER_ID, jwt.getSubject());
                    headers.add(GatewayUserHeaders.USERNAME, username(jwt));
                    headers.add(GatewayUserHeaders.ROLES, JwtSupport.rolesHeader(roles));
                })
                .build();
        return exchange.mutate().request(request).build();
    }

    private String username(Jwt jwt) {
        String username = jwt.getClaimAsString("username");
        return username == null ? "" : username;
    }

    private ServerWebExchange stripUserHeaders(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(HttpHeaders.AUTHORIZATION);
                    headers.remove(GatewayUserHeaders.USER_ID);
                    headers.remove(GatewayUserHeaders.USERNAME);
                    headers.remove(GatewayUserHeaders.ROLES);
                })
                .build();
        return exchange.mutate().request(request).build();
    }
}
