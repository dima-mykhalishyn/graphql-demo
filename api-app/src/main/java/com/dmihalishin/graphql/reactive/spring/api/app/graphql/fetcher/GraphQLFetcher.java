package com.dmihalishin.graphql.reactive.spring.api.app.graphql.fetcher;

import com.dmihalishin.graphql.reactive.spring.api.app.exceptions.DeserializationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

/**
 * GraphQL Fetcher for both regular and subscription queries
 *
 * @param <T> T response type
 * @see DataFetcher
 */
public interface GraphQLFetcher<T> extends DataFetcher<T> {

    /**
     * Retrieves Query key that will be used for such Fetcher
     *
     * @return query key
     */
    String getQueryKey();

    /**
     * Retrieves TypeWiring key that will be used for such Fetcher
     *
     * @return Type Wiring key
     */
    String getTypeWiring();

    /**
     * Deserialize GraphQL Request attributes for specific type.
     *
     * @param objectMapper  object mapper. Cannot be {@code null}
     * @param environment   environment. Cannot be {@code null}
     * @param requestName   name of the request parameter
     * @param typeReference {@link TypeReference} of the request
     * @param <U>           type
     * @return converted object.
     */
    default <U> U deserialize(final ObjectMapper objectMapper,
                              final DataFetchingEnvironment environment,
                              final String requestName,
                              final TypeReference<U> typeReference) {
        try {
            final Object request = environment.getArgument(requestName);
            return objectMapper.convertValue(request, typeReference);
        } catch (IllegalArgumentException e) {
            throw new DeserializationException("Error deserializing object from Map: " + e.getMessage(), e);
        }
    }

}
