package com.dmihalishin.graphql.reactive.spring.api.datamodel.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ProblemDetail",
        description = "Resource used to explain detailed problem in response body.")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProblemDetail implements java.io.Serializable {

    private static final long serialVersionUID = 5677480745800414626L;

    public ProblemDetail(String title, String detail) {
        this.title = title;
        this.detail = detail;
    }

    private String title;

    private String detail;

    private Integer status;

    private List<ProblemDetail> errors;

}
