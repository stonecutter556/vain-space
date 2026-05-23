package com.vainspace.controller;

import com.vainspace.entity.User;
import com.vainspace.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        }
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname());
            m.put("role", u.getRole());
            m.put("email", u.getEmail());
            m.put("createdAt", u.getCreatedAt());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> setRole(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "无权限"));
        }
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
        user.setRole(body.get("role"));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "更新成功"));
    }
}
