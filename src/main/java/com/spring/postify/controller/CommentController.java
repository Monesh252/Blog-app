package com.spring.postify.controller;

import com.spring.postify.entity.Comment;
import com.spring.postify.entity.Post;
import com.spring.postify.service.CommentService;
import com.spring.postify.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;

    public CommentController(CommentService commentService, PostService postService){
        this.commentService = commentService;
        this.postService = postService;
    }

    private Comment getComment(Long id) {
        return commentService.get(id);
    }

    @PostMapping("/add")
    public String addComment(
            @RequestParam Long postId,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam("comment") String commentText) {

        Post post = postService.getPost(postId);

        Comment comment = new Comment();
        comment.setName(name);
        comment.setEmail(email);
        comment.setComment(commentText);
        comment.setPost(post);

        commentService.save(comment);

        return "redirect:/posts/" + postId + "#comments";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id){
        Comment comment = getComment(id);
        Long postId = comment.getPost().getId();

        commentService.delete(id);

        return "redirect:/posts/" + postId + "#comments";
    }

    @GetMapping("/edit/{id}")
    public String editCommentForm(@PathVariable Long id, Model model){
        Comment comment = getComment(id);
        model.addAttribute("comment", comment);
        return "comments/edit";
    }

    @PostMapping("/update/{id}")
    public String updateComment(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam String email,
                                @RequestParam("comment") String commentText){

        Comment comment = getComment(id);
        comment.setName(name);
        comment.setEmail(email);
        comment.setComment(commentText);

        commentService.save(comment);

        return "redirect:/posts/" + comment.getPost().getId() + "#comments";
    }

}