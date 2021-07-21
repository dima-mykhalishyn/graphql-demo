package com.dmihalishin.graphql.reactive.spring.api.datamodel.resources;

import com.dmihalishin.graphql.reactive.spring.api.datamodel.resources.graphql.TaskResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataResponse implements java.io.Serializable {
    private static final long serialVersionUID = -365455053321789119L;

    private List<TaskResponse> getTasks;
}
