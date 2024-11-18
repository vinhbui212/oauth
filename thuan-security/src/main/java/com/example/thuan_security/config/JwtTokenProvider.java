package com.example.thuan_security.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.example.thuan_security.dto.AuthResponseDto;
import com.example.thuan_security.model.Department;
import com.example.thuan_security.model.Roles;
import com.example.thuan_security.model.User;
import com.example.thuan_security.repo.RoleRepo;
import com.example.thuan_security.repo.UserRepo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.nimbusds.jose.Payload;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.auth0.jwt.interfaces.DecodedJWT;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.function.Function;
import com.auth0.jwt.algorithms.Algorithm;

@Component
public class JwtTokenProvider {

    private final int accessExpirationMs = 300000;

    private Logger logger=LoggerFactory.getLogger(JwtTokenProvider.class);
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;
    public JwtTokenProvider() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        this.publicKey = (RSAPublicKey) kp.getPublic();
        this.privateKey = (RSAPrivateKey) kp.getPrivate();
    }

    public String getPublicKeyAsBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public String getPrivateKeyAsBase64() {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    // Tạo JWT access token
    public String generateAccessToken(String userName, List<String> roleArray) {
        return Jwts.builder()
                .setSubject(userName)
                .claim("username", userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + accessExpirationMs))
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }

    // Xác thực JWT token
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            System.out.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.out.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string is empty: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error validating JWT token: " + e.getMessage());
        }
        return false;
    }


    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }



    public AuthResponseDto googleRegister(String token) throws Exception {
        try {

            DecodedJWT jwt = JWT.decode(token);
            String email = jwt.getClaim("email").asString();

            if (email == null || email.isEmpty()) {
                throw new Exception("Invalid token: email not found");
            }

            User user = userRepo.findByUsername(email);

            if (user != null) {
                String accessToken = generateAccessToken(email, getUserRoles(user));
                return AuthResponseDto.builder()
                        .accessToken(accessToken)
                        .build();
            } else {
                User newUser = new User();
                newUser.setUsername(email);

                Roles roles = roleRepo.findByName("ROLE_USER").orElseThrow();
                logger.info(String.valueOf(roles));
                if (roles == null ) {
                    throw new Exception("Role 'ROLE_USER' not found");
                 }

                Set<Roles> rolesSet = new HashSet<>();
                rolesSet.add(roles); // Thêm role vào Set

                newUser.setRoles(rolesSet);
                userRepo.save(newUser);

                String newAccessToken = generateAccessToken(email, getUserRoles(newUser));

                return AuthResponseDto.builder()
                        .accessToken(newAccessToken)
                        .build();
            }

        } catch (com.auth0.jwt.exceptions.SignatureVerificationException e) {
            throw new Exception("Token signature verification failed: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Token verification failed: " + e.getMessage());
        }
    }

    private List<String> getUserRoles(User user) {
        List<String> roles = new ArrayList<>();
        for (Roles role : user.getRoles()) {
            roles.add(role.getName());
        }
        return roles;
    }


}