package com.enterprise.soap.endpoint;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

import java.math.BigDecimal;
import java.util.List;

/**
 * JAX-WS WSDL contract (SEI — Service Endpoint Interface) for the Product SOAP service.
 *
 * Published WSDL will be available at:
 *   http://localhost:8081/ws/product?wsdl
 */
@WebService(name = "ProductWebService", targetNamespace = "http://enterprise.com/soap/product")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL)
public interface ProductWebService {

    @WebMethod(operationName = "getProductById")
    @WebResult(name = "product")
    ProductSoapResponse getProductById(@WebParam(name = "productId") Long productId);

    @WebMethod(operationName = "getProductBySku")
    @WebResult(name = "product")
    ProductSoapResponse getProductBySku(@WebParam(name = "sku") String sku);

    @WebMethod(operationName = "searchProducts")
    @WebResult(name = "products")
    List<ProductSoapResponse> searchProducts(
            @WebParam(name = "categoryId") Long categoryId,
            @WebParam(name = "minPrice")   BigDecimal minPrice,
            @WebParam(name = "maxPrice")   BigDecimal maxPrice,
            @WebParam(name = "keyword")    String keyword);

    @WebMethod(operationName = "updateProductStock")
    @WebResult(name = "success")
    boolean updateProductStock(
            @WebParam(name = "productId") Long productId,
            @WebParam(name = "quantity")  int quantity);
}
