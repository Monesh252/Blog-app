package com.spring.postify.service;

import com.spring.postify.entity.Post;
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
            List<Long> authorIds) {

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

        if (authorIds != null && !authorIds.isEmpty()) {
            switch (type) {
                case "content":
                    return postRepository.findByContentContainingIgnoreCaseAndAuthorIdInAndPublishedAtBetween(
                            keyword, authorIds, from, to, pageable);
                case "author":
                    return postRepository.findByAuthorNameContainingIgnoreCaseAndAuthorIdInAndPublishedAtBetween(
                            keyword, authorIds, from, to, pageable);
                case "tags":
                    // For tags, you might need a different approach
                    return postRepository.findByTagsNameContainingIgnoreCaseAndAuthorIdInAndPublishedAtBetween(
                            keyword, authorIds, from, to, pageable);
                default: // title
                    return postRepository.findByTitleContainingIgnoreCaseAndAuthorIdInAndPublishedAtBetween(
                            keyword, authorIds, from, to, pageable);
            }
        } else {
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
                default: // title
                    return postRepository.findByTitleContainingIgnoreCaseAndPublishedAtBetween(
                            keyword, from, to, pageable);
            }
        }
    }


    public void delete(Long id){
        postRepository.deleteById(id);
    }

    public List<User> getDistinctAuthorDetails() {
        return postRepository.findDistinctAuthors();
    }
}