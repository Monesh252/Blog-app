package com.spring.postify.controller;

import com.spring.postify.entity.User;
import com.spring.postify.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model){
        model.addAttribute("users", userService.getAllUsers());
        return "users/list";
    }

    @GetMapping("/create")
    public String createForm(Model model){
        model.addAttribute("user", new User());
        return "users/create";
    }

    @PostMapping
    public String save(@ModelAttribute User user){
        userService.save(user);
        return "users/success";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model){
        model.addAttribute("user", userService.getUser(id));
        return "users/edit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute User user){
        userService.update(id, user);
        return "redirect:/users";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id){
        userService.delete(id);
        return "redirect:/users";
    }
}