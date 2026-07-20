package com.enterprise.common.util;

import java.util.UUID;

/**
 * Utility for generating correlation/request IDs for distributed tracing.
 * Clean Code: utility class with a single responsibility.
 */
public final class CorrelationIdGenerator {

    private CorrelationIdGenerator() {
        // Utility class — do not instantiate
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
