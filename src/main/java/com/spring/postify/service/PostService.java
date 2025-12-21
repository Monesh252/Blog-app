package com.spring.postify.service;

import com.spring.postify.entity.Post;
import com.spring.postify.entity.Tag;
import com.spring.postify.entity.User;
import com.spring.postify.repository.PostRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository){
        this.postRepository = postRepository;
    }

    public Page<Post> getPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return postRepository.findAll(pageable);
    }

    public Post getPost(Long id){
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post Not Found"));
    }

    public Post save(Post post){
        return postRepository.save(post);
    }

    public Post update(Long id, Post updatedPost){

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
            LocalDateTime fromDate,
            LocalDateTime toDate,
            List<Long> authorIds,
            List<String> tags) {

        Sort sort;
        switch (sortBy) {
            case "oldest":
                sort = Sort.by("publishedAt").ascending();
                break;
            case "title":
                sort = Sort.by("title").ascending();
                break;
            case "author":
                sort = Sort.by("author.name").ascending();
                break;
            default:
                sort = Sort.by("publishedAt").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        LocalDateTime from = (fromDate != null) ? fromDate : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime to = (toDate != null) ? toDate : LocalDateTime.now().plusYears(100);

        boolean hasAuthors = authorIds != null && !authorIds.isEmpty();
        boolean hasTags = tags != null && !tags.isEmpty();

        if (hasAuthors && hasTags) {
            return postRepository.filterByAuthorsAndTags(
                    authorIds, tags, from, to, pageable);
        }

        if (hasAuthors) {
            switch (type) {
                case "content":
                    return postRepository.findByContentContainingIgnoreCaseAndAuthorIdInAndPublishedAtBetween(
                            keyword, authorIds, from, to, pageable);
                case "author":
                    return postRepository.findByAuthorNameContainingIgnoreCaseAndAuthorIdInAndPublishedAtBetween(
                            keyword, authorIds, from, to, pageable);
                case "tags":
                    return postRepository.findByTagsNameContainingIgnoreCaseAndAuthorIdInAndPublishedAtBetween(
                            keyword, authorIds, from, to, pageable);
                default:
                    return postRepository.findByTitleContainingIgnoreCaseAndAuthorIdInAndPublishedAtBetween(
                            keyword, authorIds, from, to, pageable);
            }
        }

        if (hasTags) {
            switch (type) {
                case "content":
                    return postRepository.searchContentWithTags(
                            keyword, tags, from, to, pageable);
                case "author":
                    return postRepository.searchAuthorWithTags(
                            keyword, tags, from, to, pageable);
                case "title":
                default:
                    return postRepository.searchTitleWithTags(
                            keyword, tags, from, to, pageable);
            }
        }

        switch (type) {
            case "content":
                return postRepository.findByContentContainingIgnoreCaseAndPublishedAtBetween(
                        keyword, from, to, pageable);
            case "author":
                return postRepository.findByAuthorNameContainingIgnoreCaseAndPublishedAtBetween(
                        keyword, from, to, pageable);
            case "tags":
                return postRepository.findByTagsNameContainingIgnoreCaseAndPublishedAtBetween(
                        keyword, from, to, pageable);
            default:
                return postRepository.findByTitleContainingIgnoreCaseAndPublishedAtBetween(
                        keyword, from, to, pageable);
        }

    }

    public void delete(Long id){
        postRepository.deleteById(id);
    }

    public List<User> getDistinctAuthorDetails() {
        return postRepository.findDistinctAuthors();
    }

    public Page<Post> getPostsFiltered(int page, int size, List<String> tags) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());

        if (tags == null || tags.isEmpty()) {
            return postRepository.findAll(pageable);
        }

        LocalDateTime from = LocalDateTime.of(1970,1,1,0,0);
        LocalDateTime to = LocalDateTime.now().plusYears(100);

        return postRepository.filterByTags(tags, from, to, pageable);
    }
}