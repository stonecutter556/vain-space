package com.vainspace.controller;

import com.vainspace.config.JwtUtil;
import com.vainspace.entity.Article;
import com.vainspace.entity.Goal;
import com.vainspace.entity.User;
import com.vainspace.repository.ArticleRepository;
import com.vainspace.repository.GoalRepository;
import com.vainspace.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final GoalRepository goalRepository;
    private final JwtUtil jwtUtil;

    private static final String AVATAR_DIR = System.getProperty("user.dir") + "/uploads/avatars/";

    public UserController(UserRepository userRepository, ArticleRepository articleRepository, GoalRepository goalRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
        this.goalRepository = goalRepository;
        this.jwtUtil = jwtUtil;
        new File(AVATAR_DIR).mkdirs();
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));

        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "nickname", user.getNickname() != null ? user.getNickname() : user.getUsername(),
            "avatar", user.getAvatar() != null ? user.getAvatar() : "",
            "bio", user.getBio() != null ? user.getBio() : "",
            "email", user.getEmail() != null ? user.getEmail() : "",
            "createdAt", user.getCreatedAt()
        ));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(HttpServletRequest request, @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));

        if (body.containsKey("nickname")) user.setNickname(body.get("nickname"));
        if (body.containsKey("bio")) user.setBio(body.get("bio"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "更新成功"));
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));

        try {
            String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String fileName = userId + "_" + UUID.randomUUID() + ext;
            Path path = Paths.get(AVATAR_DIR + fileName);
            Files.copy(file.getInputStream(), path);

            String avatarUrl = "/uploads/avatars/" + fileName;
            user.setAvatar(avatarUrl);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("avatar", avatarUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "上传失败"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam("q") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(keyword.trim());
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", u.getId());
            item.put("username", u.getUsername());
            item.put("nickname", u.getNickname() != null ? u.getNickname() : u.getUsername());
            item.put("avatar", u.getAvatar() != null ? u.getAvatar() : "");
            return item;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicProfile(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
        }

        List<Article> publicArticles = articleRepository.findByIsPublicTrueOrderByCreatedAtDesc().stream()
            .filter(a -> a.getUserId().equals(id))
            .collect(Collectors.toList());

        List<Goal> publicGoals = goalRepository.findByIsPublicTrueOrderByCreatedAtDesc().stream()
            .filter(g -> g.getUserId().equals(id))
            .collect(Collectors.toList());

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
        profile.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        profile.put("bio", user.getBio() != null ? user.getBio() : "");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("profile", profile);
        result.put("articles", publicArticles);
        result.put("goals", publicGoals);

        return ResponseEntity.ok(result);
    }
}
