package com.example.thuan_security.config;


import com.example.thuan_security.model.User;
import com.example.thuan_security.repo.UserRepo;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Data
public class UsersDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User employee = userRepository.findByUsername(email);
        if (employee == null) {
            return new org.springframework.security.core.userdetails.User(
                    email,
                    "",
                    Set.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }
        Set<GrantedAuthority> authorities = employee.getRoles().stream()
                .map((roles) -> new SimpleGrantedAuthority(roles.getName()))
                .collect(Collectors.toSet());
        String password= employee.getPassword();
        if(password ==null || password.isEmpty()){
            password="";
        }
        return new org.springframework.security.core.userdetails.User(
                email,
                password,
                authorities
        );
    }

}