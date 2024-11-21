package com.api.registration.service.registration;

import com.api.registration.dto.DataTokenResponse;
import com.api.registration.dto.RegistrationResponse;
import com.api.registration.registration.EmailValidator;
import com.api.registration.registration.PasswordValidator;
import com.api.registration.service.appuser.AppUserService;
import com.api.registration.dto.RegistrationRequest;
import com.api.registration.model.Token;
import com.api.registration.service.token.TokenInfoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final AppUserService appUserService;
    private final EmailValidator emailValidator;
    private final PasswordValidator passwordValidator;
    private final TokenInfoService confirmationTokenService;

    public RegistrationResponse register(RegistrationRequest request) {

        if (!emailValidator.test(request.email())) {
            throw new IllegalStateException("Email no valido");
        }

        if (!passwordValidator.test(request.password())) {
            throw new IllegalStateException("Password no valido");
        }

        return appUserService.signUpUser(request);
    }

    public DataTokenResponse infoToken(String token) {
        Token confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() ->
                        new IllegalStateException("No se encuentra el token "+ token));

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El Token expiro");
        }

        return new DataTokenResponse(
                confirmationToken.getToken(),
                confirmationToken.getAppUser().getName(),
                confirmationToken.getCreatedAt().toString(),
                confirmationToken.getExpiresAt().toString());
    }
}