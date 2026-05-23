package com.vainspace.controller;

import com.vainspace.entity.Bookmark;
import com.vainspace.repository.BookmarkRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkRepository bookmarkRepository;

    public BookmarkController(BookmarkRepository bookmarkRepository) {
        this.bookmarkRepository = bookmarkRepository;
    }

    @GetMapping
    public List<Bookmark> getAll(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return bookmarkRepository.findByUserIdOrderByCategoryAscCreatedAtDesc(userId);
    }

    @GetMapping("/public")
    public List<Bookmark> getPublic() {
        return bookmarkRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }

    @PostMapping
    public Bookmark create(@RequestBody Bookmark bookmark, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        bookmark.setUserId(userId);
        if (bookmark.getIsPublic() == null) bookmark.setIsPublic(false);
        return bookmarkRepository.save(bookmark);
    }

    @PutMapping("/{id}")
    public Bookmark update(@PathVariable Long id, @RequestBody Bookmark bookmark, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Bookmark existing = bookmarkRepository.findById(id).orElse(null);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return null;
        }
        bookmark.setId(id);
        return bookmarkRepository.save(bookmark);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Bookmark existing = bookmarkRepository.findById(id).orElse(null);
        if (existing != null && existing.getUserId().equals(userId)) {
            bookmarkRepository.deleteById(id);
        }
    }
}
