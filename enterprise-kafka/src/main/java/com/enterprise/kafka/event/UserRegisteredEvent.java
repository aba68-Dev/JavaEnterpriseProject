package com.enterprise.kafka.event;

import lombok.*;

/**
 * Raised when a new User account is registered.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserRegisteredEvent extends DomainEvent {

    private Long   userId;
    private String username;
    private String email;
    private String fullName;
}
