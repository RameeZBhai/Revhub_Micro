package com.revhub.chat.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Repository;
import org.springframework.data.annotation.Id;
import java.util.*;
import java.time.LocalDateTime;

@Document(collection = "messages")
class Message {
    @Id
    private String id;
    private String senderUsername;
    private String receiverUsername;
    private String content;
    private LocalDateTime timestamp = LocalDateTime.now();

    private boolean read = false;

    public Message() {
    }

    public Message(String senderUsername, String receiverUsername, String content) {
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.content = content;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}

@Repository
interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findBySenderUsernameOrReceiverUsernameOrderByTimestampDesc(String sender, String receiver);

    List<Message> findByReceiverUsernameAndReadFalse(String receiverUsername);

    List<Message> findBySenderUsernameAndReceiverUsernameAndReadFalse(String sender, String receiver);

    @org.springframework.data.mongodb.repository.Query("{ $or: [ { 'senderUsername': ?0, 'receiverUsername': ?1 }, { 'senderUsername': ?1, 'receiverUsername': ?0 } ] }")
    List<Message> findConversation(String user1, String user2, org.springframework.data.domain.Sort sort);
}

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private com.revhub.chat.service.ChatProducer chatProducer;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/conversation/{username}")
    public ResponseEntity<?> getConversation(@PathVariable String username,
            @RequestHeader(value = "X-User-Name", defaultValue = "") String currentUser) {
        if (currentUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User not authenticated");
        }
        List<Message> messages = messageRepository.findConversation(currentUser, username,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC,
                        "timestamp"));
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getTotalUnreadCount(
            @RequestHeader(value = "X-User-Name", defaultValue = "") String currentUser) {
        if (currentUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User not authenticated");
        }
        List<Message> unreadMessages = messageRepository.findByReceiverUsernameAndReadFalse(currentUser);
        long uniqueSenders = unreadMessages.stream().map(Message::getSenderUsername).distinct().count();
        return ResponseEntity.ok(uniqueSenders);
    }

    @GetMapping("/unread-count/{username}")
    public ResponseEntity<?> getUnreadCountFromUser(@PathVariable String username,
            @RequestHeader(value = "X-User-Name", defaultValue = "") String currentUser) {
        if (currentUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User not authenticated");
        }
        List<Message> unreadMessages = messageRepository.findBySenderUsernameAndReceiverUsernameAndReadFalse(username,
                currentUser);
        return ResponseEntity.ok(unreadMessages.size());
    }

    @PostMapping("/mark-read/{username}")
    public ResponseEntity<?> markAsRead(@PathVariable String username,
            @RequestHeader(value = "X-User-Name", defaultValue = "") String currentUser) {
        if (currentUser.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }
        List<Message> unreadMessages = messageRepository.findBySenderUsernameAndReceiverUsernameAndReadFalse(username,
                currentUser);
        unreadMessages.forEach(msg -> msg.setRead(true));
        messageRepository.saveAll(unreadMessages);
        return ResponseEntity.ok(Map.of("message", "Messages marked as read", "count", unreadMessages.size()));
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessage chatMessage,
            @RequestHeader(value = "X-User-Name", defaultValue = "") String senderUsername) {
        if (senderUsername.isEmpty()) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        Message message = new Message(senderUsername, chatMessage.getReceiverUsername(), chatMessage.getContent());
        Message savedMessage = messageRepository.save(message);

        // Send real-time message via WebSocket
        try {
            messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiverUsername(), 
                "/queue/messages", 
                savedMessage
            );
        } catch (Exception e) {
            System.err.println("Error sending WebSocket message: " + e.getMessage());
        }

        // Publish to Kafka
        try {
            String kafkaMessage = String.format("{\"sender\":\"%s\",\"receiver\":\"%s\",\"content\":\"%s\"}",
                    message.getSenderUsername(), message.getReceiverUsername(), message.getContent());
            chatProducer.sendMessage(kafkaMessage);
        } catch (Exception e) {
            System.err.println("Error sending to Kafka: " + e.getMessage());
        }

        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping("/contacts/{userId}")
    public ResponseEntity<?> getContacts(@PathVariable String userId) {
        return ResponseEntity.ok("Contacts for user: " + userId);
    }

    @GetMapping("/contacts")
    public ResponseEntity<?> getAllContacts(@RequestHeader(value = "X-User-Name", defaultValue = "") String username) {
        if (username.isEmpty()) {
            return ResponseEntity.badRequest().body("User not authenticated");
        }

        List<Message> messages = messageRepository.findBySenderUsernameOrReceiverUsernameOrderByTimestampDesc(username,
                username);
        Set<String> contacts = new HashSet<>();

        for (Message msg : messages) {
            if (msg.getSenderUsername() == null || msg.getReceiverUsername() == null) {
                continue;
            }
            if (msg.getSenderUsername().equals(username)) {
                contacts.add(msg.getReceiverUsername());
            } else {
                contacts.add(msg.getSenderUsername());
            }
        }

        return ResponseEntity.ok(new ArrayList<>(contacts));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<?> getChatRoom(@PathVariable String roomId) {
        return ResponseEntity.ok("Chat room: " + roomId);
    }

    @PostMapping("/room/create")
    public ResponseEntity<?> createChatRoom(@RequestBody RoomRequest request) {
        return ResponseEntity.ok("Chat room created between users");
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("Chat service is running");
    }
}
