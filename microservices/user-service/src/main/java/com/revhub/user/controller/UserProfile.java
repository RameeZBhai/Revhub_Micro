package com.revhub.user.controller;

public class UserProfile {
    private String username;
    private String email;
    private String bio;
    private String profilePicture;
    private boolean isPrivate;
    private int followersCount;
    private int followingCount;

    public UserProfile(String username, String email, String bio, boolean isPrivate, int followersCount,
            int followingCount) {
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.profilePicture = null;
        this.isPrivate = isPrivate;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
    }

    public UserProfile(String username, String email, String bio, String profilePicture, boolean isPrivate,
            int followersCount, int followingCount) {
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.profilePicture = profilePicture;
        this.isPrivate = isPrivate;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
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

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}