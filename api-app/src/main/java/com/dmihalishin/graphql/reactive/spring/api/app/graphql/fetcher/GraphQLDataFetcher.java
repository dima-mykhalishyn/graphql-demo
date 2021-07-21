package com.dmihalishin.graphql.reactive.spring.api.app.graphql.fetcher;

import com.dmihalishin.graphql.reactive.spring.api.app.config.GraphQLConfiguration;
import graphql.execution.DataFetcherResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * GraphQL Fetcher that return data as {@link CompletionStage}
 * to not block anything
 *
 * @param <T> T response type
 * @see GraphQLFetcher
 */
public interface GraphQLDataFetcher<T> extends GraphQLFetcher<CompletionStage<DataFetcherResult<List<CompletableFuture<T>>>>> {

    /**
     * Retrieves TypeWiring key that will be used for such Fetcher
     *
     * @return Type Wiring key
     */
    default String getTypeWiring() {
        return GraphQLConfiguration.QUERY_KEY;
    }

}
