package com.dmihalishin.graphql.reactive.spring.api.app.graphql.fetcher.task;

import akka.actor.ActorRef;
import com.dmihalishin.graphql.reactive.spring.api.app.config.akka.AkkaConfig;
import com.dmihalishin.graphql.reactive.spring.api.app.graphql.fetcher.GraphQLDataFetcher;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.commons.EmptyResponse;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.task.TaskDataRequest;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.task.TaskDataResponse;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.dto.task.TaskEntity;
import com.dmihalishin.graphql.reactive.spring.api.app.util.AkkaUtils;
import com.dmihalishin.graphql.reactive.spring.api.datamodel.resources.graphql.StatusResource;
import com.dmihalishin.graphql.reactive.spring.api.datamodel.resources.graphql.TaskResource;
import com.dmihalishin.graphql.reactive.spring.api.datamodel.resources.graphql.TaskResponse;
import graphql.GraphQLException;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.pattern.Patterns.ask;

@Profile("!TestWithoutGraphQL")
@Component
@Slf4j
public class TaskDataFetcher extends BaseTaskFetcher implements GraphQLDataFetcher<TaskResponse> {
    private static final String REQUEST_NAME = "ids";

    public TaskDataFetcher(@Qualifier(AkkaConfig.DATA_ACTOR_REF_NAME) ActorRef dataLoaderActor) {
        super(dataLoaderActor);
    }

    @Override
    public String getQueryKey() {
        return KEY;
    }

    @Override
    public CompletionStage<DataFetcherResult<List<CompletableFuture<TaskResponse>>>> get(DataFetchingEnvironment environment) throws Exception {
        final List<Integer> graphQlRequest = environment.getArgument(REQUEST_NAME);
        final Set<String> attributes = environment.getSelectionSet().getArguments().keySet();

        final DataFetcherResult.Builder<List<CompletableFuture<TaskResponse>>> builder = DataFetcherResult.newResult();

        final List<CompletableFuture<TaskResponse>> results = getAkkaResponses(graphQlRequest, attributes);
        builder.data(results);
        return Mono.just(builder.build()).toFuture();
    }
}
