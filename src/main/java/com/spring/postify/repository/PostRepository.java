package com.spring.postify.repository;

import com.spring.postify.entity.Post;
import com.spring.postify.entity.Tag;
import com.spring.postify.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByTitleContainingIgnoreCaseAndPublishedAtBetween(
            String title, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    Page<Post> findByContentContainingIgnoreCaseAndPublishedAtBetween(
            String content, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);


    @Query("SELECT DISTINCT p.author FROM Post p ORDER BY p.author.name")
    List<User> findDistinctAuthors();

    @Query(value = "SELECT p FROM Post p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND " +
            "p.author.id IN :authorIds AND " +
            "p.publishedAt BETWEEN :fromDate AND :toDate",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE " +
                    "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND " +
                    "p.author.id IN :authorIds AND " +
                    "p.publishedAt BETWEEN :fromDate AND :toDate")
    Page<Post> findByTitleContainingIgnoreCaseAndAuthorIdInAndPublishedAtBetween(
            @Param("keyword") String keyword,
            @Param("authorIds") List<Long> authorIds,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query(value = "SELECT p FROM Post p WHERE " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND " +
            "p.author.id IN :authorIds AND " +
            "p.publishedAt BETWEEN :fromDate AND :toDate",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE " +
                    "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND " +
                    "p.author.id IN :authorIds AND " +
                    "p.publishedAt BETWEEN :fromDate AND :toDate")
    Page<Post> findByContentContainingIgnoreCaseAndAuthorIdInAndPublishedAtBetween(
            @Param("keyword") String keyword,
            @Param("authorIds") List<Long> authorIds,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query(value = "SELECT p FROM Post p WHERE " +
            "LOWER(p.author.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND " +
            "p.author.id IN :authorIds AND " +
            "p.publishedAt BETWEEN :fromDate AND :toDate",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE " +
                    "LOWER(p.author.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND " +
                    "p.author.id IN :authorIds AND " +
                    "p.publishedAt BETWEEN :fromDate AND :toDate")
    Page<Post> findByAuthorNameContainingIgnoreCaseAndAuthorIdInAndPublishedAtBetween(
            @Param("keyword") String keyword,
            @Param("authorIds") List<Long> authorIds,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Post p " +
            "JOIN p.tags t " +
            "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND p.author.id IN :authorIds " +
            "AND p.publishedAt BETWEEN :fromDate AND :toDate",
            countQuery = "SELECT COUNT(DISTINCT p) FROM Post p " +
                    "JOIN p.tags t " +
                    "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "AND p.author.id IN :authorIds " +
                    "AND p.publishedAt BETWEEN :fromDate AND :toDate")
    Page<Post> findByTagsNameContainingIgnoreCaseAndAuthorIdInAndPublishedAtBetween(
            @Param("keyword") String keyword,
            @Param("authorIds") List<Long> authorIds,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Post p " +
            "JOIN p.tags t " +
            "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND p.publishedAt BETWEEN :fromDate AND :toDate",
            countQuery = "SELECT COUNT(DISTINCT p) FROM Post p " +
                    "JOIN p.tags t " +
                    "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "AND p.publishedAt BETWEEN :fromDate AND :toDate")
    Page<Post> findByTagsNameContainingIgnoreCaseAndPublishedAtBetween(
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query("SELECT p FROM Post p WHERE " +
            "LOWER(p.author.name) LIKE LOWER(CONCAT('%', :authorName, '%')) AND " +
            "p.publishedAt BETWEEN :startDate AND :endDate")
    Page<Post> findByAuthorNameContainingIgnoreCaseAndPublishedAtBetween(
            @Param("authorName") String authorName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("""
        SELECT DISTINCT p FROM Post p
        JOIN p.tags t
        WHERE t.name IN :tags
        AND p.publishedAt BETWEEN :from AND :to
    """)
    Page<Post> filterByTags(
            @Param("tags") List<String> tags,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );


    @Query("""
        SELECT DISTINCT p FROM Post p 
        JOIN p.tags t
        WHERE p.author.id IN :authorIds
        AND t.name IN :tags
        AND p.publishedAt BETWEEN :from AND :to
    """)
    Page<Post> filterByAuthorsAndTags(
            List<Long> authorIds,
            List<String> tags,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );


    @Query("SELECT DISTINCT p.name FROM Tag p ORDER BY p.name")
    List<String> findDistinctTags();

    @Query("""
        SELECT DISTINCT p FROM Post p
        JOIN p.tags t
        WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        AND t.name IN :tags
        AND p.publishedAt BETWEEN :from AND :to
    """)
    Page<Post> searchTitleWithTags(
            @Param("keyword") String keyword,
            @Param("tags") List<String> tags,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT p FROM Post p
        JOIN p.tags t
        WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
        AND t.name IN :tags
        AND p.publishedAt BETWEEN :from AND :to
    """)
    Page<Post> searchContentWithTags(
            @Param("keyword") String keyword,
            @Param("tags") List<String> tags,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT p FROM Post p
        JOIN p.tags t
        WHERE LOWER(p.author.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        AND t.name IN :tags
        AND p.publishedAt BETWEEN :from AND :to
    """)
    Page<Post> searchAuthorWithTags(
            @Param("keyword") String keyword,
            @Param("tags") List<String> tags,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );


}