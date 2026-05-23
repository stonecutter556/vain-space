package com.vainspace.controller;

import com.vainspace.entity.Article;
import com.vainspace.repository.ArticleRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleRepository articleRepository;

    public ArticleController(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @GetMapping
    public List<Article> getAll(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return articleRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @GetMapping("/public")
    public List<Article> getPublic() {
        return articleRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }

    @GetMapping("/type/{type}")
    public List<Article> getByType(@PathVariable String type, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return articleRepository.findByUserIdAndArticleTypeOrderByCreatedAtDesc(userId, type);
    }

    @GetMapping("/{id}")
    public Article getById(@PathVariable Long id) {
        return articleRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Article create(@RequestBody Article article, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        article.setUserId(userId);
        if (article.getIsPublic() == null) article.setIsPublic(false);
        return articleRepository.save(article);
    }

    @PutMapping("/{id}")
    public Article update(@PathVariable Long id, @RequestBody Article article, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Article existing = articleRepository.findById(id).orElse(null);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return null;
        }
        article.setId(id);
        return articleRepository.save(article);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Article existing = articleRepository.findById(id).orElse(null);
        if (existing != null && existing.getUserId().equals(userId)) {
            articleRepository.deleteById(id);
        }
    }
}
