package com.dmihalishin.graphql.reactive.spring.api.app.graphql.fetcher;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.PropertyDataFetcher;
import graphql.schema.PropertyDataFetcherHelper;

import java.util.concurrent.CompletableFuture;

/**
 * Custom {@link PropertyDataFetcher} that allow to work
 * with {@link CompletableFuture} as a fetcher response.
 * This will allow to load array elements in distributed way
 *
 * @param <T> type of the property
 * @see PropertyDataFetcher
 */
public class CustomPropertyDataFetcher<T> extends PropertyDataFetcher<T> {

    /**
     * Default constructor
     *
     * @param propertyName the property name that must be fetched from the source.
     */
    public CustomPropertyDataFetcher(final String propertyName) {
        super(propertyName);
    }

    /**
     * {@inheritDoc}
     *
     * @see PropertyDataFetcher#get(DataFetchingEnvironment)
     */
    @Override
    @SuppressWarnings("unchecked")
    public T get(DataFetchingEnvironment environment) {
        Object source = environment.getSource();
        if (source instanceof CompletableFuture) {
            return (T) ((CompletableFuture<?>) source).thenApply(value -> {
                if (value == null) {
                    return null;
                } else {
                    return PropertyDataFetcherHelper.getPropertyValue(getPropertyName(), value, environment.getFieldType(), environment);
                }
            });
        } else {
            return super.get(environment);
        }
    }

    /**
     * Create new CustomPropertyDataFetcher for specific property
     *
     * @param propertyName property name that should be fetched from the source
     * @param <T>          type of the property
     * @return property value
     */
    public static <T> CustomPropertyDataFetcher<T> fetching(final String propertyName) {
        return new CustomPropertyDataFetcher<>(propertyName);
    }
}
