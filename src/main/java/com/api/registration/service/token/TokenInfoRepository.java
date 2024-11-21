package com.api.registration.service.token;

import com.api.registration.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface TokenInfoRepository
        extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);
}
