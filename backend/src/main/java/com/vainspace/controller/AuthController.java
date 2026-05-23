package com.vainspace.controller;

import com.vainspace.config.JwtUtil;
import com.vainspace.entity.User;
import com.vainspace.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.length() < 2) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名至少2个字符"));
        }
        if (password == null || password.length() < 4) {
            return ResponseEntity.badRequest().body(Map.of("error", "密码至少4个字符"));
        }
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名已存在"));
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(hashPassword(password));
        if (userRepository.count() == 0) {
            user.setRole("admin");
        }
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return ResponseEntity.ok(Map.of("token", token, "username", username, "role", user.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !user.getPassword().equals(hashPassword(password))) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名或密码错误"));
        }

        if (user.getRole() == null) {
            user.setRole("user");
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return ResponseEntity.ok(Map.of("token", token, "username", username, "role", user.getRole()));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
