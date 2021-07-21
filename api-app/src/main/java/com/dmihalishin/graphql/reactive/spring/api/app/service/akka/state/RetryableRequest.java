package com.dmihalishin.graphql.reactive.spring.api.app.service.akka.state;

import akka.actor.ActorRef;
import lombok.Getter;

import java.util.function.Consumer;

/**
 * Retryable Request
 */
@Getter
public class RetryableRequest {

    private final int maxResponseTime;
    private final Consumer<ActorRef> call;

    /**
     * Default constructor
     *
     * @param maxResponseTime max response time in milliseconds for this call, before retry.
     * @param call            method call. Cannot be {@code null}
     */
    public RetryableRequest(int maxResponseTime, Consumer<ActorRef> call) {
        this.maxResponseTime = maxResponseTime;
        this.call = call;
    }
}
