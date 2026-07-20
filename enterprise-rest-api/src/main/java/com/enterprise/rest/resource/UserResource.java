package com.enterprise.rest.resource;

import com.enterprise.common.model.ApiResponse;
import com.enterprise.common.model.PagedResponse;
import com.enterprise.domain.dto.UserDto;
import com.enterprise.rest.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.net.URI;

import static com.enterprise.common.constants.AppConstants.DEFAULT_PAGE_SIZE;

/**
 * JAX-RS resource for the /api/v1/users endpoint.
 *
 * HTTP Protocol:
 *   POST   /users/register — public registration
 *   GET    /users          — list users (ADMIN)
 *   GET    /users/{id}     — get by ID (ADMIN)
 *   PUT    /users/{id}     — update profile
 *   DELETE /users/{id}     — deactivate (ADMIN)
 */
@Component
@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "User registration and management")
@RequiredArgsConstructor
public class UserResource {

    private final UserService userService;

    @POST
    @Path("/register")
    @Operation(summary = "Register a new user (public)")
    public Response register(@Valid UserDto dto) {
        UserDto created = userService.register(dto);
        URI location = URI.create("/api/v1/users/" + created.getId());
        return Response.created(location)
                .entity(ApiResponse.success(created, "Registration successful"))
                .build();
    }

    @GET
    @PreAuthorize("hasAuthority('SCOPE_write')")
    @Operation(summary = "List all users (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public Response listAll(
            @QueryParam("page") @DefaultValue("0")  int page,
            @QueryParam("size") @DefaultValue("" + DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<UserDto> result = userService.findAll(page, size);
        return Response.ok(ApiResponse.success(result)).build();
    }

    @GET
    @Path("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_read')")
    @Operation(summary = "Get user by ID")
    @SecurityRequirement(name = "bearerAuth")
    public Response getById(@PathParam("id") Long id) {
        return Response.ok(ApiResponse.success(userService.findById(id))).build();
    }

    @PUT
    @Path("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_write')")
    @Operation(summary = "Update user profile")
    @SecurityRequirement(name = "bearerAuth")
    public Response update(@PathParam("id") Long id, @Valid UserDto dto) {
        return Response.ok(ApiResponse.success(userService.update(id, dto), "Profile updated")).build();
    }

    @DELETE
    @Path("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_write')")
    @Operation(summary = "Deactivate a user (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public Response deactivate(@PathParam("id") Long id) {
        userService.deactivate(id);
        return Response.ok(ApiResponse.success(null, "User deactivated")).build();
    }
}
