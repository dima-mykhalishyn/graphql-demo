package com.dmihalishin.graphql.reactive.spring.api.app.graphql.fetcher.task;

import akka.actor.ActorRef;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.commons.EmptyResponse;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.task.TaskDataRequest;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.task.TaskDataResponse;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.task.TaskEntity;
import com.dmihalishin.graphql.reactive.spring.api.app.util.AkkaUtils;
import com.dmihalishin.graphql.reactive.spring.api.datamodel.resources.graphql.StatusResource;
import com.dmihalishin.graphql.reactive.spring.api.datamodel.resources.graphql.TaskResource;
import com.dmihalishin.graphql.reactive.spring.api.datamodel.resources.graphql.TaskResponse;
import graphql.GraphQLException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.pattern.Patterns.ask;

@Slf4j
class BaseTaskFetcher {

    private static final String LOADER_ERROR = "Cannot load Task data for id \"%d\"";
    static final String KEY = "getTasks";

    private final ActorRef dataLoaderActor;

    protected BaseTaskFetcher(ActorRef dataLoaderActor) {
        this.dataLoaderActor = dataLoaderActor;
    }

    protected List<CompletableFuture<TaskResponse>> getAkkaResponses(List<Integer> graphQlRequest, Set<String> attributes) {
        final List<CompletableFuture<TaskResponse>> results = new ArrayList<>();
        graphQlRequest.forEach(id -> {
            final CompletionStage<TaskResource> completionStage = doRequestAndAskStage(id, attributes);
            results.add(completionStage.exceptionally(error -> {
                log.error(error.getMessage());
                throw new GraphQLException(String.format(LOADER_ERROR, id));
            }).thenApply(resource -> buildResponse(id, resource))
                    .toCompletableFuture());
        });
        return results;
    }

    private static TaskDataRequest createProductDataRequest(Integer id, Set<String> attributes) {
        TaskDataRequest.Builder builder = TaskDataRequest.newBuilder()
                .setTraceId(AkkaUtils.generateTraceUUID())
                .setId(id)
                .addAllAttributes(attributes);
        return builder.build();
    }

    private CompletionStage<TaskResource> doRequestAndAskStage(Integer id, Set<String> attributes) {
        TaskDataRequest dataRequest = createProductDataRequest(id, attributes);
        return ask(dataLoaderActor, dataRequest,
                Duration.ofMillis(1000))
                .thenApply(response -> {
                    if(response instanceof EmptyResponse) {
                        return null;
                    } else {
                        final TaskEntity entity = ((TaskDataResponse) response).getEntity();
                        StatusResource statusResource = null;
                        if (entity.hasStatus()) {
                            statusResource = new StatusResource(entity.getStatus().getCode(),
                                    entity.getStatus().getDescription());
                        }
                        return new TaskResource(entity.getId(), entity.getName(), statusResource);
                    }
                });
    }

    protected TaskResponse buildResponse(Integer id, TaskResource resource) {
        return TaskResponse.builder()
                .setKey(id)
                .setTask(resource)
                .build();
    }
}
