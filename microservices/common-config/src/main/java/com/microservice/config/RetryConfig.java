package com.microservice.config;


import lombok.Data;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@Configuration
@ConfigurationProperties(prefix = "retry-config")
public class RetryConfig {
}
