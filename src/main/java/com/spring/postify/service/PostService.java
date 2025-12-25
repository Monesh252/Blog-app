package com.spring.postify.service;

import com.spring.postify.entity.Post;
import com.spring.postify.entity.User;
import com.spring.postify.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final TagService tagService;

    @Autowired
    public PostService(PostRepository postRepository, TagService tagService) {
        this.postRepository = postRepository;
        this.tagService = tagService;
    }

    public Post getPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post Not Found"));
    }

    public Post save(Post post) {
        return postRepository.save(post);
    }

    public Post update(Long id, Post updatedPost) {

        Post post = getPost(id);

        post.setTitle(updatedPost.getTitle());
        post.setExcerpt(updatedPost.getExcerpt());
        post.setContent(updatedPost.getContent());
        post.setAuthor(updatedPost.getAuthor());
        post.setTags(updatedPost.getTags());
        post.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    public Page<Post> search(
            String type,
            String keyword,
            int page,
            int size,
            String sortBy,
            LocalDateTime from,
            LocalDateTime to,
            List<Long> authorIds,
            List<String> tags
    ) {

        Sort sort = switch (sortBy) {
            case "oldest" -> Sort.by("publishedAt").ascending();
            case "title" -> Sort.by("title").ascending();
            case "author" -> Sort.by("author.name").ascending();
            default -> Sort.by("publishedAt").descending();
        };

        Pageable pageable = PageRequest.of(page, size, sort);

        if (from == null) from = LocalDateTime.of(1970, 1, 1, 0, 0);
        if (to == null) to = LocalDateTime.now().plusYears(100);

        return postRepository.searchEverything(
                type,
                keyword,
                from,
                to,
                authorIds,
                tags,
                pageable
        );
    }

    public void delete(Long id) {
        postRepository.deleteById(id);
    }

    public List<User> getDistinctAuthorDetails() {
        return postRepository.findDistinctAuthors();
    }

    public Page<Post> getPostsFiltered(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());

        return postRepository.listPosts(pageable);
    }

    public LocalDateTime parseFromDate(String fromDate) {

        if (fromDate == null || fromDate.isBlank() || fromDate.equalsIgnoreCase("null"))
            return null;

        try {
            return LocalDateTime.parse(fromDate);
        } catch (Exception e) {
            try {
                return LocalDate.parse(fromDate).atStartOfDay();
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    public LocalDateTime parseToDate(String toDate) {

        if (toDate == null || toDate.isBlank() || toDate.equalsIgnoreCase("null"))
            return null;

        try {
            return LocalDateTime.parse(toDate);
        } catch (Exception e) {
            try {
                return LocalDate.parse(toDate).atTime(LocalTime.MAX);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    public List<Long> parseAuthorIds(String[] authorIds) {

        if (authorIds == null)
            return null;

        List<Long> result = new ArrayList<>();

        for (String id : authorIds) {
            try {
                result.add(Long.parseLong(id));
            } catch (Exception ignored) {
            }
        }

        return result.isEmpty() ? null : result;
    }
}