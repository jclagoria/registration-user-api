package com.api.registration.token;

import com.api.registration.dto.TokenDetails;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private SecretKey secretKey;

    private final long expirationTime;

    public JwtTokenProvider(@Value("${jwt.expiration}") long expirationTime) {
        this.expirationTime = expirationTime;
    }

    @PostConstruct
    public void init() {
        this.secretKey = Jwts.SIG.HS256.key().build();
    }

    public TokenDetails generateToken(String name, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", name);
        claims.put("email", email);

        Instant issuedAt = Instant.now();
        Instant validity = issuedAt.plus(expirationTime, ChronoUnit.MILLIS);

        String token = Jwts.builder().claims(claims)
                .subject(email).issuedAt(Date.from(issuedAt))
                .expiration(Date.from(validity))
                .signWith(secretKey)
                .compact();

        return new TokenDetails(token, issuedAt, validity);
    }

}
