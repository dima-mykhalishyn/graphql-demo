package com.dmihalishin.graphql.reactive.spring.api.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration class for maximum request size
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "config.query")
public class GraphQLRequestConfiguration {

    private Map<String, EntityQuerySize> size;

    @Data
    public static class EntityQuerySize {

        private int maxIds;

        private int minIds;

        private int minAttributes;
    }

}
