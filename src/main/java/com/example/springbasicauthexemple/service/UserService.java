package com.example.springbasicauthexemple.service;

import com.example.springbasicauthexemple.entity.Role;
import com.example.springbasicauthexemple.entity.User;
import com.example.springbasicauthexemple.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createNewAccount(User user, Role role) {
        user.setRoles(Collections.singletonList(role));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        role.setUser(user);

        return userRepository.saveAndFlush(user);
    }

    public User findByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username not found!"));
    }
}
