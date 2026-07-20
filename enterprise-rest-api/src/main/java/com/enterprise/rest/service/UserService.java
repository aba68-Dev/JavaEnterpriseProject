package com.enterprise.rest.service;

import com.enterprise.common.exception.BusinessValidationException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.common.model.PagedResponse;
import com.enterprise.domain.dto.UserDto;
import com.enterprise.domain.entity.User;
import com.enterprise.domain.entity.UserStatus;
import com.enterprise.domain.mapper.UserMapper;
import com.enterprise.kafka.event.UserRegisteredEvent;
import com.enterprise.kafka.producer.KafkaEventProducer;
import com.enterprise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.enterprise.common.constants.AppConstants.*;

/**
 * Business service for User registration and management.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository     userRepository;
    private final UserMapper         userMapper;
    private final PasswordEncoder    passwordEncoder;
    private final KafkaEventProducer kafkaEventProducer;

    @Transactional(readOnly = true)
    public PagedResponse<UserDto> findAll(int page, int size) {
        Page<User> users = userRepository.findAll(
                PageRequest.of(page, size, Sort.by("username")));
        return PagedResponse.of(
                users.getContent().stream().map(userMapper::toDto).toList(),
                page, size, users.getTotalElements());
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return userMapper.toDto(getUserOrThrow(id));
    }

    public UserDto register(UserDto dto) {
        validateUniqueConstraints(dto);

        User user = userMapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(List.of(ROLE_USER));

        User saved = userRepository.save(user);
        log.info("User registered: id={}, username={}", saved.getId(), saved.getUsername());

        publishUserRegisteredEvent(saved);
        return userMapper.toDto(saved);
    }

    public UserDto update(Long id, UserDto dto) {
        User existing = getUserOrThrow(id);
        existing.setFullName(dto.getFullName());
        existing.setPhone(dto.getPhone());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existing.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        return userMapper.toDto(userRepository.save(existing));
    }

    public void deactivate(Long id) {
        User user = getUserOrThrow(id);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("User deactivated: id={}", id);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private void validateUniqueConstraints(UserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessValidationException("Username [" + dto.getUsername() + "] is already taken");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessValidationException("Email [" + dto.getEmail() + "] is already registered");
        }
    }

    private void publishUserRegisteredEvent(User user) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("USER_REGISTERED");
        event.setOccurredAt(LocalDateTime.now());
        event.setSourceService("enterprise-rest-api");

        kafkaEventProducer.publish(TOPIC_USER_REGISTERED, user.getUsername(), event);
    }
}
