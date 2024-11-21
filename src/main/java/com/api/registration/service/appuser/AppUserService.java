package com.api.registration.service.appuser;

import com.api.registration.dto.RegistrationRequest;
import com.api.registration.dto.RegistrationResponse;
import com.api.registration.dto.TokenDetails;
import com.api.registration.model.AppUser;
import com.api.registration.model.Token;
import com.api.registration.model.Phone;
import com.api.registration.token.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@AllArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public RegistrationResponse signUpUser(RegistrationRequest request) {
        boolean userExists = appUserRepository
                .findByEmail(request.email())
                .isPresent();

        if (userExists) {
            throw new IllegalStateException("El correo ya registrado");
        }

        String encodedPassword = bCryptPasswordEncoder
                .encode(request.password());

        AppUser appUser = new AppUser(request.name(), request.email(), encodedPassword);
        appUser.setActive(true);
        appUser.setLastLoginAt(LocalDateTime.now());

        request.phones().forEach(phoneRequest -> {
            Phone phone = new Phone(phoneRequest.number(),
                    phoneRequest.cityCode(),
                    phoneRequest.countryCode(), appUser);
            appUser.addPhone(phone);
        });
        TokenDetails tokenDetails = jwtTokenProvider.generateToken(appUser.getName(), appUser.getEmail());

        Token token = new Token(
                tokenDetails.token(),
                LocalDateTime.ofInstant(tokenDetails.issuedAt(), ZoneId.systemDefault()),
                LocalDateTime.ofInstant(tokenDetails.expiration(), ZoneId.systemDefault()),
                appUser
        );

        appUser.addConfirmationToken(token);

        appUserRepository.save(appUser);

        return new RegistrationResponse(
                appUser.getId().toString(),
                appUser.getCreatedAt().toString(),
                appUser.getModifiedAt() != null ? appUser.getModifiedAt().toString() : null,
                tokenDetails.token(),
                appUser.isActive());
    }
}
