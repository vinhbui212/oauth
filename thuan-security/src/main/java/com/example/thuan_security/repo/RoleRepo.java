package com.example.thuan_security.repo;

import com.example.thuan_security.model.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface RoleRepo extends JpaRepository<Roles,Long> {

    Optional<Roles> findByName(String name);

}
