package com.spring.postify.controller;

import com.spring.postify.entity.Post;
import com.spring.postify.entity.Tag;
import com.spring.postify.service.CommentService;
import com.spring.postify.service.PostService;
import com.spring.postify.service.TagService;
import com.spring.postify.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final CommentService commentService;
    private final TagService tagService;

    public PostController(PostService postService, UserService userService,
                          CommentService commentService, TagService tagService){
        this.postService = postService;
        this.userService = userService;
        this.commentService = commentService;
        this.tagService = tagService;
    }

    @GetMapping
    public String listPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Post> postPage = postService.getPosts(page, size);

        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("totalItems", postPage.getTotalElements());
        model.addAttribute("size", size);

        return "posts/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model){
        model.addAttribute("post", postService.getPost(id));
        model.addAttribute("comments", commentService.getCommentsByPost(id));

        return "posts/view";
    }

    @GetMapping("/create")
    public String createForm(Model model){
        model.addAttribute("post", new Post());
        return "posts/create";
    }

    @PostMapping
    public String save(@ModelAttribute Post post,
                       @RequestParam(required = false) String tagsInput){
        post.setAuthor(userService.getUser(3L));
        if (tagsInput != null && !tagsInput.isBlank()) {

            Set<Tag> tags = Arrays.stream(tagsInput.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(tagService::getOrCreateTag)
                    .collect(Collectors.toSet());

            post.setTags(tags);

            System.out.println(tags);
        }
        postService.save(post);
        return "redirect:/posts";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model){
        Post post = postService.getPost(id);
        model.addAttribute("post", post);

        String existingTags = post.getTags()
                .stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));

        model.addAttribute("tagsInput", existingTags);

        return "posts/edit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute Post post,
                         @RequestParam(required = false) String tagsInput) {

        Set<Tag> tags = new HashSet<>();

        if (tagsInput != null && !tagsInput.isBlank()) {

            tags = Arrays.stream(tagsInput.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(tagService::getOrCreateTag)
                    .collect(Collectors.toSet());
        }

        post.setTags(tags);

        postService.update(id, post);

        return "redirect:/posts";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id){
        postService.delete(id);
        return "redirect:/posts";
    }

    @GetMapping("/createtag")
    public String create(Model model){
        model.addAttribute("post", new Post());
        model.addAttribute("tags", tagService.getAllTags());
        return "posts/create";
    }

    @GetMapping("/search")
    public String searchResult(
            @RequestParam String type,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(required = false) List<Long> authorIds,
            Model model) {

        Page<Post> result;

        if(authorIds != null && !authorIds.isEmpty()){
            result = postService.searchWithAuthors(type, keyword, authorIds, page, size, sortBy);
        } else {
            result = postService.search(type, keyword, page, size, sortBy);
        }

        model.addAttribute("posts", result.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("selectedAuthors", authorIds);

        return "posts/list";
    }
}