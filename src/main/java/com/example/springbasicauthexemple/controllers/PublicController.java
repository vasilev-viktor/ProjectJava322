package com.example.springbasicauthexemple.controllers;

import com.example.springbasicauthexemple.entity.Role;
import com.example.springbasicauthexemple.entity.RoleType;
import com.example.springbasicauthexemple.entity.User;
import com.example.springbasicauthexemple.model.UserDto;
import com.example.springbasicauthexemple.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<String> getPublic() {
        return ResponseEntity.ok("Осуществлен вызов публичного метода!");
    }

    @PostMapping("/account")
    public ResponseEntity<UserDto> createNewAccount(@RequestBody UserDto userDto, @RequestParam RoleType roleType) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createAccount(userDto, roleType));
    }

    private UserDto createAccount(UserDto userDto, RoleType roleType) {
        var user = new User();

        user.setPassword(userDto.getPassword());
        user.setUsername(userDto.getUsername());

        var createdUser = userService.createNewAccount(user, Role.from(roleType));

        return UserDto
                .builder()
                .username(createdUser.getUsername())
                .password(createdUser.getPassword())
                .build();
    }
}
