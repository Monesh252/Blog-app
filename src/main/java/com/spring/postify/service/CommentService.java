package com.spring.postify.service;

import com.spring.postify.entity.Comment;
import com.spring.postify.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository){
        this.commentRepository = commentRepository;
    }

    public List<Comment> getCommentsByPost(Long postId){
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    public Comment save(Comment comment){
        return commentRepository.save(comment);
    }

    public Comment get(Long id){
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    public void delete(Long id){
        commentRepository.deleteById(id);
    }

}