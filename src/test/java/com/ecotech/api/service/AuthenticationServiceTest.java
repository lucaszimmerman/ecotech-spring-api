package com.ecotech.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import com.ecotech.api.config.JwtProperties;
import com.ecotech.api.controller.dto.CreateUserDTO;
import com.ecotech.api.controller.dto.auth.LoginResponseDTO;
import com.ecotech.api.controller.mappers.UserMapper;
import com.ecotech.api.model.User;
import com.ecotech.api.model.enums.UserRole;
import com.ecotech.api.security.UserPrincipal;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthenticationService service;

    @Test
    void shouldRegisterUserAndReturnAccessToken() {
        CreateUserDTO dto = new CreateUserDTO(
                "lucas",
                "lucas@email.com",
                "123456",
                "Lucas Zimmerman");
        User user = new User();
        User savedUser = new User();
        UUID userId = UUID.randomUUID();

        savedUser.setId(userId);
        savedUser.setUsername("lucas");
        savedUser.setEmail("lucas@email.com");
        savedUser.setPassword("encoded-password");
        savedUser.setName("Lucas Zimmerman");
        savedUser.setRole(UserRole.USER);
        savedUser.setActive(true);

        when(userMapper.toEntity(dto))
                .thenReturn(user);

        when(userService.save(user))
                .thenReturn(savedUser);

        when(jwtService.generateToken(org.mockito.ArgumentMatchers.any()))
                .thenReturn("access-token");

        when(jwtProperties.expiration())
                .thenReturn(3600L);

        LoginResponseDTO response = service.register(dto);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.username()).isEqualTo("lucas");
        assertThat(response.name()).isEqualTo("Lucas Zimmerman");
        assertThat(response.role()).isEqualTo(UserRole.USER);
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);

        verify(userMapper).toEntity(dto);
        verify(userService).save(user);
        ArgumentCaptor<UserPrincipal> principalCaptor =
                ArgumentCaptor.forClass(UserPrincipal.class);

        verify(jwtService).generateToken(principalCaptor.capture());
        verify(jwtProperties).expiration();
        verifyNoInteractions(authenticationManager);

        UserPrincipal principal = principalCaptor.getValue();

        assertThat(principal.getId()).isEqualTo(userId);
        assertThat(principal.getUsername()).isEqualTo("lucas");
        assertThat(principal.getName()).isEqualTo("Lucas Zimmerman");
        assertThat(principal.getRole()).isEqualTo(UserRole.USER);
    }
}
