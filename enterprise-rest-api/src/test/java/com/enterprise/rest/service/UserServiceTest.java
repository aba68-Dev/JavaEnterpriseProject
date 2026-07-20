package com.enterprise.rest.service;

import com.enterprise.common.exception.BusinessValidationException;
import com.enterprise.domain.dto.UserDto;
import com.enterprise.domain.entity.User;
import com.enterprise.domain.mapper.UserMapper;
import com.enterprise.kafka.producer.KafkaEventProducer;
import com.enterprise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock UserRepository     userRepository;
    @Mock UserMapper         userMapper;
    @Mock PasswordEncoder    passwordEncoder;
    @Mock KafkaEventProducer kafkaEventProducer;

    @InjectMocks UserService userService;

    private UserDto testDto;
    private User    testUser;

    @BeforeEach
    void setUp() {
        testDto = UserDto.builder()
                .username("alice")
                .email("alice@example.com")
                .password("Secret@123")
                .fullName("Alice Smith")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .fullName("Alice Smith")
                .build();
    }

    @Test
    @DisplayName("register throws exception when username already taken")
    void register_throwsException_whenUsernameTaken() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(testDto))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("alice");
    }

    @Test
    @DisplayName("register throws exception when email already taken")
    void register_throwsException_whenEmailTaken() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(testDto))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("alice@example.com");
    }

    @Test
    @DisplayName("register saves user and publishes Kafka event on success")
    void register_savesUserAndPublishesEvent_onSuccess() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userMapper.toEntity(testDto)).thenReturn(testUser);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testDto);

        UserDto result = userService.register(testDto);

        assertThat(result.getUsername()).isEqualTo("alice");
        verify(userRepository).save(any(User.class));
        verify(kafkaEventProducer).publish(anyString(), anyString(), any());
    }
}
