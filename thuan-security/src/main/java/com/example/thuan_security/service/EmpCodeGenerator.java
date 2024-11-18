package com.example.thuan_security.service;

import java.util.Random;

public class EmpCodeGenerator {


    public static String generateEmpCode() {

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder empCode = new StringBuilder();


        for (int i = 0; i < 4; i++) {
            int index = random.nextInt(characters.length());
            empCode.append(characters.charAt(index));
        }

        return empCode.toString();
    }

    public static void main(String[] args) {
        String empCode = generateEmpCode();
        System.out.println("Generated Employee Code: " + empCode);
    }
}

