package com.spring.postify.service;

import com.spring.postify.entity.Post;
import com.spring.postify.entity.Tag;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public List<String> splitStringTags(String tags) {
        if (tags == null || tags.isBlank())
            return List.of();

        List<String> list = new ArrayList<>();
        for (String t : tags.split("#")) {
            String trimmed = t.trim();
            if (!trimmed.isEmpty()) list.add(trimmed);
        }
        return list;
    }

    public Set<Tag> parseTags(String tagsInput) {

        Set<Tag> tags = new HashSet<>();

        if (tagsInput == null || tagsInput.isBlank())
            return tags;

        for (String t : tagsInput.split("#")) {
            String trimmed = t.trim();
            if (!trimmed.isEmpty()) {
                tags.add(tagService.getOrCreateTag(trimmed));
            }
        }

        return tags;
    }

    public String formatTags(Set<Tag> tags) {

        if (tags == null || tags.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();

        for (Tag tag : tags) {
            if (tag != null && tag.getName() != null) {
                sb.append("#")
                        .append(tag.getName().trim())
                        .append(" ");
            }
        }

        return sb.toString().trim();
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

    public List<String> parseTagNames(String input) {

        List<String> result = new ArrayList<>();

        if (input == null || input.isBlank() || "null".equalsIgnoreCase(input)) {
            return result;   // return empty list
        }

        String[] parts = input.split("#");

        for (String s : parts) {
            if (s != null) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed.toLowerCase());
                }
            }
        }

        return result;
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