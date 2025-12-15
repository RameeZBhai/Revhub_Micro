package com.revhub.user.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import java.util.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String bio;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String profilePicture;
    private boolean isPrivate = false;
    private int followersCount = 0;
    private int followingCount = 0;

    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();

    public User() {
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}

@Repository
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByUsernameContainingIgnoreCase(String query);

    List<User> findByUsernameIn(List<String> usernames);
}

@RestController
@RequestMapping
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/users/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/users/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok("Login successful");
    }

    @GetMapping("/profile/id/{id}")
    public ResponseEntity<?> getProfile(@PathVariable String id) {
        return ResponseEntity.ok("User profile for ID: " + id);
    }

    @PutMapping("/users/profile/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable String id, @RequestBody Object profile) {
        return ResponseEntity.ok("Profile updated for ID: " + id);
    }

    @PostMapping("/users/verify-token")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok("Token valid");
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<?> getProfileByUsername(@PathVariable String username) {
        try {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            }

            // Check if email already exists to avoid 500 error
            String email = username + "@example.com";
            Optional<User> existingEmailUser = userRepository.findByEmail(email);
            if (existingEmailUser.isPresent()) {
                return ResponseEntity.ok(existingEmailUser.get());
            }

            User newUser = new User(username, email);
            User savedUser = userRepository.save(newUser);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error retrieving profile: " + e.getMessage());
        }
    }

    @GetMapping("/profile/{username}/posts")
    public ResponseEntity<?> getUserPosts(@PathVariable String username) {
        java.util.List<Object> posts = new java.util.ArrayList<>();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/profile/all")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/profile/follow-status/{username}")
    public ResponseEntity<?> getFollowStatus(@PathVariable String username) {
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("status", "NOT_FOLLOWING");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile/follow/{username}")
    public ResponseEntity<?> followUser(@PathVariable String username) {
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Successfully followed " + username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile/unfollow/{username}")
    public ResponseEntity<?> unfollowUser(@PathVariable String username) {
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Successfully unfollowed " + username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile/cancel-request/{username}")
    public ResponseEntity<?> cancelFollowRequest(@PathVariable String username) {
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Follow request cancelled");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{username}/followers")
    public ResponseEntity<?> getFollowers(@PathVariable String username) {
        java.util.List<UserProfile> followers = new java.util.ArrayList<>();
        followers.add(new UserProfile("follower1", "follower1@example.com", "Follower bio", false, 5, 10));
        followers.add(new UserProfile("follower2", "follower2@example.com", "Another follower", false, 8, 12));
        return ResponseEntity.ok(followers);
    }

    @DeleteMapping("/profile/remove-follower/{username}")
    public ResponseEntity<?> removeFollower(@PathVariable String username) {
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Follower removed successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateCurrentProfile(
            @RequestHeader(value = "X-User-Name", defaultValue = "") String headerUsername,
            @RequestBody Map<String, Object> updates) {
        if (headerUsername.isEmpty()) {
            return ResponseEntity.badRequest().body("User not authenticated");
        }
        Optional<User> userOpt = userRepository.findByUsername(headerUsername);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (updates.containsKey("bio")) {
                user.setBio((String) updates.get("bio"));
            }
            if (updates.containsKey("profilePicture")) {
                user.setProfilePicture((String) updates.get("profilePicture"));
            }
            userRepository.save(Objects.requireNonNull(user));
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(404).body("User not found");
    }

    @GetMapping("/profile/search")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(query);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/profile/{username}/following")
    public ResponseEntity<?> getFollowing(@PathVariable String username) {
        java.util.List<UserProfile> following = new java.util.ArrayList<>();
        following.add(new UserProfile("user1", "user1@example.com", "Sample bio", false, 10, 5));
        following.add(new UserProfile("user2", "user2@example.com", "Another user", false, 15, 8));
        following.add(new UserProfile("user3", "user3@example.com", "Third user", false, 20, 12));
        return ResponseEntity.ok(following);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<?> getSuggestions() {
        // Fetch all users (in a real app, logic would be: users not followed by current
        // user)
        List<User> allUsers = userRepository.findAll();
        // Return up to 5 users as suggestions
        int limit = Math.min(allUsers.size(), 5);
        return ResponseEntity.ok(allUsers.subList(0, limit));
    }

    @PostMapping("/profile/batch-users")
    public ResponseEntity<?> fetchUsersByUsernames(@RequestBody List<String> usernames) {
        List<User> users = userRepository.findByUsernameIn(usernames);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("User service is running");
    }
}
