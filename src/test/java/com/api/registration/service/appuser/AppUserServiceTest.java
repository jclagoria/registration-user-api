package com.api.registration.service.appuser;

import com.api.registration.dto.PhoneRequest;
import com.api.registration.dto.RegistrationRequest;
import com.api.registration.dto.RegistrationResponse;
import com.api.registration.dto.TokenDetails;
import com.api.registration.model.AppUser;
import com.api.registration.token.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepositoryMock;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoderMock;

    @Mock
    private JwtTokenProvider jwtTokenProviderMock;

    private AppUserService appUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appUserService = new AppUserService(appUserRepositoryMock, bCryptPasswordEncoderMock, jwtTokenProviderMock);
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        RegistrationRequest request = new RegistrationRequest(
                "John Doe",
                "test@example.com",
                "password123",
                List.of()
        );

        when(appUserRepositoryMock.findByEmail(request.email()))
                .thenReturn(Optional.of(new AppUser()));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> appUserService.signUpUser(request)
        );

        assertEquals("El correo ya registrado", exception.getMessage());
        verify(appUserRepositoryMock, never()).save(any());
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Given
        RegistrationRequest request = new RegistrationRequest(
                "John Doe",
                "test@example.com",
                "password123",
                List.of(new PhoneRequest("123456789", "1", "44"))
        );

        String encodedPassword = "encodedPassword123";
        String token = "jwtToken";
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plusSeconds(900);
        UUID generatedId = UUID.randomUUID(); // Simula el ID generado

        when(appUserRepositoryMock.findByEmail(request.email()))
                .thenReturn(Optional.empty());
        when(bCryptPasswordEncoderMock.encode(request.password()))
                .thenReturn(encodedPassword);
        when(jwtTokenProviderMock.generateToken(request.name(), request.email()))
                .thenReturn(new TokenDetails(token, issuedAt, expiration));

        // Simula el guardado del usuario y la generación del ID
        when(appUserRepositoryMock.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser savedUser = invocation.getArgument(0);
            savedUser.setId(generatedId); // Asigna el ID generado
            return savedUser;
        });

        // When
        RegistrationResponse response = appUserService.signUpUser(request);

        // Then
        ArgumentCaptor<AppUser> appUserArgumentCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepositoryMock).save(appUserArgumentCaptor.capture());

        AppUser savedUser = appUserArgumentCaptor.getValue();
        assertEquals(request.name(), savedUser.getName());
        assertEquals(request.email(), savedUser.getEmail());
        assertEquals(encodedPassword, savedUser.getPassword());
        assertTrue(savedUser.isActive());
        assertEquals(1, savedUser.getPhones().size());
        assertEquals("123456789", savedUser.getPhones().get(0).getNumber());

        assertEquals(token, response.token());
        assertEquals(generatedId.toString(), response.id());
    }
}