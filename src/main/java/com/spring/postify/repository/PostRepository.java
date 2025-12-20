package com.spring.postify.repository;

import com.spring.postify.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Post> findByContentContainingIgnoreCase(String content, Pageable pageable);

    Page<Post> findByAuthor_NameContainingIgnoreCase(String name, Pageable pageable);

    Page<Post> findByTags_NameContainingIgnoreCase(String keyword, Pageable pageable);
}