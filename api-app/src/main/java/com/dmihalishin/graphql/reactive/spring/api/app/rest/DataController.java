package com.dmihalishin.graphql.reactive.spring.api.app.rest;

import com.dmihalishin.graphql.reactive.spring.api.datamodel.resources.GraphQLResponse;
import com.dmihalishin.graphql.reactive.spring.api.datamodel.resources.ProblemDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON_VALUE;

/**
 * Data Controller specification
 */
@Tag(name = "DEMO", description = "Demo GraphQL API")
@RestController
public interface DataController {

    /**
     * Retrieve Data using GET HTTP method
     *
     * @param query         GraphQL query. Can be {@code blank}
     * @param operationName GraphQL operation name. Can be {@code blank}
     * @param variablesJson GraphQL variables as JSON. Can be {@code blank}
     * @param userDetails   User details of authorized user. Can be {@code null}
     * @return data
     */
    @Operation(method = "GET",
            summary = "Demo GraphQL API",
            description = "Allow to get data from the API",
            operationId = "graphqlGet")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(mediaType = APPLICATION_STREAM_JSON_VALUE,
                    schema = @Schema(implementation = GraphQLResponse.class, example = "{\n" +
                            "    getTasks(ids: [123,1,10,56]){\n" +
                            "        key \n" +
                            "        task {\n" +
                            "            id\n" +
                            "            name\n" +
                            "            status {\n" +
                            "                code\n" +
                            "            }\n" +
                            "        }\n" +
                            "    }\n" +
                            "}")))
    @ApiResponse(responseCode = "400", description = "Bad request",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "This operation is forbidden for this user",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected Internal Server Error",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    Flux<Map<String, Object>> getData(
            @RequestParam("query") String query,
            @RequestParam(value = "operationName", required = false) String operationName,
            @RequestParam(value = "variables", required = false) String variablesJson,
            @Parameter(hidden = true) UserDetails userDetails);

    /**
     * Retrieve Data using POST HTTP method
     *
     * @param contentType content type. Cannot be {@code null}
     * @param body        request Body. Can be {@code null}
     * @param userDetails User details of authorized user. Can be {@code null}
     * @return Data
     */
    @Operation(method = "POST",
            summary = "GraphQL API",
            description = "Allow to get data from the API",
            operationId = "graphqlPost")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(mediaType = APPLICATION_STREAM_JSON_VALUE,
                    schema = @Schema(implementation = GraphQLResponse.class, example = "{\n" +
                            "    getTasks(ids: [123,1,10,56]){\n" +
                            "        key \n" +
                            "        task {\n" +
                            "            id\n" +
                            "            name\n" +
                            "            status {\n" +
                            "                code\n" +
                            "            }\n" +
                            "        }\n" +
                            "    }\n" +
                            "}")))
    @ApiResponse(responseCode = "400", description = "Bad request",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "This operation is forbidden for this user",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected Internal Server Error",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    Flux<Map<String, Object>> getData(@Parameter(hidden = true) String contentType,
                                      @RequestBody(content = @Content(mediaType = "", examples = @ExampleObject("{\n" +
                                              "    getTasks(ids: [123,1,10,56]){\n" +
                                              "        key \n" +
                                              "        task {\n" +
                                              "            id\n" +
                                              "            name\n" +
                                              "            status {\n" +
                                              "                code\n" +
                                              "            }\n" +
                                              "        }\n" +
                                              "    }\n" +
                                              "}"))) String body,
                                      @Parameter(hidden = true) UserDetails userDetails);

}
