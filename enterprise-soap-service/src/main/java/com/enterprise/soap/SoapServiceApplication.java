package com.enterprise.soap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * SOAP Web Service microservice entry point.
 *
 * Exposes:
 *   - ProductWebService (JAX-WS / CXF) at port 8081
 *   - WSDL: http://localhost:8081/ws/product?wsdl
 *
 * Intended for B2B partners requiring XML-over-HTTP integration.
 */
@SpringBootApplication(scanBasePackages = {
        "com.enterprise.soap"
})
@EntityScan(basePackages = "com.enterprise.domain.entity")
@EnableJpaRepositories(basePackages = "com.enterprise.repository")
public class SoapServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoapServiceApplication.class, args);
    }
}
