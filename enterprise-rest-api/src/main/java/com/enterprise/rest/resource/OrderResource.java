package com.enterprise.rest.resource;

import com.enterprise.common.model.ApiResponse;
import com.enterprise.common.model.PagedResponse;
import com.enterprise.domain.dto.OrderDto;
import com.enterprise.domain.entity.OrderStatus;
import com.enterprise.rest.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.net.URI;

import static com.enterprise.common.constants.AppConstants.DEFAULT_PAGE_SIZE;

/**
 * JAX-RS resource for the /api/v1/orders endpoint.
 *
 * HTTP Protocol:
 *   GET    /orders            — list all orders (ADMIN)
 *   GET    /orders/{id}       — get by ID
 *   GET    /orders/my         — authenticated user's orders
 *   POST   /orders            — place a new order
 *   PATCH  /orders/{id}/status — update status (ADMIN)
 *   DELETE /orders/{id}/cancel — cancel order
 */
@Component
@Path("/api/v1/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Orders", description = "Order lifecycle management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class OrderResource {

    private final OrderService orderService;

    @GET
    @PreAuthorize("hasAuthority('SCOPE_write')")
    @Operation(summary = "List all orders (admin)")
    public Response listAll(
            @QueryParam("page") @DefaultValue("0")  int page,
            @QueryParam("size") @DefaultValue("" + DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<OrderDto> result = orderService.findAll(page, size);
        return Response.ok(ApiResponse.success(result)).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get order by ID")
    public Response getById(@PathParam("id") Long id) {
        return Response.ok(ApiResponse.success(orderService.findById(id))).build();
    }

    @GET
    @Path("/my")
    @Operation(summary = "Get orders for the authenticated user")
    public Response getMyOrders(
            @AuthenticationPrincipal Jwt jwt,
            @QueryParam("page") @DefaultValue("0")  int page,
            @QueryParam("size") @DefaultValue("" + DEFAULT_PAGE_SIZE) int size) {

        Long userId = Long.parseLong(jwt.getSubject());
        PagedResponse<OrderDto> result = orderService.findByUser(userId, page, size);
        return Response.ok(ApiResponse.success(result)).build();
    }

    @POST
    @Operation(summary = "Place a new order")
    public Response placeOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid OrderDto dto) {

        Long userId = Long.parseLong(jwt.getSubject());
        OrderDto created = orderService.createOrder(userId, dto);
        URI location = URI.create("/api/v1/orders/" + created.getId());
        return Response.created(location)
                .entity(ApiResponse.success(created, "Order placed successfully"))
                .build();
    }

    @PATCH
    @Path("/{id}/status")
    @PreAuthorize("hasAuthority('SCOPE_write')")
    @Operation(summary = "Update order status (admin)")
    public Response updateStatus(
            @PathParam("id") Long id,
            @QueryParam("status") String status) {

        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        return Response.ok(ApiResponse.success(orderService.updateStatus(id, orderStatus))).build();
    }

    @DELETE
    @Path("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public Response cancelOrder(
            @PathParam("id") Long id,
            @QueryParam("reason") @DefaultValue("Cancelled by user") String reason) {

        orderService.cancelOrder(id, reason);
        return Response.ok(ApiResponse.success(null, "Order cancelled successfully")).build();
    }
}
