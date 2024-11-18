package com.example.thuan_security.controller;

import com.example.thuan_security.config.JwtTokenProvider;
import com.example.thuan_security.dto.*;
import com.example.thuan_security.model.RoleEnum;
import com.example.thuan_security.model.Roles;
import com.example.thuan_security.model.Token;
import com.example.thuan_security.model.User;
import com.example.thuan_security.repo.RoleRepo;
import com.example.thuan_security.repo.TokenRepository;
import com.example.thuan_security.repo.UserRepo;
import com.example.thuan_security.service.AuthServiceImpl;

import com.example.thuan_security.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.thuan_security.service.TokenService.expirationRefreshToken;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private AuthServiceImpl authService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private TokenRepository tokenRepository;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginDto loginDto) {
        try {
            LoginResponse response = authService.login(loginDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {

            logger.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/googleRegister")
    public ResponseEntity<AuthResponseDto> authenticate(@RequestBody Map<String, String> payload) throws Exception {

        String token = payload.get("token");
        //token cua google

        return ResponseEntity.ok(jwtTokenProvider.googleRegister(token));
    }
    @PostMapping("/refreshtoken")
    public TokenRefreshResponse refreshtoken(@RequestBody TokenRefreshRequest request) throws Exception {
        String requestRefreshToken = request.getRefreshToken();

        Optional<Token> token = tokenRepository.findByRfToken(requestRefreshToken);
        if(token.isPresent()){
            User user = token.get().getUser();
            String jwt= jwtTokenProvider.generateAccessToken(user.getUsername(), Collections.singletonList(user.getAuthorities().toString()));
             Token rftoken=tokenService.refreshToken(user.getId());
             rftoken.setExpirationDate(LocalDateTime.now().plus(Duration.ofSeconds(expirationRefreshToken)));
             rftoken.setJwtToken(jwt);
            tokenRepository.save(rftoken);
            return TokenRefreshResponse.builder().accessToken(jwt)
                    .refreshToken(rftoken.getRfToken()).build();
        }

        return null;
    }
    @PostMapping("/demote")
    public String demote(@RequestParam String jwt) throws Exception {
        String username= jwtTokenProvider.extractUsername(jwt);
        User user=userRepo.findByUsername(username);
        logger.info(user.getRoles().toString());
        Optional<Roles> roles = roleRepo.findByName("ROLE_USER");
        if (roles == null || roles.isEmpty()) {
            throw new Exception("Role 'ROLE_USER' not found");
        }
        Roles role = roles.get();

        Set<Roles> rolesSet = new HashSet<>();
        rolesSet.add(role);

        user.setRoles(rolesSet);
        logger.info(roles.toString());
        tokenService.revokeToken(jwt);
        userRepo.save(user);
        return "Demoted Admin to User";
    }
}
