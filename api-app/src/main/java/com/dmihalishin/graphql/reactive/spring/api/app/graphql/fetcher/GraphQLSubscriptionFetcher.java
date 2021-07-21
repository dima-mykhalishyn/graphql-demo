package com.dmihalishin.graphql.reactive.spring.api.app.graphql.fetcher;

import com.dmihalishin.graphql.reactive.spring.api.app.config.GraphQLConfiguration;
import graphql.execution.DataFetcherResult;
import org.reactivestreams.Publisher;

/**
 * GraphQL Fetcher that return data as {@link Publisher} for subscription requests
 *
 * @param <T> T response type
 * @see GraphQLFetcher
 */
public interface GraphQLSubscriptionFetcher<T> extends GraphQLFetcher<Publisher<DataFetcherResult<T>>> {

    /**
     * Retrieves TypeWiring key that will be used for such Fetcher
     *
     * @return Type Wiring key
     */
    default String getTypeWiring() {
        return GraphQLConfiguration.SUBSCRIPTION_KEY;
    }

}
