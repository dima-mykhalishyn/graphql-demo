package com.dmihalishin.graphql.reactive.spring.api.app.graphql;

import com.dmihalishin.graphql.reactive.spring.api.app.graphql.fetcher.GraphQLExecutionLocalContext;
import com.dmihalishin.graphql.reactive.spring.api.datamodel.resources.GraphQLRequestBody;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dataloader.DataLoaderRegistry;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * GraphQL Invocation
 */
@Profile("!TestWithoutGraphQL")
@Component
@Slf4j
public class GraphQLInvocation {

    private static final double TOTAL_CONTEXT_SIZE_CAPACITY = 100.0;

    private final GraphQL graphQL;

    private final DataLoaderRegistry dataLoaderRegistry;

    /**
     * GraphQLInvocation constructor
     *
     * @param graphQL            GraphQL engine. Cannot be @{code null}
     * @param dataLoaderRegistry data loader registry. Can be {@code null}
     */
    public GraphQLInvocation(final GraphQL graphQL,
                             @Autowired(required = false) final DataLoaderRegistry dataLoaderRegistry) {
        this.graphQL = graphQL;
        this.dataLoaderRegistry = dataLoaderRegistry;
    }

    /**
     * Invoke GraphQL request
     *
     * @param requestBody request body. Cannot be @{code null}
     * @param userDetails {@code UserDetails} of authorized user. Can be {@code null}
     * @return {@code Mono} with response as Map
     */
    public Flux<Map<String, Object>> invoke(final GraphQLRequestBody requestBody,
                                            final UserDetails userDetails) {
        final ExecutionInput.Builder executionInputBuilder = ExecutionInput.newExecutionInput()
                .query(StringUtils.trimToEmpty(requestBody.getQuery()))
                .operationName(requestBody.getOperationName())
                .variables(Optional.ofNullable(requestBody.getVariables()).orElseGet(HashMap::new));
        if (dataLoaderRegistry != null) {
            executionInputBuilder.dataLoaderRegistry(dataLoaderRegistry);
        }
        executionInputBuilder.context(userDetails);
        executionInputBuilder.localContext(GraphQLExecutionLocalContext.builder().totalCapacity(TOTAL_CONTEXT_SIZE_CAPACITY).build());
        final ExecutionInput executionInput = executionInputBuilder.build();
        return getMapFlux(executionInput);
    }

    private Flux<Map<String, Object>> getMapFlux(ExecutionInput executionInput) {
        final CompletableFuture<ExecutionResult> future = graphQL.executeAsync(executionInput);
        return Mono.fromFuture(future).flatMapMany(executionResult -> {
            if (executionResult.getData() instanceof Publisher) {
                Publisher<ExecutionResult> stream = executionResult.getData();
                return Flux.from(stream).map(ExecutionResult::toSpecification);
            } else {
                return Flux.just(executionResult.toSpecification());
            }
        });
    }
}
