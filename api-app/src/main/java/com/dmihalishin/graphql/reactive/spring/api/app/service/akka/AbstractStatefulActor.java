package com.dmihalishin.graphql.reactive.spring.api.app.service.akka;

import akka.actor.AbstractActorWithTimers;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import lombok.Getter;

import java.time.LocalDateTime;

public abstract class AbstractStatefulActor<T> extends AbstractActorWithTimers {

    protected final T state;

    AbstractStatefulActor(T state) {
        this.state = state;
    }

    /**
     * It check if it is required to retry some messages:
     * Get ActorState.RetryState and base on that state and retry configuration, retries are performed.
     * Template method:
     * Children should implement meaningful doCheckForRetries
     *
     * @param tick Tick message
     */
    void checkForRetries(CheckTick tick) {
        int resentMsgNumber = doCleanupOrRetries(state);
        if (resentMsgNumber > 0) {
            log().info("Actor: {}, checkForRetries. Number of re sent messages: {}", tick.getName(), resentMsgNumber);
        }
    }

    protected LoggingAdapter log() {
        return Logging.getLogger(this);
    }

    /**
     * Children should implement cleanup and retry logic here.
     *
     * @param state state used to do retry Checks. Cannot be {@code null}
     * @return amount of retries that was done
     */
    protected abstract int doCleanupOrRetries(T state);

    /**
     * Payload for messages triggering a check for retries
     */
    static class CheckTick {

        @Getter
        private String name;

        private LocalDateTime initialExecDate;

        /**
         * @param initialExecDate initial exec date
         */
        CheckTick(LocalDateTime initialExecDate, String name) {
            this.initialExecDate = initialExecDate;
            this.name = name;
        }
    }
}
