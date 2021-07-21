package com.dmihalishin.graphql.reactive.spring.api.app.config;

import com.dmihalishin.graphql.reactive.spring.api.app.graphql.fetcher.CustomPropertyDataFetcher;
import com.dmihalishin.graphql.reactive.spring.api.app.graphql.fetcher.GraphQLFetcher;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.execution.SubscriptionExecutionStrategy;
import graphql.scalars.ExtendedScalars;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.WiringFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

@Profile("!TestWithoutGraphQL")
@Configuration
public class GraphQLConfiguration {

    public static final String QUERY_KEY = "Query";
    public static final String SUBSCRIPTION_KEY = "Subscription";

    @Autowired
    private Collection<GraphQLFetcher<?>> fetchers;

    /**
     * Create GraphQL engine from Schema.
     *
     * @param resourceFile  schema file. Cannot be {@code null}
     * @param resourceFiles type definitions schema files. Cannot be {@code null}
     * @return {@code GraphQL} engine
     */
    @Bean
    GraphQL schema(
            @Value("classpath:schema.graphqls") final Resource resourceFile,
            @Value("classpath*:/{filename:[a-z\\-]+-model}.graphqls") final Resource[] resourceFiles
    ) {
        try {
            final GraphQLSchema graphQLSchema = buildSchema(resourceFile, resourceFiles);
            return GraphQL.newGraphQL(graphQLSchema)
                    .subscriptionExecutionStrategy(new SubscriptionExecutionStrategy())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Build {@link GraphQLSchema} from schema
     *
     * @param resourceFile  schema file. Cannot be {@code null}
     * @param resourceFiles type definitions schema files. Cannot be {@code null}
     * @return {@link GraphQLSchema}
     */
    private GraphQLSchema buildSchema(final Resource resourceFile, final Resource[] resourceFiles) throws IOException {

        final String sdl = StreamUtils.copyToString(resourceFile.getInputStream(), StandardCharsets.UTF_8);
        final SchemaParser schemaParser = new SchemaParser();
        final TypeDefinitionRegistry typeRegistry = schemaParser.parse(sdl);

        for (Resource schemaResource : resourceFiles) {
            String sdlContent = StreamUtils.copyToString(schemaResource.getInputStream(), StandardCharsets.UTF_8);
            TypeDefinitionRegistry typeDefinition = schemaParser.parse(sdlContent);
            typeRegistry.merge(typeDefinition);
        }

        final RuntimeWiring runtimeWiring = buildWiring();
        final SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        final WiringFactory wiringFactory = new WiringFactory() {
            @Override
            public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {
                return CustomPropertyDataFetcher.fetching(environment.getFieldDefinition().getName());
            }
        };
        RuntimeWiring.Builder builder = newRuntimeWiring()
                .wiringFactory(wiringFactory)
                // add Scalars => GraphQl Types to support Java Types
                .scalar(ExtendedScalars.Date)
                .scalar(ExtendedScalars.DateTime)
                .scalar(Scalars.GraphQLLong)
                .scalar(Scalars.GraphQLBigDecimal)
                .scalar(Scalars.GraphQLBigInteger);
        // add fetchers that will serve data for queries
        for (final GraphQLFetcher<?> fetcher : fetchers) {
            builder = builder.type(fetcher.getTypeWiring(), b -> b.dataFetcher(fetcher.getQueryKey(), fetcher));
        }
        return builder.build();
    }
}
