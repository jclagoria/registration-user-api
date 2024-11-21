package com.api.registration.registration;

import com.api.registration.dto.RegistrationRequest;
import com.api.registration.dto.RegistrationResponse;
import com.api.registration.exception.GlobalExceptionHandler;
import com.api.registration.security.config.WebSecurityConfig;
import com.api.registration.service.registration.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistrationController.class)
@Import({WebSecurityConfig.class, GlobalExceptionHandler.class})
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        // Given
        RegistrationRequest request = new RegistrationRequest(
                "John Doe",
                "test@example.com",
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

        when(registrationService.register(request)).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.created").value("2024-11-21T12:00:00"))
                .andExpect(jsonPath("$.token").value("jwtToken"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(registrationService, times(1)).register(request);
    }
}
