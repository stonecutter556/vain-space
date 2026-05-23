package com.vainspace.controller;

import com.vainspace.entity.Article;
import com.vainspace.repository.ArticleRepository;
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
    public List<Article> getAll() {
        return articleRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/type/{type}")
    public List<Article> getByType(@PathVariable String type) {
        return articleRepository.findByArticleTypeOrderByCreatedAtDesc(type);
    }

    @GetMapping("/{id}")
    public Article getById(@PathVariable Long id) {
        return articleRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Article create(@RequestBody Article article) {
        return articleRepository.save(article);
    }

    @PutMapping("/{id}")
    public Article update(@PathVariable Long id, @RequestBody Article article) {
        article.setId(id);
        return articleRepository.save(article);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        articleRepository.deleteById(id);
    }
}
