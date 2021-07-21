package com.dmihalishin.graphql.reactive.spring.api.app.graphql.fetcher.task;

import akka.actor.ActorRef;
import com.dmihalishin.graphql.reactive.spring.api.app.config.akka.AkkaConfig;
import com.dmihalishin.graphql.reactive.spring.api.app.graphql.fetcher.GraphQLSubscriptionFetcher;
import com.dmihalishin.graphql.reactive.spring.api.datamodel.resources.graphql.TaskResponse;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Profile("!TestWithoutGraphQL")
@Component
@Slf4j
public class TaskSubscriptionFetcher extends BaseTaskFetcher implements GraphQLSubscriptionFetcher<TaskResponse> {

    public TaskSubscriptionFetcher(@Qualifier(AkkaConfig.DATA_ACTOR_REF_NAME) ActorRef dataLoaderActor) {
        super(dataLoaderActor);
    }

    @Override
    public String getQueryKey() {
        return KEY;
    }

    @Override
    public Publisher<DataFetcherResult<TaskResponse>> get(DataFetchingEnvironment environment) {
        final Set<String> attributeList = environment
                .getSelectionSet()
                .getArguments()
                .keySet();

        // return publisher from akka responses
        return Flux.fromIterable(getAkkaResponses(IntStream.range(50, 60).boxed().collect(Collectors.toList()), attributeList))
                .flatMap(Mono::fromFuture)
                .map(response -> {
                    DataFetcherResult.Builder<TaskResponse> builder = DataFetcherResult.newResult();
                    return builder
                            .data(response)
                            .build();
                });
    }

}
