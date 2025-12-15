package com.revhub.follow.controller;

import com.revhub.follow.model.Follow;
import com.revhub.follow.repository.FollowRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private com.revhub.follow.service.FollowProducer followProducer;

    @PostMapping("/{userId}/follow/{targetUserId}")
    public ResponseEntity<?> followUser(@PathVariable String userId, @PathVariable String targetUserId) {
        // Check if already following
        if (followRepository.existsByFollowerUsernameAndFollowingUsername(userId, targetUserId)) {
            return ResponseEntity.ok(Map.of("message", "Already following"));
        }

        Follow follow = new Follow(userId, targetUserId);
        followRepository.save(follow);

        try {
            followProducer.sendFollowEvent(userId, targetUserId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(Map.of("message", "Follow request sent"));
    }

    @DeleteMapping("/{userId}/unfollow/{targetUserId}")
    @Transactional
    public ResponseEntity<?> unfollowUser(@PathVariable String userId, @PathVariable String targetUserId) {
        followRepository.deleteByFollowerUsernameAndFollowingUsername(userId, targetUserId);
        return ResponseEntity.ok(Map.of("message", "Unfollowed successfully"));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<?> getFollowers(@PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Follow> follows = followRepository.findByFollowingUsername(userId,
                pageable);
        List<String> followers = follows.stream()
                .map(Follow::getFollowerUsername)
                .collect(Collectors.toList());
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<?> getFollowing(@PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Follow> follows = followRepository.findByFollowerUsername(userId,
                pageable);
        List<String> following = follows.stream()
                .map(Follow::getFollowingUsername)
                .collect(Collectors.toList());
        return ResponseEntity.ok(following);
    }

    @GetMapping("/{userId}/stats")
    public ResponseEntity<?> getFollowStats(@PathVariable String userId) {
        long followersCount = followRepository.countByFollowingUsername(userId);
        long followingCount = followRepository.countByFollowerUsername(userId);
        return ResponseEntity.ok(Map.of(
                "followersCount", followersCount,
                "followingCount", followingCount));
    }

    @GetMapping("/{userId}/status/{targetUserId}")
    public ResponseEntity<?> getFollowStatus(@PathVariable String userId, @PathVariable String targetUserId) {
        boolean isFollowing = followRepository.existsByFollowerUsernameAndFollowingUsername(userId, targetUserId);
        String status = isFollowing ? "ACCEPTED" : "NOT_FOLLOWING";
        return ResponseEntity.ok(Map.of("status", status));
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("Follow service is running");
    }
}