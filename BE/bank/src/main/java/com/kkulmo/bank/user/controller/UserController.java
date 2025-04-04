package com.kkulmo.bank.user.controller;

import com.kkulmo.bank.user.dto.UserCIDTO;
import com.kkulmo.bank.user.dto.UserCIRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kkulmo.bank.user.dto.UserDTO;
import com.kkulmo.bank.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 은행 user 생성시에 카드 생성하기
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.createUser(userDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/userci")
    public ResponseEntity<UserCIDTO> getUserCI(@RequestParam("userName") String userName, @RequestParam("birth") String birth) {
        return ResponseEntity.ok(userService.getCIbyNameAndBirth(userName , birth)) ;
    }
}
