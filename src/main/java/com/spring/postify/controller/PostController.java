package com.spring.postify.controller;

import com.spring.postify.entity.Post;
import com.spring.postify.entity.Tag;
import com.spring.postify.entity.User;
import com.spring.postify.service.CommentService;
import com.spring.postify.service.PostService;
import com.spring.postify.service.TagService;
import com.spring.postify.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
        List<User> authors = postService.getDistinctAuthorDetails();
        System.out.println("Number of authors found: " + authors.size());


        model.addAttribute("authors", authors);
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
        post.setAuthor(userService.getUser(17L));

        Set<Tag> tags = new HashSet<>();
        if (tagsInput != null && !tagsInput.isBlank()) {

            String[] splitTags = tagsInput.split("#");

            for (String t : splitTags) {
                t = t.trim();
                if (t != null) {
                    String trimmed = t.trim();
                    if (!trimmed.isEmpty()) {
                        Tag tag = tagService.getOrCreateTag(trimmed);
                        tags.add(tag);
                    }
                }
            }

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

        String existingTags = "";
        if (post.getTags() != null && !post.getTags().isEmpty()) {

            StringBuilder sb = new StringBuilder();

            for (Tag tag : post.getTags()) {
                if (tag != null && tag.getName() != null) {
                    sb.append("#").append(tag.getName()).append(" ");
                }
            }

            existingTags = sb.toString().trim();
        }


        model.addAttribute("tagsInput", existingTags);

        return "posts/edit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute Post post,
                         @RequestParam(required = false) String tagsInput) {

        Set<Tag> tags = new HashSet<>();

        if (tagsInput != null && !tagsInput.isBlank()) {

            String[] splitTags = tagsInput.split("#");

            for (String t : splitTags) {
                if (t != null) {
                    String trimmed = t.trim();
                    if (!trimmed.isEmpty()) {
                        Tag tag = tagService.getOrCreateTag(trimmed);
                        tags.add(tag);
                    }
                }
            }
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
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String[] authorIds,
            Model model) {

        LocalDateTime from = null;
        LocalDateTime to = null;

        if (fromDate != null && !fromDate.trim().isEmpty() && !fromDate.equalsIgnoreCase("null")) {
            try {
                from = LocalDateTime.parse(fromDate);
            } catch (DateTimeParseException e) {
                try {
                    from = LocalDate.parse(fromDate).atStartOfDay();
                } catch (DateTimeParseException e2) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid fromDate format. Use yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss");
                }
            }
        }

        if (toDate != null && !toDate.trim().isEmpty() && !toDate.equalsIgnoreCase("null")) {
            try {
                to = LocalDateTime.parse(toDate);
            } catch (DateTimeParseException e) {
                try {
                    to = LocalDate.parse(toDate).atTime(LocalTime.MAX);
                } catch (DateTimeParseException e2) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid toDate format. Use yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss");
                }
            }
        }

        List<User> authors = postService.getDistinctAuthorDetails();

        List<Long> selectedAuthorIds = new ArrayList<>();
        if (authorIds != null && authorIds.length > 0) {
            for (String id : authorIds) {
                if (id != null && !id.trim().isEmpty() && !id.equals("null")) {
                    try {
                        selectedAuthorIds.add(Long.parseLong(id));
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }

        Page<Post> result = postService.search(type, keyword, page, size, sortBy, from, to,
                            selectedAuthorIds.isEmpty() ? null : selectedAuthorIds);

        model.addAttribute("selectedAuthors", selectedAuthorIds);
        model.addAttribute("authors",authors);
        model.addAttribute("posts", result.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);

        return "posts/list";
    }
}