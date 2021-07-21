package com.dmihalishin.graphql.reactive.spring.api.app.service.akka;

import akka.actor.AbstractLoggingActor;
import com.dmihalishin.graphql.reactive.spring.api.app.config.akka.Actor;
import com.dmihalishin.graphql.reactive.spring.api.app.dao.TaskRepository;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.commons.EmptyResponse;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.commons.Reference;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.task.TaskDataRequest;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.task.TaskDataResponse;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.task.TaskEntity;
import lombok.RequiredArgsConstructor;

@Actor(TaskLoaderActor.NAME)
@RequiredArgsConstructor
public class TaskLoaderActor extends AbstractLoggingActor {

    public static final String NAME = "taskLoaderActor";
    public static final String ACTOR_NAME = "/user/taskLoaderActor";
    private final TaskRepository taskRepository;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TaskDataRequest.class, this::load)
                .matchAny(this::unhandled)
                .build();
    }

    private void load(final TaskDataRequest request) {
        log().debug("TaskLoaderActor {} : {}", context().self(), request.getTraceId());

        if (taskRepository.existsById(request.getId())) {
            final TaskEntity entity = TaskEntity.newBuilder()
                    .setId(request.getId())
                    .setName("Test " + request.getId())
                    .setStatus(Reference.newBuilder()
                            .setCode("DONE")
                            .setDescription("Done").build()).build();
            sender().tell(TaskDataResponse.newBuilder()
                    .setTraceId(request.getTraceId())
                    .setEntity(entity)
                    .build(), self());
        } else {
            sender().tell(EmptyResponse.newBuilder()
                    .setTraceId(request.getTraceId())
                    .build(), self());
        }
    }
}
