package com.socialmediablog.platform.common.web.correlation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;

public class CorrelationIdFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            filterChain.doFilter(request, response);
            return;
        }
        String correlationId = CorrelationIds.resolve(httpRequest.getHeader(CorrelationHeaders.CORRELATION_ID));
        httpResponse.setHeader(CorrelationHeaders.CORRELATION_ID, correlationId);
        MDC.put(CorrelationHeaders.MDC_CORRELATION_ID, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CorrelationHeaders.MDC_CORRELATION_ID);
        }
    }
}
