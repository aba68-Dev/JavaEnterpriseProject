package com.enterprise.rest.config;

import com.enterprise.rest.exception.GlobalExceptionMapper;
import com.enterprise.rest.filter.CorrelationIdFilter;
import com.enterprise.rest.resource.OrderResource;
import com.enterprise.rest.resource.ProductResource;
import com.enterprise.rest.resource.UserResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

/**
 * Jersey (JAX-RS) application configuration.
 *
 * Registers all resources, filters, and providers in one place.
 * Design Pattern: Registry — central configuration for Jersey components.
 */
@Configuration
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        // Resources
        register(ProductResource.class);
        register(OrderResource.class);
        register(UserResource.class);

        // Filters and interceptors
        register(CorrelationIdFilter.class);

        // Exception handling
        register(GlobalExceptionMapper.class);

        // JSON serialisation
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        register(mapper);
    }
}
