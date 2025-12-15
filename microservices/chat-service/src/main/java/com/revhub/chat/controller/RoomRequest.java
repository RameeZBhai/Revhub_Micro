package com.revhub.chat.controller;

public class RoomRequest {
    private String[] userIds;
    private String roomName;
    
    public String[] getUserIds() { return userIds; }
    public void setUserIds(String[] userIds) { this.userIds = userIds; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
}