package com.revhub.post.controller;

import com.revhub.post.entity.Comment;
import com.revhub.post.entity.Post;
import com.revhub.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import jakarta.annotation.PostConstruct;
import java.security.Principal;

@RestController
@RequestMapping("/posts")
public class SimplePostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private com.revhub.post.service.PostProducer postProducer;

    @org.springframework.beans.factory.annotation.Value("${app.upload-dir}")
    private String uploadDir;

    private final AtomicLong commentIdGenerator = new AtomicLong(1);

    @PostConstruct
    public void init() {
        try {
            if (uploadDir != null && !uploadDir.isEmpty()) {
                java.nio.file.Paths.get(uploadDir).toFile().mkdirs();
            }
        } catch (Exception e) {
            System.err.println("Failed to create upload directory: " + e.getMessage());
            e.printStackTrace();
        }

        // Comment ID generator initialization
        long maxCommentId = postRepository.findAll().stream()
                .flatMap(p -> p.getComments().stream())
                .mapToLong(c -> c.getId() != null ? c.getId() : 0)
                .max().orElse(0);

        // Also check replies
        long maxReplyId = postRepository.findAll().stream()
                .flatMap(p -> p.getComments().stream())
                .flatMap(c -> c.getReplies().stream())
                .mapToLong(r -> r.getId() != null ? r.getId() : 0)
                .max().orElse(0);

        commentIdGenerator.set(Math.max(maxCommentId, maxReplyId) + 1);
    }

    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        List<Post> allPosts = postRepository.findAll();
        System.out.println("=== GETTING ALL POSTS ===");
        System.out.println("Total posts found: " + allPosts.size());
        for (Post post : allPosts) {
            System.out.println("Post ID: " + post.getId() + ", Author: " + post.getAuthor() + ", Content: " + post.getContent());
        }
        
        // Sort by creation date (newest first)
        allPosts.sort((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()));

        Map<String, Object> response = new HashMap<>();
        response.put("content", allPosts);
        response.put("totalElements", allPosts.size());
        response.put("totalPages", 1);
        response.put("size", 10);
        response.put("number", 0);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Map<String, String> postData, Principal principal) {
        Post post = new Post();
        // MongoDB will generate String ID automatically
        post.setContent(postData.get("content"));
        String author = principal != null ? principal.getName() : "anonymous";
        post.setAuthor(author);
        post.setImageUrl(postData.getOrDefault("imageUrl", ""));
        post.setCreatedDate(Instant.now().toString());

        System.out.println("=== CREATING POST ===");
        System.out.println("Author: " + author);
        System.out.println("Content: " + post.getContent());
        System.out.println("Principal: " + (principal != null ? principal.getName() : "null"));

        postRepository.save(post);
        System.out.println("Post saved with ID: " + post.getId());

        return ResponseEntity.ok(post);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> createPostWithFile(
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Principal principal) {

        String imageUrl = "";
        // We need an ID for filename? Use timestamp + random or just temp
        String tempIdForFile = UUID.randomUUID().toString();

        if (file != null && !file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String fileName = System.currentTimeMillis() + "_" + tempIdForFile + extension;
                File dest = new File(uploadDir, fileName);
                file.transferTo(dest);

                imageUrl = "/api/posts/images/" + fileName;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Post post = new Post();
        post.setContent(content);
        String author = principal != null ? principal.getName() : "anonymous";
        post.setAuthor(author);
        post.setImageUrl(imageUrl);
        post.setCreatedDate(Instant.now().toString());

        postRepository.save(post);

        return ResponseEntity.ok(post);
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> getImage(@PathVariable String filename) {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(uploadDir, filename);
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(
                    Objects.requireNonNull(path.toUri()));

            if (resource.exists() || resource.isReadable()) {
                String contentType = java.nio.file.Files.probeContentType(path);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, contentType)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/toggle-like")
    public ResponseEntity<?> toggleLike(@PathVariable String id, Principal principal) {
        Optional<Post> postOpt = postRepository.findById(Objects.requireNonNull(id));
        if (postOpt.isEmpty())
            return ResponseEntity.notFound().build();

        Post post = postOpt.get();
        String username = principal != null ? principal.getName() : "anonymous";

        Set<String> likes = post.getLikes();

        boolean isLiked;
        if (likes.contains(username)) {
            likes.remove(username);
            isLiked = false;
        } else {
            likes.add(username);
            isLiked = true;
            // Send Notification
            postProducer.sendEvent("LIKE", username, post.getAuthor(), post.getId());
        }

        post.setLikesCount(likes.size());
        postRepository.save(Objects.requireNonNull(post));

        Map<String, Object> response = new HashMap<>();
        response.put("likesCount", likes.size());
        response.put("isLiked", isLiked);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable String id, @RequestBody Map<String, String> commentData,
            Principal principal) {
        Optional<Post> postOpt = postRepository.findById(Objects.requireNonNull(id));
        if (postOpt.isEmpty())
            return ResponseEntity.notFound().build();

        Post post = postOpt.get();
        String username = principal != null ? principal.getName() : "anonymous";
        String content = commentData.get("content");

        Comment comment = new Comment();
        comment.setId(commentIdGenerator.getAndIncrement());
        comment.setContent(content);
        comment.setAuthor(username);
        comment.setTimestamp(Instant.now().toString());

        post.getComments().add(comment);
        post.setCommentsCount(post.getComments().size()); // Update simplified count
        post.setCommentsCount(post.getComments().size()); // Update simplified count
        postRepository.save(Objects.requireNonNull(post));

        // Send Notification
        postProducer.sendEvent("COMMENT", username, post.getAuthor(), post.getId());

        return ResponseEntity.ok(comment);
    }

    @PostMapping("/{postId}/comments/{commentId}/reply")
    public ResponseEntity<?> addReply(@PathVariable String postId, @PathVariable Long commentId,
            @RequestBody Map<String, String> replyData, Principal principal) {
        Optional<Post> postOpt = postRepository.findById(Objects.requireNonNull(postId));
        if (postOpt.isEmpty())
            return ResponseEntity.notFound().build();

        Post post = postOpt.get();
        String username = principal != null ? principal.getName() : "anonymous";
        String content = replyData.get("content");

        for (Comment comment : post.getComments()) {
            if (comment.getId().equals(commentId)) {
                Comment reply = new Comment();
                reply.setId(commentIdGenerator.getAndIncrement());
                reply.setContent(content);
                reply.setAuthor(username);
                reply.setTimestamp(Instant.now().toString());

                comment.getReplies().add(reply);
                postRepository.save(Objects.requireNonNull(post));
                return ResponseEntity.ok(reply);
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{postId}/comments/{commentId}/replies/{replyId}")
    public ResponseEntity<?> deleteReply(@PathVariable String postId, @PathVariable Long commentId,
            @PathVariable Long replyId) {
        Optional<Post> postOpt = postRepository.findById(Objects.requireNonNull(postId));
        if (postOpt.isEmpty())
            return ResponseEntity.notFound().build();

        Post post = postOpt.get();
        for (Comment comment : post.getComments()) {
            if (comment.getId().equals(commentId)) {
                if (comment.getReplies().removeIf(r -> r.getId().equals(replyId))) {
                    postRepository.save(Objects.requireNonNull(post));
                    return ResponseEntity.ok("Reply deleted");
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable String id) {
        Optional<Post> postOpt = postRepository.findById(Objects.requireNonNull(id));
        if (postOpt.isEmpty())
            return ResponseEntity.ok(new ArrayList<>());
        return ResponseEntity.ok(postOpt.get().getComments());
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable String postId, @PathVariable Long commentId) {
        Optional<Post> postOpt = postRepository.findById(Objects.requireNonNull(postId));
        if (postOpt.isEmpty())
            return ResponseEntity.notFound().build();

        Post post = postOpt.get();
        if (post.getComments().removeIf(c -> c.getId().equals(commentId))) {
            post.setCommentsCount(post.getComments().size());
            postRepository.save(Objects.requireNonNull(post));
            return ResponseEntity.ok("Comment deleted");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable String id) {
        postRepository.deleteById(Objects.requireNonNull(id));
        return ResponseEntity.ok("Post deleted: " + id);
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<?> sharePost(@PathVariable String id, Principal principal) {
        Optional<Post> postOpt = postRepository.findById(Objects.requireNonNull(id));
        Map<String, Object> response = new HashMap<>();
        String username = principal != null ? principal.getName() : "anonymous";

        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.setSharesCount(post.getSharesCount() + 1);
            postRepository.save(Objects.requireNonNull(post));
            // Send Notification
            // Assuming current user is share actor. Wait, sharePost doesn't have principal?
            // User context is missing in sharePost method signature.
            // I'll skip current user check or assume anonymous if not provided, but
            // Notifications need actor.
            // I will update signature to include Principal.
            response.put("sharesCount", post.getSharesCount());

            // Send Notification
            postProducer.sendEvent("SHARE", username, post.getAuthor(), post.getId());
        } else {
            response.put("sharesCount", 0);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUserPosts(@PathVariable String username) {
        List<Post> allPosts = postRepository.findAll();
        List<Post> userPosts = new ArrayList<>();

        for (Post post : allPosts) {
            if (username.equals(post.getAuthor())) {
                userPosts.add(post);
            }
        }
        return ResponseEntity.ok(userPosts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable String id, @RequestBody Map<String, String> postData) {
        Optional<Post> postOpt = postRepository.findById(Objects.requireNonNull(id));
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.setContent(postData.get("content"));
            postRepository.save(Objects.requireNonNull(post));
            return ResponseEntity.ok(post);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchPosts(@RequestParam String query) {
        List<Post> allPosts = postRepository.findAll();
        List<Post> matchingPosts = new ArrayList<>();
        
        String lowerQuery = query.toLowerCase();
        for (Post post : allPosts) {
            if (post.getContent() != null && post.getContent().toLowerCase().contains(lowerQuery)) {
                matchingPosts.add(post);
            }
        }
        
        // Sort by creation date (newest first)
        matchingPosts.sort((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()));
        
        return ResponseEntity.ok(matchingPosts);
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("Post service is running");
    }
}