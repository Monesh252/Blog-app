package com.spring.postify.repository;

import com.spring.postify.entity.Post;
import com.spring.postify.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT DISTINCT p.author FROM Post p ORDER BY p.author.name")
    List<User> findDistinctAuthors();

    @Query("""
        SELECT DISTINCT p FROM Post p 
        JOIN p.tags t
    """)
    Page<Post> listPosts(Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE p.publishedAt BETWEEN :from AND :to
            AND (:authorIds IS NULL OR p.author.id IN :authorIds)
            AND (
                :tagList IS NULL
                OR EXISTS (
                    SELECT 1 FROM p.tags t2
                    WHERE LOWER(t2.name) IN :tagList
                )
            )
            AND (
                :keyword IS NULL
                OR (
                    (:type = 'all' AND (
                        LOWER(p.title) LIKE :keyword
                     OR LOWER(p.content) LIKE :keyword
                     OR LOWER(p.author.name) LIKE :keyword
                     OR EXISTS (
                            SELECT 1 FROM p.tags t3
                            WHERE LOWER(t3.name) LIKE :keyword
                     )
                    ))
                    OR (:type = 'title'   AND LOWER(p.title) LIKE :keyword)
                    OR (:type = 'content' AND LOWER(p.content) LIKE :keyword)
                    OR (:type = 'author'  AND LOWER(p.author.name) LIKE :keyword)
                    OR (:type = 'tags' AND EXISTS(
                            SELECT 1 FROM p.tags t4
                            WHERE LOWER(t4.name) LIKE :keyword
                    ))
                )
            )
            """)
    Page<Post> searchEverything(
            @Param("type") String type,
            @Param("keyword") String keyword,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("authorIds") List<Long> authorIds,
            @Param("tagList") List<String> tagList,
            Pageable pageable
    );
}