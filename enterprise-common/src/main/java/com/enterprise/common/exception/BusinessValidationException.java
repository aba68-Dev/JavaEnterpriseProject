package com.enterprise.common.exception;

/**
 * Thrown when business validation rules are violated.
 */
public class BusinessValidationException extends EnterpriseException {

    private static final String ERROR_CODE = "BUSINESS_VALIDATION_ERROR";

    public BusinessValidationException(String message) {
        super(ERROR_CODE, message);
    }
}
