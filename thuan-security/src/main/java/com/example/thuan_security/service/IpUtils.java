package com.example.thuan_security.service;

import jakarta.servlet.http.HttpServletRequest;


public class IpUtils {
    public static String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}

