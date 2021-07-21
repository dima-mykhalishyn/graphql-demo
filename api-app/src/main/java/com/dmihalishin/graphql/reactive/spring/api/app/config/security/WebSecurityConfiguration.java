package com.dmihalishin.graphql.reactive.spring.api.app.config.security;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Setter
@Configuration
@ConfigurationProperties(prefix = "security")
public class WebSecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChainOpen(
            final CorsWebFilter corsWebFilter,
            final ServerHttpSecurity http) {
        final ServerHttpSecurity security = http.csrf().disable()
                .formLogin().disable()
                .httpBasic().disable();
        return security.authorizeExchange()
                .anyExchange().permitAll().and()
                .addFilterAt(corsWebFilter, SecurityWebFiltersOrder.CORS)
                .build();
    }

    @Bean
    CorsWebFilter corsWebFilter() {
        final CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedHeader(CorsConfiguration.ALL);
        corsConfig.addAllowedOrigin(CorsConfiguration.ALL);
        corsConfig.setMaxAge(3600L);
        corsConfig.addAllowedMethod("GET");
        corsConfig.addAllowedMethod("POST");
        corsConfig.addAllowedMethod("OPTIONS");

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
