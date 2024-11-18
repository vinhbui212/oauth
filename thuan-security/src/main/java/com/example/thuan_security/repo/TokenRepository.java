package com.example.thuan_security.repo;

import com.example.thuan_security.model.Token;
import com.example.thuan_security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findByUserId(Long userId);

    Optional<Token> findByRfToken(String token);

    Token findByJwtToken(String token);
    @Modifying
    int deleteByUser(User user);
}
