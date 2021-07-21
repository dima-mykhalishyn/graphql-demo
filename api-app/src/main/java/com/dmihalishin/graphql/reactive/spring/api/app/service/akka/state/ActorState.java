package com.dmihalishin.graphql.reactive.spring.api.app.service.akka.state;

import akka.actor.ActorRef;
import akka.event.LoggingAdapter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Actor state
 *
 * @param <T> state type
 */
public abstract class ActorState<T> {

    private final ActorRef target;
    private final String traceUuid;
    private final LocalDateTime created;
    final Map<String, RetryableRequest> requests;

    ActorState(final ActorRef target, final String traceUuid) {
        this.target = target;
        this.traceUuid = traceUuid;
        this.created = LocalDateTime.now();
        this.requests = new HashMap<>();
    }

    /**
     * Handle empty response, that come for specific request ID.
     *
     * @param requestId request id. Cannot be {@code null}
     */
    public void emptyResponse(final String requestId) {
        requests.remove(requestId);
        logger().debug("Empty Request processing: waiting {}", requests.size());
    }

    /**
     * Add request ID to the list, to expected response for it
     *
     * @param requestId request id. Cannot be {@code null}
     * @param request   {@code RetryableRequest} that could be used in Retry Logic.
     *                  Cannot be {@code null}
     */
    public void expectResponse(final String requestId, final RetryableRequest request) {
        requests.put(requestId, request);
    }

    public Map<String, RetryableRequest> getRequests() {
        return Collections.unmodifiableMap(requests);
    }

    /**
     * Process response data
     *
     * @param requestId request id. Cannot be {@code null}
     * @param data      response data. Can be {@code null}
     */
    public void addData(final String requestId, final T data) {
        requests.remove(requestId);
        logger().debug("Request processing: waiting {}", requests.size());
        if (data != null) {
            this.updateState(data);
        }
    }

    /**
     * Check State and send results if complete.
     *
     * @return return {@code true} if state not waiting any responses anymore. Otherwise {@code false}
     */
    public boolean isDone() {
        if (requests.isEmpty()) {
            logger().debug("Done. Sending result {}", ChronoUnit.MILLIS.between(created, LocalDateTime.now()));
            sendResults(target, traceUuid);
        }

        return requests.isEmpty();
    }

    /**
     * Retrieves logger. Cannot return {@code null}
     *
     * @return return {@code LoggingAdapter}
     */
    protected abstract LoggingAdapter logger();

    /**
     * Update state with data
     *
     * @param data response data. Cannot be {@code null}
     */
    protected abstract void updateState(final T data);

    /**
     * Send results to the target actor
     *
     * @param target    target actor. Cannot be {@code null}
     * @param traceUuid trace UUID of request. Cannot be {@code null}
     */
    protected abstract void sendResults(ActorRef target, String traceUuid);

    /**
     * Retrieves state created date.
     *
     * @return state created date.
     */
    public LocalDateTime getCreated() {
        return created;
    }
}
