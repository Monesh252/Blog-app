package com.spring.postify.repository;

import com.spring.postify.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Post> findByContentContainingIgnoreCase(String content, Pageable pageable);

    Page<Post> findByAuthor_NameContainingIgnoreCase(String name, Pageable pageable);

    Page<Post> findByTags_NameContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Post> findByTitleContainingIgnoreCaseAndPublishedAtBetween(
            String title, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    // Content search with date range
    Page<Post> findByContentContainingIgnoreCaseAndPublishedAtBetween(
            String content, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    // Author search with date range
    Page<Post> findByAuthor_NameContainingIgnoreCaseAndPublishedAtBetween(
            String authorName, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    // Tags search with date range
    Page<Post> findByTags_NameContainingIgnoreCaseAndPublishedAtBetween(
            String tagName, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
}