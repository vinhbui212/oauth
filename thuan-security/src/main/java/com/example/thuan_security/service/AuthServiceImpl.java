package com.example.thuan_security.service;


import com.example.thuan_security.config.JwtTokenProvider;
import com.example.thuan_security.controller.AuthController;
import com.example.thuan_security.dto.LoginDto;
import com.example.thuan_security.dto.LoginResponse;
import com.example.thuan_security.model.Roles;
import com.example.thuan_security.model.Token;
import com.example.thuan_security.model.User;
import com.example.thuan_security.repo.RoleRepo;
import com.example.thuan_security.repo.TokenRepository;
import com.example.thuan_security.repo.UserRepo;
import com.google.api.client.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.thuan_security.service.TokenService.expirationRefreshToken;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl  {


    private final AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private TokenRepository tokenRepository;
    private final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public LoginResponse login(LoginDto loginDto) throws Exception {
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        String depCode = loginDto.getDepCode();

        User user = userRepo.findByUsername(username);

        if (user == null) {
            throw new Exception("Invalid username or password");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("Invalid username or password");
        }

        if (!user.getDepartment().getDep_code().equals(depCode)) {
            throw new Exception("Invalid department code");
        }

        List<String> roles = new ArrayList<>();
        for (Roles role : user.getRoles()) {
            roles.add(role.getName());
        }

        String token = jwtTokenProvider.generateAccessToken(username, roles);
        Token refreshToken = tokenService.refreshToken(user.getId());

        Token existingToken = tokenRepository.findByUserId(user.getId());
        if (existingToken != null) {
            existingToken.setRfToken(refreshToken.getRfToken());
            existingToken.setExpirationDate(LocalDateTime.now().plus(Duration.ofSeconds(expirationRefreshToken)));
            existingToken.setJwtToken(token);
            tokenRepository.save(existingToken);
        } else {
            Token token1 = new Token();
            token1.setRfToken(refreshToken.getRfToken());
            token1.setExpirationDate(LocalDateTime.now().plus(Duration.ofSeconds(expirationRefreshToken)));
            token1.setJwtToken(token);
            token1.setUser(user);
            tokenRepository.save(token1);
        }


        LoginResponse response = LoginResponse.builder()
                .username(user.getUsername())
                .refreshToken(refreshToken.getRfToken())
                .roles(roles)
                .token(token)
                .build();
        // Trả về token
        logger.info("Login response: {}", response);
        return response;
    }




    public  String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getAuthorities().toString();
            } else {
                return principal.toString();
            }
        }
        return null;
    }
//    public  String generateEmpCode() {
//        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
//        Random random = new Random();
//        StringBuilder empCode = new StringBuilder();
//
//        for (int i = 0; i < 4; i++) {
//            int index = random.nextInt(characters.length());
//            empCode.append(characters.charAt(index));
//        }
//
//        return empCode.toString();
//    }
}