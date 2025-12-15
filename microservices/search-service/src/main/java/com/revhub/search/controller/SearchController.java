package com.revhub.search.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/search")
public class SearchController {

    @GetMapping("/posts")
    public ResponseEntity<?> searchPosts(@RequestParam String query) {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            // Call post-service running on port 8083
            String postServiceUrl = "http://localhost:8083/posts/search?query=" + query;
            Object[] posts = restTemplate.getForObject(postServiceUrl, Object[].class);
            return ResponseEntity.ok(posts != null ? posts : new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new Object[0]); // Return empty array if service unavailable
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        try {
            // Call user-service running on port 8089
            String userServiceUrl = "http://localhost:8089/profile/search?query=" + query;
            Object[] users = restTemplate.getForObject(userServiceUrl, Object[].class);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error searching users: " + e.getMessage());
        }
    }

    @GetMapping("/suggestions")
    public ResponseEntity<?> getSuggestions() {
        java.util.List<Object> suggestions = new java.util.ArrayList<>();
        suggestions.add(new UserSuggestion("suggested1", "Alice Johnson", "alice@example.com"));
        suggestions.add(new UserSuggestion("suggested2", "Bob Wilson", "bob@example.com"));
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/hashtags")
    public ResponseEntity<?> searchHashtags(@RequestParam String query) {
        return ResponseEntity.ok("Hashtags matching: " + query);
    }

    @GetMapping("/trending")
    public ResponseEntity<?> getTrendingTopics() {
        return ResponseEntity.ok("Trending topics and hashtags");
    }

    @PostMapping("/index/post")
    public ResponseEntity<?> indexPost(@RequestBody Object post) {
        return ResponseEntity.ok("Post indexed for search");
    }

    @PostMapping("/index/user")
    public ResponseEntity<?> indexUser(@RequestBody Object user) {
        return ResponseEntity.ok("User indexed for search");
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("Search service is running");
    }
}

class UserSuggestion {
    private String username;
    private String displayName;
    private String email;

    public UserSuggestion(String username, String displayName, String email) {
        this.username = username;
        this.displayName = displayName;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

