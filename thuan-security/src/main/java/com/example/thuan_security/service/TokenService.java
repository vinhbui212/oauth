package com.example.thuan_security.service;

import com.example.thuan_security.config.JwtTokenProvider;
import com.example.thuan_security.controller.AuthController;
import com.example.thuan_security.model.Token;
import com.example.thuan_security.model.User;
import com.example.thuan_security.repo.TokenRepository;
import com.example.thuan_security.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {


    public static final int expirationRefreshToken = 5184000;
    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepo userRepo;
    private final Logger logger = LoggerFactory.getLogger(TokenService.class);

    public Optional<Token> findByToken(String token) {
        return tokenRepository.findByRfToken(token);
    }

    public Token refreshToken(Long userId) throws Exception {
        Token refreshToken = new Token();

        User user = userRepo.findById(userId).orElseThrow(() -> new Exception("User not found"));
        refreshToken.setUser(user);

        refreshToken.setExpirationDate(LocalDateTime.now().plus(Duration.ofSeconds(expirationRefreshToken)));
        logger.info("Refresh token expiration date: {}", refreshToken.getExpirationDate());

        String newTokenValue = UUID.randomUUID().toString();
        refreshToken.setRfToken(newTokenValue);

        Token existingTokenOpt = tokenRepository.findByUserId(userId); // Giả sử có phương thức này
        if (existingTokenOpt != null) {
            Token existingToken = existingTokenOpt;
            existingToken.setRfToken(newTokenValue);
            existingToken.setExpirationDate(LocalDateTime.now().plus(Duration.ofSeconds(expirationRefreshToken)));
            refreshToken = tokenRepository.save(existingToken);
        } else {
            refreshToken = tokenRepository.save(refreshToken);
        }

        logger.info("Generated refresh token: {}", refreshToken.getRfToken());
        return refreshToken;
    }

    public Token verifyExpiration(Token token) throws Exception {
        if (token.getExpirationDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);
            throw new Exception("Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        return tokenRepository.deleteByUser(userRepo.findById(userId).get());
    }

    public String revokeToken(String jwtToken) {
        Token token = tokenRepository.findByJwtToken(jwtToken);
        if (token.isRevoked() == false) {
            token.setRevoked(true);
            tokenRepository.save(token);
            return "Revoked token";
        } else {
            return "";
        }
    }
}
