package com.enterprise.common.exception;

/**
 * Base exception for all enterprise application errors.
 * Follows Clean Code principle: descriptive exception hierarchy.
 */
public class EnterpriseException extends RuntimeException {

    private final String errorCode;

    public EnterpriseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public EnterpriseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
