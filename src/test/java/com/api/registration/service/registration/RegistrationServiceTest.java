package com.api.registration.service.registration;

import com.api.registration.dto.DataTokenResponse;
import com.api.registration.dto.RegistrationRequest;
import com.api.registration.dto.RegistrationResponse;
import com.api.registration.model.AppUser;
import com.api.registration.model.Token;
import com.api.registration.registration.EmailValidator;
import com.api.registration.registration.PasswordValidator;
import com.api.registration.service.appuser.AppUserService;
import com.api.registration.service.token.TokenInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationServiceTest {

    @Mock
    private AppUserService appUserService;

    @Mock
    private EmailValidator emailValidator;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private TokenInfoService confirmationTokenService;

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registrationService = new RegistrationService(appUserService, emailValidator,
                passwordValidator, confirmationTokenService);
    }

    @Test
    void shouldThrowWhenEmailIsInvalid() {
        // Given
        RegistrationRequest request = new RegistrationRequest(
                "John Doe",
                "invalid-email",
                "password123",
                List.of()
        );

        when(emailValidator.test(request.email())).thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> registrationService.register(request)
        );

        assertEquals("Email no valido", exception.getMessage());
        verifyNoInteractions(appUserService); // Asegura que no se llamó a appUserService
    }

    @Test
    void shouldThrowWhenPasswordIsInvalid() {
        // Given
        RegistrationRequest request = new RegistrationRequest(
                "John Doe",
                "valid.email@example.com",
                "password123",
                List.of()
        );

        when(emailValidator.test(request.email())).thenReturn(true);
        when(passwordValidator.test(request.password())).thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> registrationService.register(request)
        );

        assertEquals("Password no valido", exception.getMessage());
        verifyNoInteractions(appUserService); // Asegura que no se llamó a appUserService
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Given
        RegistrationRequest request = new RegistrationRequest(
                "John Doe",
                "valid.email@example.com",
                "password123",
                List.of()
        );

        RegistrationResponse expectedResponse = new RegistrationResponse(
                "123",
                "2024-11-21T12:00:00",
                null,
                "jwtToken",
                true
        );

        when(emailValidator.test(request.email())).thenReturn(true);
        when(passwordValidator.test(request.password())).thenReturn(true);
        when(appUserService.signUpUser(request)).thenReturn(expectedResponse);

        // When
        RegistrationResponse response = registrationService.register(request);

        // Then
        assertEquals(expectedResponse, response);
        ArgumentCaptor<RegistrationRequest> requestCaptor =
                ArgumentCaptor.forClass(RegistrationRequest.class);
        verify(appUserService).signUpUser(requestCaptor.capture());
        assertEquals(request, requestCaptor.getValue());
    }

    @Test
    void shouldReturnTokenInfoSuccessfully() {
        // Given
        String token = "validToken";
        LocalDateTime now = LocalDateTime.now();
        Token confirmationToken = new Token(
                token,
                now.minusMinutes(5),
                now.plusMinutes(5),
                new AppUser("John Doe", "test@example.com", "Password123")
        );

        when(confirmationTokenService.getToken(token)).thenReturn(Optional.of(confirmationToken));

        // When
        DataTokenResponse response = registrationService.infoToken(token);

        // Then
        assertEquals(token, response.token());
        assertEquals(confirmationToken.getAppUser().getName(), response.nombreUsuario());
        assertEquals(confirmationToken.getCreatedAt().toString(), response.createAt());
        assertEquals(confirmationToken.getExpiresAt().toString(), response.expirationDate());
        verify(confirmationTokenService).getToken(token);
    }

    @Test
    void shouldThrowWhenTokenNotFound() {
        // Given
        String token = "invalidToken";

        when(confirmationTokenService.getToken(token)).thenReturn(Optional.empty());

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> registrationService.infoToken(token)
        );

        assertEquals("No se encuentra el token invalidToken", exception.getMessage());
        verify(confirmationTokenService).getToken(token);
    }

    @Test
    void shouldThrowWhenTokenIsExpired() {
        // Given
        String token = "expiredToken";
        LocalDateTime now = LocalDateTime.now();
        Token expiredToken = new Token(
                token,
                now.minusHours(2),
                now.minusMinutes(1),
                new AppUser("John Doe", "test@example.com", "Password123")
        );

        when(confirmationTokenService.getToken(token)).thenReturn(Optional.of(expiredToken));

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> registrationService.infoToken(token)
        );

        assertEquals("El Token expiro", exception.getMessage());
        verify(confirmationTokenService).getToken(token);
    }

}