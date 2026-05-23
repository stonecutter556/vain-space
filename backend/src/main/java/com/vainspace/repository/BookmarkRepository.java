package com.vainspace.repository;

import com.vainspace.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findAllByOrderByCategoryAscCreatedAtDesc();
}
