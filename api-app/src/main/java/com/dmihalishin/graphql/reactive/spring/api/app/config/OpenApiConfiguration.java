package com.dmihalishin.graphql.reactive.spring.api.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings({"unused"})
@Configuration
public class OpenApiConfiguration {
    private static final String DEFAULT_REST_PACKAGE = "com.dmihalishin.graphql.reactive.spring.api.app.rest";

    private static final String API_INFO_TITLE = "Demo GraphQL API";

    private static final String API_INFO_VERSION = "0.0.1";

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group(this.applicationName)
                .packagesToScan(DEFAULT_REST_PACKAGE)
                .build();
    }

    @Bean
    public OpenAPI unsecuredOpenApi() {
        return new OpenAPI()
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title(API_INFO_TITLE)
                .description("DEMO")
                .version(API_INFO_VERSION);
    }
}
