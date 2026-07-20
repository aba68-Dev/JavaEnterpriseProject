package com.enterprise.rest.resource;

import com.enterprise.common.model.ApiResponse;
import com.enterprise.common.model.PagedResponse;
import com.enterprise.domain.dto.ProductDto;
import com.enterprise.rest.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;

import static com.enterprise.common.constants.AppConstants.DEFAULT_PAGE_SIZE;

/**
 * JAX-RS resource for the /api/v1/products endpoint.
 *
 * HTTP Protocol:
 *   GET    /products          — list with pagination, filtering
 *   GET    /products/{id}     — get by ID
 *   GET    /products/sku/{sku}— get by SKU
 *   POST   /products          — create (SCOPE_write)
 *   PUT    /products/{id}     — full update (SCOPE_write)
 *   DELETE /products/{id}     — soft-delete (SCOPE_write)
 *   PATCH  /products/{id}/restock — increase stock (SCOPE_write)
 *
 * Design Pattern: Resource (REST) — thin HTTP adapter delegating to ProductService.
 */
@Component
@Path("/api/v1/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Products", description = "Product catalogue management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ProductResource {

    private final ProductService productService;

    @GET
    @Operation(summary = "List products", description = "Returns a paginated, filterable list of active products")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Response list(
            @QueryParam("page")       @DefaultValue("0")  int page,
            @QueryParam("size")       @DefaultValue("" + DEFAULT_PAGE_SIZE) int size,
            @QueryParam("categoryId") Long categoryId,
            @QueryParam("minPrice")   BigDecimal minPrice,
            @QueryParam("maxPrice")   BigDecimal maxPrice,
            @QueryParam("search")     String search) {

        PagedResponse<ProductDto> result =
                productService.findAll(page, size, categoryId, minPrice, maxPrice, search);
        return Response.ok(ApiResponse.success(result)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get product by ID")
    public Response getById(@PathParam("id") Long id) {
        return Response.ok(ApiResponse.success(productService.findById(id))).build();
    }

    @GET
    @Path("/sku/{sku}")
    @Operation(summary = "Get product by SKU")
    public Response getBySku(@PathParam("sku") String sku) {
        return Response.ok(ApiResponse.success(productService.findBySku(sku))).build();
    }

    @POST
    @PreAuthorize("hasAuthority('SCOPE_write')")
    @Operation(summary = "Create a new product")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "SKU already exists")
    })
    public Response create(@Valid ProductDto dto) {
        ProductDto created = productService.create(dto);
        URI location = URI.create("/api/v1/products/" + created.getId());
        return Response.created(location)
                .entity(ApiResponse.success(created, "Product created successfully"))
                .build();
    }

    @PUT
    @Path("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_write')")
    @Operation(summary = "Update a product")
    public Response update(@PathParam("id") Long id, @Valid ProductDto dto) {
        return Response.ok(ApiResponse.success(productService.update(id, dto), "Product updated")).build();
    }

    @DELETE
    @Path("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_write')")
    @Operation(summary = "Soft-delete a product")
    public Response delete(@PathParam("id") Long id) {
        productService.delete(id);
        return Response.ok(ApiResponse.success(null, "Product deleted successfully")).build();
    }

    @PATCH
    @Path("/{id}/restock")
    @PreAuthorize("hasAuthority('SCOPE_write')")
    @Operation(summary = "Restock a product")
    public Response restock(
            @PathParam("id") Long id,
            @Parameter(description = "Quantity to add", required = true)
            @QueryParam("quantity") int quantity) {

        return Response.ok(ApiResponse.success(productService.restock(id, quantity), "Restocked successfully")).build();
    }
}
