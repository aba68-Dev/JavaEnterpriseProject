package com.enterprise.soap.config;

import com.enterprise.soap.endpoint.ProductWebServiceImpl;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.xml.ws.Endpoint; // import javax.xml.ws.Endpoint;

/**
 * Apache CXF configuration — publishes the JAX-WS SOAP endpoint.
 *
 * The WSDL contract is auto-generated at: /ws/product?wsdl
 */
@Configuration
public class CxfConfig {

    private final Bus cxfBus;
    private final ProductWebServiceImpl productWebService;

    public CxfConfig(Bus cxfBus, ProductWebServiceImpl productWebService) {
        this.cxfBus         = cxfBus;
        this.productWebService = productWebService;
    }

    @Bean
    public Endpoint productEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(cxfBus, productWebService);
        endpoint.publish("/product");
        return endpoint;
    }
}
