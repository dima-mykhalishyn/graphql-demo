package com.dmihalishin.graphql.reactive.spring.api.app.rest;

import com.dmihalishin.graphql.reactive.spring.api.app.dao.TaskRepository;
import com.dmihalishin.graphql.reactive.spring.api.app.exceptions.DeserializationException;
import com.dmihalishin.graphql.reactive.spring.api.app.graphql.GraphQLInvocation;
import com.dmihalishin.graphql.reactive.spring.api.datamodel.resources.GraphQLRequestBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@ExposesResourceFor(ExecutionResult.class)
public class GraphQLController implements DataController {

    static final String APPLICATION_GRAPHQL_VALUE = "application/graphql";

    private final GraphQLInvocation graphQLInvocation;

    private final ObjectMapper objectMapper;

    private final TaskRepository taskRepository;

    /**
     * Constructor for the class
     *
     * @param graphQLInvocation {@link GraphQLInvocation} object that will invoke GraphQL request
     * @param objectMapper      Jackson object mapper
     */
    @Autowired
    public GraphQLController(GraphQLInvocation graphQLInvocation, ObjectMapper objectMapper,
                             final TaskRepository taskRepository) {
        this.graphQLInvocation = graphQLInvocation;
        this.objectMapper = objectMapper;
        this.taskRepository = taskRepository;
    }

    /**
     * {@inheritDoc}
     *
     * @see DataController#getData(String, String, String, UserDetails)
     * @see <a href="https://graphql.org/learn/serving-over-http/#get-request">GraphQL Get Request</a>
     */
    @GetMapping(value = "/graphql",
            produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    @SuppressWarnings("unchecked")
    @Override
    public Flux<Map<String, Object>> getData(
            final String query,
            final String operationName,
            final String variablesJson,
            @AuthenticationPrincipal UserDetails userDetails) {

        final GraphQLRequestBody requestBody = new GraphQLRequestBody(query,
                operationName,
                StringUtils.isBlank(variablesJson) ? null :
                        deserialize(variablesJson, Map.class, "Invalid `variables` parameter. Please check specification."));
        return executeRequest(requestBody, userDetails);
    }

    /**
     * {@inheritDoc}
     *
     * @see DataController#getData(String, String, UserDetails)
     * @see <a href="https://graphql.org/learn/serving-over-http/#post-request">GraphQL Post Request</a>
     */
    @PostMapping(value = "/graphql",
            consumes = {APPLICATION_GRAPHQL_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    @Override
    public Flux<Map<String, Object>> getData(
            @RequestHeader(value = HttpHeaders.CONTENT_TYPE) String contentType,
            @RequestBody String body,
            @AuthenticationPrincipal UserDetails userDetails) {

        final MediaType mediaType = Optional.ofNullable(contentType)
                .map(MediaType::parseMediaType).orElse(MediaType.APPLICATION_JSON);
        GraphQLRequestBody requestBody;
        if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(mediaType)) {
            requestBody = deserialize(body, GraphQLRequestBody.class, "Wrong Request Body. Please check specification.");
        } else {
            // * If the "application/graphql" Content-Type header is present,
            //   treat the HTTP POST body contents as the GraphQL query string.
            requestBody = new GraphQLRequestBody(body, null, null);
        }
        return executeRequest(requestBody, userDetails);
    }

    @GetMapping(value = "/task/count",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<HashMap<String, Long>> createTask() {
        final long size = taskRepository.count();
        return Mono.just(new HashMap<>() {{
            put("count", size);
        }});
    }

    private Flux<Map<String, Object>> executeRequest(final GraphQLRequestBody requestBody,
                                                     final UserDetails userDetails) {
        return graphQLInvocation.invoke(requestBody, userDetails);
    }

    private <T> T deserialize(final String json,
                              final Class<T> requiredType,
                              final String errorMessage) {
        try {
            return objectMapper.readValue(json, requiredType);
        } catch (IOException e) {
            throw new DeserializationException(errorMessage, e);
        }
    }
}
