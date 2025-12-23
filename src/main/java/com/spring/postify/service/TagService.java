package com.spring.postify.service;

import com.spring.postify.entity.Tag;
import com.spring.postify.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TagService {

    private TagRepository tagRepository;

    public TagService(TagRepository tagRepository){
        this.tagRepository = tagRepository;
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Tag getOrCreateTag(String name) {
        return tagRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Tag t = new Tag();
                    t.setName(name);
                    t.setCreatedAt(LocalDateTime.now());
                    t.setUpdatedAt(LocalDateTime.now());
                    return tagRepository.save(t);
                });
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

    public Set<Tag> parseTags(String tagsInput) {

        Set<Tag> tags = new HashSet<>();

        if (tagsInput == null || tagsInput.isBlank())
            return tags;

        for (String t : tagsInput.split("#")) {
            String trimmed = t.trim();
            if (!trimmed.isEmpty()) {
                tags.add(getOrCreateTag(trimmed));
            }
        }

        return tags;
    }
}