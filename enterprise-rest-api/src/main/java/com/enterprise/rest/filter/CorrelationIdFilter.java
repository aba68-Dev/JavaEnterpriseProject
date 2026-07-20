package com.enterprise.rest.filter;

import com.enterprise.common.constants.AppConstants;
import com.enterprise.common.util.CorrelationIdGenerator;
import jakarta.annotation.Priority;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;

/**
 * JAX-RS ContainerRequestFilter that injects a correlation ID into every request.
 *
 * Clean Code: cross-cutting concern isolated in a filter, not scattered in business code.
 * Design Pattern: Decorator — wraps every request transparently.
 */
@Slf4j
@Provider
@Priority(1)
public class CorrelationIdFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String MDC_KEY = "correlationId";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String correlationId = requestContext.getHeaderString(AppConstants.HEADER_CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = CorrelationIdGenerator.generate();
        }
        MDC.put(MDC_KEY, correlationId);
        requestContext.setProperty(MDC_KEY, correlationId);
        log.debug("Request [{} {}] correlationId={}",
                requestContext.getMethod(), requestContext.getUriInfo().getPath(), correlationId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        String correlationId = (String) requestContext.getProperty(MDC_KEY);
        if (correlationId != null) {
            responseContext.getHeaders().add(AppConstants.HEADER_CORRELATION_ID, correlationId);
        }
        MDC.remove(MDC_KEY);
    }
}
