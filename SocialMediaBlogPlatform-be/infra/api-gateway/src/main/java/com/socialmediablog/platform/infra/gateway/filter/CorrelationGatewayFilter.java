package com.socialmediablog.platform.infra.gateway.filter;

import com.socialmediablog.platform.common.web.correlation.CorrelationHeaders;
import com.socialmediablog.platform.common.web.correlation.CorrelationIds;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CorrelationGatewayFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CorrelationGatewayFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Instant startedAt = Instant.now();
        String correlationId = CorrelationIds.resolve(
                exchange.getRequest().getHeaders().getFirst(CorrelationHeaders.CORRELATION_ID)
        );
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> headers.set(CorrelationHeaders.CORRELATION_ID, correlationId))
                .build();
        ServerWebExchange correlatedExchange = exchange.mutate().request(request).build();
        correlatedExchange.getResponse().getHeaders().set(CorrelationHeaders.CORRELATION_ID, correlationId);

        return chain.filter(correlatedExchange)
                .doFinally(signalType -> {
                    withCorrelationMdc(correlationId, () ->
                            logCompletedRequest(correlatedExchange, correlationId, startedAt)
                    );
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private void logCompletedRequest(ServerWebExchange exchange, String correlationId, Instant startedAt) {
        HttpStatusCode status = exchange.getResponse().getStatusCode();
        long durationMs = Duration.between(startedAt, Instant.now()).toMillis();
        log.info(
                "request completed correlationId={} method={} path={} status={} durationMs={}",
                correlationId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getRawPath(),
                status == null ? "UNKNOWN" : status.value(),
                durationMs
        );
    }

    private void withCorrelationMdc(String correlationId, Runnable action) {
        MDC.put(CorrelationHeaders.MDC_CORRELATION_ID, correlationId);
        try {
            action.run();
        } finally {
            MDC.remove(CorrelationHeaders.MDC_CORRELATION_ID);
        }
    }
}
