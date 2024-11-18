package com.example.thuan_security.service;

import com.example.thuan_security.model.User;
import com.example.thuan_security.repo.RoleRepo;
import com.example.thuan_security.repo.UserRepo;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;

    public List<User> getAllUser(){
        return userRepo.findAll();
    }
}
