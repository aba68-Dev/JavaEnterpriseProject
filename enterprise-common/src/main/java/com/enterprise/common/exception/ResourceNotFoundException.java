package com.enterprise.common.exception;

/**
 * Thrown when a requested resource does not exist.
 */
public class ResourceNotFoundException extends EnterpriseException {

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String resourceType, Object id) {
        super(ERROR_CODE, String.format("%s with id [%s] was not found", resourceType, id));
    }
}
