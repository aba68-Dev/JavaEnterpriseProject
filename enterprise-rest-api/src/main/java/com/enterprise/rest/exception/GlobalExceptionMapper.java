package com.enterprise.rest.exception;

import com.enterprise.common.exception.BusinessValidationException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.common.exception.UnauthorizedException;
import com.enterprise.common.model.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception mapper — translates domain exceptions to RFC-7807 compatible HTTP responses.
 *
 * Design Pattern: Chain of Responsibility — exceptions bubble up and are caught here.
 * Clean Code: each exception type has one clear handling path.
 */
@Slf4j
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception ex) {
        if (ex instanceof ResourceNotFoundException e) {
            log.warn("Resource not found: {}", e.getMessage());
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        }

        if (ex instanceof BusinessValidationException e) {
            log.warn("Business validation error: {}", e.getMessage());
            return errorResponse(Response.Status.CONFLICT, e.getMessage());
        }

        if (ex instanceof UnauthorizedException e) {
            log.warn("Unauthorized access: {}", e.getMessage());
            return errorResponse(Response.Status.UNAUTHORIZED, e.getMessage());
        }

        if (ex instanceof ConstraintViolationException e) {
            List<String> violations = e.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.toList());
            log.warn("Validation failed: {}", violations);
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ApiResponse.error("Validation failed", violations))
                    .build();
        }

        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return errorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                "An internal server error occurred. Please try again later.");
    }

    private Response errorResponse(Response.Status status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(ApiResponse.error(message))
                .build();
    }
}
