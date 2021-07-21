package com.dmihalishin.graphql.reactive.spring.api.app.service.akka;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import com.dmihalishin.graphql.reactive.spring.api.app.config.akka.Actor;
import com.dmihalishin.graphql.reactive.spring.api.app.config.akka.AkkaConfig;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.commons.EmptyResponse;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.task.TaskDataRequest;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.task.TaskDataResponse;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.state.DataState;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.state.RetryableRequest;
import com.dmihalishin.graphql.reactive.spring.api.app.util.AkkaUtils;
import com.google.protobuf.GeneratedMessageV3;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Data Loader Actor.
 * Actor load data from different actors base on the user request.
 *
 * @see AbstractLoggingActor
 */
@Actor
public class DataLoaderActor extends AbstractStatefulActor<Map<String, DataState>> {

    public static final String NAME = "dataLoaderActor";

    private final ActorRef taskLoaderActor;

    @Autowired
    public DataLoaderActor(@Qualifier(AkkaConfig.TASK_ROUTER_NAME) final ActorRef taskLoaderActor) {
        super(new HashMap<>());
        this.taskLoaderActor = taskLoaderActor;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        // start ticker
        getTimers().startPeriodicTimer(
                "DataLoaderActor_TICK",
                new CheckTick(LocalDateTime.now(), this.getSelf().path().toString()),
                java.time.Duration.ofMillis(300));
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractLoggingActor#createReceive()
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // task logic
                .match(TaskDataRequest.class, this::processTaskDataRequest)
                .match(TaskDataResponse.class, this::processTaskDataResponse)
                // common
                .match(EmptyResponse.class, this::processEmptyResponse)
                .match(CheckTick.class, this::checkForRetries)
                .matchAny(this::unhandled)
                .build();
    }

    @Override
    public int doCleanupOrRetries(final Map<String, DataState> state) {
        final MutableInt counter = new MutableInt(0);
        final LocalDateTime now = LocalDateTime.now();
        final List<String> removeKeys = new ArrayList<>();
        state.forEach((key, value) -> {
            // if state created date + max state TTL less the current date, then it must be removed
            if (now.isAfter(
                    value.getCreated().plus(1000, ChronoUnit.MILLIS)
            )) {
                log().warning("Cleaning up state {}", key);
                removeKeys.add(key);
            }

            value.getRequests().forEach((requestId, request) -> {
                final LocalDateTime maxRequestTime = value.getCreated().plus(100, ChronoUnit.MILLIS);
                final LocalDateTime maxResponseTime = value.getCreated().plus(request.getMaxResponseTime(), ChronoUnit.MILLIS);
                // if max request time not exceeded and state not done after delay,
                // then we resent requests that was not processed
                if (now.isBefore(maxRequestTime) && now.isAfter(maxResponseTime)) {
                    request.getCall().accept(self());
                    counter.increment();
                }
            });
        });
        removeKeys.forEach(state::remove);
        return counter.getValue();
    }

    private void processTaskDataRequest(final TaskDataRequest request) {
        log().debug("DataLoaderActor.processProductDataRequest {} : {}", context().self(), request.getTraceId());
        processDataRequest(request,
                taskLoaderActor,
                100,
                TaskDataRequest::getTraceId);
    }

    private void processTaskDataResponse(final TaskDataResponse response) {
        log().debug("DataLoaderActor.processProductDataResponse {} : {}", context().self(), response.getTraceId());
        AkkaUtils.consumeResponseWithStateId(response, response.getTraceId(), this.state,
                (TaskDataResponse resp, DataState actorState) -> actorState.addData(resp.getTraceId(), resp));
    }

    private <R extends GeneratedMessageV3> void processDataRequest(final R request,
                                                                   final ActorRef loaderActor,
                                                                   final Integer delay,
                                                                   java.util.function.Function<R, String> traceIdFunction) {
        final String traceId = traceIdFunction.apply(request);
        final DataState dataState = new DataState(this, sender(), traceId);
        this.state.put(traceId, dataState);
        final Consumer<ActorRef> call = ref -> loaderActor.tell(request, ref);
        // ask for data
        call.accept(self());
        // expect response
        dataState.expectResponse(traceId, new RetryableRequest(delay, call));
    }

    private void processEmptyResponse(final EmptyResponse response) {
        log().debug("DataLoaderActor.processEmptyResponse {} : {}", context().self(), response.getTraceId());
        AkkaUtils.consumeResponseWithStateId(response, response.getTraceId(), this.state,
                (EmptyResponse resp, DataState actorState) ->
                        actorState.emptyResponse(resp.getTraceId()));
    }
}

