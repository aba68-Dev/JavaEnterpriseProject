package com.enterprise.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for User resource.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User resource")
public class UserDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Schema(description = "Unique login username")
    private String username;

    @NotBlank
    @Email(message = "Must be a valid email")
    @Schema(description = "Email address")
    private String email;

    @Schema(description = "Plaintext password — write-only", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Full display name")
    private String fullName;

    @Size(max = 20)
    private String phone;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String status;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> roles;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
