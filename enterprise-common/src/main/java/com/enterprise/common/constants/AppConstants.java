package com.enterprise.common.constants;

/**
 * Application-wide constants.
 * Clean Code: named constants eliminate magic strings.
 */
public final class AppConstants {

    private AppConstants() {}

    // HTTP Headers
    public static final String HEADER_CORRELATION_ID    = "X-Correlation-Id";
    public static final String HEADER_REQUEST_ID        = "X-Request-Id";
    public static final String HEADER_AUTHORIZATION     = "Authorization";
    public static final String BEARER_PREFIX            = "Bearer ";

    // Kafka Topics
    public static final String TOPIC_ORDER_CREATED      = "order.created";
    public static final String TOPIC_ORDER_UPDATED      = "order.updated";
    public static final String TOPIC_ORDER_CANCELLED    = "order.cancelled";
    public static final String TOPIC_PRODUCT_UPDATED    = "product.updated";
    public static final String TOPIC_USER_REGISTERED    = "user.registered";
    public static final String TOPIC_AUDIT_EVENTS       = "audit.events";

    // Consumer Groups
    public static final String GROUP_ORDER_SERVICE      = "order-service-group";
    public static final String GROUP_NOTIFICATION       = "notification-group";
    public static final String GROUP_AUDIT              = "audit-group";

    // Pagination defaults
    public static final int    DEFAULT_PAGE_SIZE        = 20;
    public static final int    MAX_PAGE_SIZE            = 100;

    // Security
    public static final String ROLE_ADMIN               = "ROLE_ADMIN";
    public static final String ROLE_USER                = "ROLE_USER";
    public static final String ROLE_SERVICE             = "ROLE_SERVICE";
    public static final String SCOPE_READ               = "read";
    public static final String SCOPE_WRITE              = "write";
}
