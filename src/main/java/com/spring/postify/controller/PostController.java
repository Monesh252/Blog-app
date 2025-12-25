package com.spring.postify.controller;

import com.spring.postify.entity.Post;
import com.spring.postify.entity.User;
import com.spring.postify.service.CommentService;
import com.spring.postify.service.PostService;
import com.spring.postify.service.TagService;
import com.spring.postify.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final CommentService commentService;
    private final TagService tagService;

    public PostController(PostService postService, UserService userService,
                          CommentService commentService, TagService tagService) {
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

        Page<Post> postPage = postService.getPostsFiltered(page, size);
        List<User> authors = postService.getDistinctAuthorDetails();

        model.addAttribute("authors", authors);
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("totalItems", postPage.getTotalElements());
        model.addAttribute("size", size);

        return "posts/list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.getPost(id));
        model.addAttribute("comments", commentService.getCommentsByPost(id));
        return "posts/view";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        List<User> authors = postService.getDistinctAuthorDetails();

        model.addAttribute("post", new Post());
        model.addAttribute("authors", authors);
        return "posts/create";
    }

    @PostMapping
    public String save(@ModelAttribute Post post,
                       @RequestParam(required = false) String tagsInput,
                       @RequestParam(required = false) Long authorId) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                && authorId != null) {

            post.setAuthor(userService.getUser(authorId));

        } else {
            post.setAuthor(userService.getUserByEmail(email));
        }

        post.setTags(tagService.parseTags(tagsInput));
        postService.save(post);

        return "redirect:/posts";
    }


    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Post post = postService.getPost(id);
        String existingTags = tagService.formatTags(post.getTags());
        model.addAttribute("post", post);
        model.addAttribute("tagsInput", existingTags);
        return "posts/edit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute Post incoming,
                         @RequestParam(required = false) String tagsInput) {

        Post existing = postService.getPost(id);
        existing.setTitle(incoming.getTitle());
        existing.setExcerpt(incoming.getExcerpt());
        existing.setContent(incoming.getContent());
        existing.setTags(tagService.parseTags(tagsInput));
        postService.save(existing);

        return "redirect:/posts";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        postService.delete(id);
        return "redirect:/posts";
    }

    @GetMapping("/createtag")
    public String create(Model model) {
        model.addAttribute("post", new Post());
        model.addAttribute("tags", tagService.getAllTags());
        return "posts/create";
    }

    @GetMapping("/search")
    public String searchResult(
            @RequestParam(required = false) String action,
            @RequestParam String type,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String[] authorIds,
            @RequestParam(required = false) String tags,
            Model model) {

        if ("search".equals(action) || "filter".equals(action)) page = 0;

        LocalDateTime from = postService.parseFromDate(fromDate);
        LocalDateTime to = postService.parseToDate(toDate);
        List<String> tagList = tagService.parseTagNames(tags);
        if (tagList == null || tagList.isEmpty())
            tagList = null;

        List<Long> selectedAuthorIds = postService.parseAuthorIds(authorIds);
        List<User> authors = postService.getDistinctAuthorDetails();

        if (selectedAuthorIds == null || selectedAuthorIds.isEmpty())
            selectedAuthorIds = null;

        model.addAttribute("keyword", keyword);
        keyword = (keyword == null || keyword.isBlank())
                ? null
                : "%" + keyword.toLowerCase() + "%";

        Page<Post> result = postService.search(
                type,
                keyword,
                page,
                size,
                sortBy,
                from,
                to,
                selectedAuthorIds,
                tagList
        );

        model.addAttribute("authors", authors);
        model.addAttribute("posts", result.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("type", type);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", to);
        model.addAttribute("tags", tags);
        model.addAttribute("selectedAuthors", selectedAuthorIds);
        model.addAttribute("totalItems", result.getTotalElements());

        return "posts/list";
    }
}
