package com.dmihalishin.graphql.reactive.spring.api.app.exceptions;

/**
 * Wraps an {@link Throwable} for {@link org.springframework.retry.annotation.Retryable} methods
 */
public class RetryableException extends RuntimeException {
    /**
     * Creates {@link RetryableException} wrapping a {@link Throwable} with a custom message
     *
     * @param message custom message
     * @param cause   wrapped {@link Throwable}
     */
    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
