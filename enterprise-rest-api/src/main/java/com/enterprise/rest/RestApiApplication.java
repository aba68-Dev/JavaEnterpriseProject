package com.enterprise.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Enterprise REST API microservice entry point.
 *
 * This service exposes:
 *   - JAX-RS (Jersey) REST endpoints on port 8080
 *   - Kafka producers for Order, Product, and User events
 *   - Kafka consumers for event processing
 *
 * Security: OAuth2 Resource Server validating JWT tokens from the Gateway (port 9000)
 */
@SpringBootApplication(scanBasePackages = {
        "com.enterprise.rest",
        "com.enterprise.kafka",
        "com.enterprise.security"
})
@EntityScan(basePackages = "com.enterprise.domain.entity")
@EnableJpaRepositories(basePackages = "com.enterprise.repository")
@EnableKafka
public class RestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestApiApplication.class, args);
    }
}
