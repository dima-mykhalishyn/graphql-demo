package com.dmihalishin.graphql.reactive.spring.api.datamodel.resources;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphQLResponse implements java.io.Serializable {
    private static final long serialVersionUID = -1679757221012113290L;

    private List<GraphQLError> errors;
    private DataResponse data;
}
