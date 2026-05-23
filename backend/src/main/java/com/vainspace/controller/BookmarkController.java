package com.vainspace.controller;

import com.vainspace.entity.Bookmark;
import com.vainspace.repository.BookmarkRepository;
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
    public List<Bookmark> getAll() {
        return bookmarkRepository.findAllByOrderByCategoryAscCreatedAtDesc();
    }

    @PostMapping
    public Bookmark create(@RequestBody Bookmark bookmark) {
        return bookmarkRepository.save(bookmark);
    }

    @PutMapping("/{id}")
    public Bookmark update(@PathVariable Long id, @RequestBody Bookmark bookmark) {
        bookmark.setId(id);
        return bookmarkRepository.save(bookmark);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        bookmarkRepository.deleteById(id);
    }
}
