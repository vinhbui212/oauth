package com.example.thuan_security.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class LoginDto {

    private String username;
    private String password;
    private String depCode;
}
