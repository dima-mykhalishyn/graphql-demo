package com.dmihalishin.graphql.reactive.spring.api.datamodel.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphQLRequestBody implements java.io.Serializable {
    private static final long serialVersionUID = -1452763308350382117L;

    private String query;
    private String operationName;
    private Map<String, Object> variables;
}
