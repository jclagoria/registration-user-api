package com.api.registration.registration;

import com.api.registration.dto.DataTokenResponse;
import com.api.registration.dto.RegistrationRequest;
import com.api.registration.dto.RegistrationResponse;
import com.api.registration.service.registration.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/registration")
@AllArgsConstructor
@Tag(name = "Registration API", description = "Endpoints for user registration and token confirmation")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    @Operation(summary = "Registro de un nuevo Usuario", description = "This endpoint registers a new user with the provided details.")
    public ResponseEntity<RegistrationResponse> register(@RequestBody RegistrationRequest request) {
        return ResponseEntity.ok(registrationService.register(request));
    }

    @GetMapping(path = "info/token")
    @Operation(summary = "Informacion de un Token",
            description = "El endpoint retorna informacion sobre un token en especifico, si este existe o si dicho token expiro")
    public DataTokenResponse confirm(@RequestParam("token") String token) {
        return registrationService.infoToken(token);
    }

}
