package com.example.thuan_security.repo;

import com.example.thuan_security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User,Long> {
    User findByUsername(String username);
    @Query("SELECT u FROM User u WHERE u.department.dep_code = :depCode")
    User findUsersByDepartmentCode(@Param("depCode") UUID depCode);

}
