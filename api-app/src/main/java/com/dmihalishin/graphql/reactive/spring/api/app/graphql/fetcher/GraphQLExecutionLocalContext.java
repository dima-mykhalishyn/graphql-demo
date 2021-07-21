package com.dmihalishin.graphql.reactive.spring.api.app.graphql.fetcher;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for holding GraphQL requests context values.
 */
@Data
@Builder
public class GraphQLExecutionLocalContext {

    private double totalCapacity;

}
