package com.enterprise.common.exception;

/**
 * Thrown when an operation is not authorized.
 */
public class UnauthorizedException extends EnterpriseException {

    private static final String ERROR_CODE = "UNAUTHORIZED";

    public UnauthorizedException(String message) {
        super(ERROR_CODE, message);
    }
}
