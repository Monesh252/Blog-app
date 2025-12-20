package com.spring.postify.service;

import com.spring.postify.entity.Tag;
import com.spring.postify.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Tag getTag(Long id){
        return tagRepository.findById(id).orElseThrow();
    }

    public Tag getOrCreateTag(String name){
        return tagRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Tag t = new Tag();
                    t.setName(name);
                    t.setCreatedAt(LocalDateTime.now());
                    t.setUpdatedAt(LocalDateTime.now());
                    return tagRepository.save(t);
                });
    }
}