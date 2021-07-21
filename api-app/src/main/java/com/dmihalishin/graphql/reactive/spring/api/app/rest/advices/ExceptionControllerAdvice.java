package com.dmihalishin.graphql.reactive.spring.api.app.rest.advices;

import com.dmihalishin.graphql.reactive.spring.api.app.exceptions.DeserializationException;
import com.dmihalishin.graphql.reactive.spring.api.datamodel.resources.ProblemDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebInputException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionControllerAdvice.class);
    private static final String APPLICATION_PROBLEM_JSON = "application/problem+json";

    /**
     * Handles a case when message from request body cannot be de-serialized
     *
     * @param e       any exception of type {@link HttpMessageNotReadableException}
     * @param request request that produced the exception
     * @return {@link ProblemDetail} containing standard body in case of errors
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public HttpEntity<ProblemDetail> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
                                                                           final ServerHttpRequest request) {

        LOGGER.debug("Cannot de-serialize message in request body: {}", e.getMessage());

        final ProblemDetail problem = new ProblemDetail("Message cannot be converted",
                String.format("Invalid request body: %s", e.getMessage()));
        problem.setStatus(HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(problem, overrideContentType(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles a case when validation of the request parameter/body fails
     *
     * @param e       any exception of type {@link MethodArgumentNotValidException} or {@link BindException}
     * @param request request that produced the exception
     * @return {@link ProblemDetail} containing standard body in case of errors
     */
    @ExceptionHandler(value = {MethodArgumentNotValidException.class, BindException.class})
    public HttpEntity<ProblemDetail> handleRequestBindingException(Exception e,
                                                                   final ServerHttpRequest request) {

        LOGGER.debug("Request body is invalid: {}", e.getMessage());

        final ProblemDetail problem = new ProblemDetail("Validation failed", null);
        problem.setStatus(HttpStatus.BAD_REQUEST.value());
        problem.setErrors(extractErrors(e));
        return new ResponseEntity<>(problem, overrideContentType(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles a case when validation of the request parameter/body cannot be deserialized
     *
     * @param e       any exception of type {@link DeserializationException}
     * @param request request that produced the exception
     * @return {@link ProblemDetail} containing standard body in case of errors
     */
    @ExceptionHandler(value = DeserializationException.class)
    public HttpEntity<ProblemDetail> handleDeserializationException(final DeserializationException e,
                                                                    final ServerHttpRequest request) {

        LOGGER.debug("Request parameter is invalid: {}", e.getMessage());

        final ProblemDetail problem = new ProblemDetail("Parameter is invalid", e.getMessage());
        problem.setStatus(HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(problem, overrideContentType(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles a case when validation of the request parameter/body fails
     *
     * @param e       any exception of type {@link ServerWebInputException}
     * @param request request that produced the exception
     * @return {@link ProblemDetail} containing standard body in case of errors
     */
    @ExceptionHandler(value = ServerWebInputException.class)
    public HttpEntity<ProblemDetail> handleServerWebInputException(final ServerWebInputException e,
                                                                   final ServerHttpRequest request) {

        LOGGER.debug("Request parameter is invalid: {}", e.getMessage());

        final ProblemDetail problem = new ProblemDetail("Parameter is invalid",
                Optional.ofNullable(e.getReason()).map(v -> v.split(":")[0]).orElseGet(e::getMessage));
        problem.setStatus(HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(problem, overrideContentType(), HttpStatus.BAD_REQUEST);
    }


    /**
     * Handles a case when validation of the request body fails
     *
     * @param e       any exception of type {@link MethodArgumentTypeMismatchException}
     * @param request request that produced the exception
     * @return {@link ProblemDetail} containing standard body in case of errors
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public HttpEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                                               final ServerHttpRequest request) {

        LOGGER.debug("Request body is invalid: {}", e.getMessage());

        final ProblemDetail problem = new ProblemDetail("Field type mismatch", null);
        problem.setStatus(HttpStatus.BAD_REQUEST.value());
        problem.setErrors(Collections.singletonList(
                new ProblemDetail("Wrong field value format",
                        String.format("Incorrect value '%s' for field '%s'. Expected value type '%s'", e.getValue(), e.getName(), e.getParameter().getParameterType().getTypeName()))));

        return new ResponseEntity<>(problem, overrideContentType(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles Access Denied case, when permissions is not enough to perform operation
     *
     * @param e       any exception of type {@link AccessDeniedException}
     * @param request the {@link ServerHttpRequest}
     * @return {@link ProblemDetail} containing standard body in case of errors
     */
    @ExceptionHandler(AccessDeniedException.class)
    public HttpEntity<ProblemDetail> handleAccessDenied(AccessDeniedException e,
                                                        final ServerHttpRequest request) {

        LOGGER.debug("Access denied: {}", e.getMessage());

        final ProblemDetail problem = new ProblemDetail("Access denied", e.getMessage());
        problem.setStatus(HttpStatus.UNAUTHORIZED.value());

        return new ResponseEntity<>(problem, overrideContentType(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles all unexpected situations
     *
     * @param e       any exception of type {@link Exception}
     * @param request request that produced the exception
     * @return {@link ProblemDetail} containing standard body in case of errors
     */
    @ExceptionHandler(Exception.class)
    public HttpEntity<ProblemDetail> handleException(Exception e,
                                                     final ServerHttpRequest request) {

        LOGGER.error("An unexpected error occurred", e);

        final ProblemDetail problem = new ProblemDetail("Internal Error", "An unexpected error has occurred");
        problem.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        return new ResponseEntity<>(problem, overrideContentType(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpHeaders overrideContentType() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", APPLICATION_PROBLEM_JSON);
        return httpHeaders;
    }

    private String wrapWithFieldName(ObjectError objectError) {
        String defaultMessage = objectError.getDefaultMessage();
        //put field name in case of Field Error
        if (objectError instanceof FieldError) {
            defaultMessage = ((FieldError) objectError).getField() + " " + objectError.getDefaultMessage();
        }
        return defaultMessage;
    }

    private List<ProblemDetail> extractErrors(Exception exception) {
        BindingResult bindingResult;
        if (exception instanceof BindException) {
            bindingResult = ((BindException) exception).getBindingResult();
        } else {
            bindingResult = ((MethodArgumentNotValidException) exception).getBindingResult();
        }
        return bindingResult.getAllErrors().stream()
                .map(objectError -> new ProblemDetail("Invalid Parameter", wrapWithFieldName(objectError)))
                .collect(Collectors.toList());
    }
}
