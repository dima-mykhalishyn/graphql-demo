package com.dmihalishin.graphql.reactive.spring.api.app.service.akka.state;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.DataLoaderActor;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.commons.EmptyResponse;

import java.util.Objects;

/**
 * Data Actor State that will contains all needed data from response
 *
 * @see ActorState
 */
public class DataState extends ActorState<Object> {

    private final DataLoaderActor dataLoaderActor;

    private Object result;

    /**
     * Data State constructor
     *
     * @param dataLoaderActor data Loader Actor. Cannot be {@code null}
     * @param target          target actor. Cannot be {@code null}
     * @param traceUuid       trace UUID. Cannot be {@code null}
     */
    public DataState(final DataLoaderActor dataLoaderActor,
                     final ActorRef target,
                     final String traceUuid) {
        super(target, traceUuid);
        this.dataLoaderActor = dataLoaderActor;
    }

    /**
     * {@inheritDoc}
     *
     * @see ActorState#logger()
     */
    @Override
    protected LoggingAdapter logger() {
        return Logging.getLogger(dataLoaderActor);
    }

    /**
     * {@inheritDoc}
     *
     * @see ActorState#updateState(Object)
     */
    @Override
    protected void updateState(final Object data) {
        result = data;
    }

    /**
     * {@inheritDoc}
     *
     * @see ActorState#sendResults(ActorRef, String)
     */
    @Override
    protected void sendResults(ActorRef target, String traceUuid) {
        target.tell(Objects.requireNonNullElseGet(
                result,
                () -> EmptyResponse.newBuilder().setTraceId(traceUuid).build()),
                dataLoaderActor.self());
    }
}
