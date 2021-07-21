package com.dmihalishin.graphql.reactive.spring.api.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class RetryableConfiguration {
}
