package com.dmihalishin.graphql.reactive.spring.api.app.exceptions;

/**
 * Deserialization Exception that could be thrown
 * if request parameter cannot be deserialized
 */
public class DeserializationException extends RuntimeException {

    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
