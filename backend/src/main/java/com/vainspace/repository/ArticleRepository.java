package com.vainspace.repository;

import com.vainspace.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findAllByOrderByCreatedAtDesc();
    List<Article> findByArticleTypeOrderByCreatedAtDesc(String articleType);
    List<Article> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Article> findByUserIdAndArticleTypeOrderByCreatedAtDesc(Long userId, String articleType);
    List<Article> findByIsPublicTrueOrderByCreatedAtDesc();
}
