package com.api.registration.service.token;

import com.api.registration.model.Token;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TokenInfoService {

    private final TokenInfoRepository confirmationTokenRepository;

    public Optional<Token> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

}
