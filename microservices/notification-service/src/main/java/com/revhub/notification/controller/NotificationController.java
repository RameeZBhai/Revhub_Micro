package com.revhub.notification.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Repository;
import org.springframework.data.annotation.Id;
import java.util.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.kafka.annotation.KafkaListener;

@Document(collection = "notifications")
class Notification {
    @Id
    private String id;
    private String userId;
    private String message;
    private String type;
    private boolean readStatus = false;
    private LocalDateTime createdDate = LocalDateTime.now();

    public Notification() {
    }

    public Notification(String userId, String message, String type) {
        this.userId = userId;
        this.message = message;
        this.type = type;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}

@Repository
interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdOrderByCreatedDateDesc(String userId);

    long countByUserIdAndReadStatus(String userId, boolean readStatus);

    List<Notification> findByUserIdAndReadStatusFalse(String userId);
}

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @RequestHeader(value = "X-User-Name", defaultValue = "") String username) {
        if (username.isEmpty())
            return ResponseEntity.status(401).body("User not authenticated");
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedDateDesc(username);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getNotificationsForUser(@PathVariable String userId) {
        return ResponseEntity.ok("Notifications for user: " + userId);
    }

    @PostMapping
    public ResponseEntity<?> createNotification(@RequestBody NotificationRequest notificationRequest) {
        Notification notification = new Notification(notificationRequest.getUserId(), notificationRequest.getMessage(),
                notificationRequest.getType());
        Notification savedNotification = notificationRepository.save(notification);
        return ResponseEntity.ok(savedNotification);
    }

    @PostMapping("/push")
    public ResponseEntity<?> sendPushNotification(@RequestBody PushRequest request) {
        return ResponseEntity.ok("Push notification sent to: " + request.getUserId());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable String id) {
        return ResponseEntity.ok("Notification marked as read: " + id);
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@RequestHeader(value = "X-User-Name", defaultValue = "") String username) {
        if (username.isEmpty())
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));

        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndReadStatusFalse(username);
        unreadNotifications.forEach(n -> n.setReadStatus(true));
        notificationRepository.saveAll(unreadNotifications);

        return ResponseEntity.ok(Map.of("message", "All notifications marked as read", "count", unreadNotifications.size()));
    }

    @PostMapping("/alert")
    public ResponseEntity<?> sendAlert(@RequestBody AlertRequest request) {
        return ResponseEntity.ok("Alert sent: " + request.getMessage());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@RequestHeader(value = "X-User-Name", defaultValue = "") String username) {
        if (username.isEmpty())
            return ResponseEntity.status(401).body("User not authenticated");
        long count = notificationRepository.countByUserIdAndReadStatus(username, false);
        return ResponseEntity.ok("{\"count\": " + count + "}");
    }

    @KafkaListener(topics = "chat-messages", groupId = "notification-group")
    public void handleChatMessage(String message) {
        try {
            Map<String, String> payload = objectMapper.readValue(message, new TypeReference<Map<String, String>>() {
            });
            String sender = payload.get("sender");
            String receiver = payload.get("receiver");

            Notification notification = new Notification(receiver, sender + " messaged you", "CHAT");
            notificationRepository.save(notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void handleUserEvent(String message) {
        try {
            Map<String, String> payload = objectMapper.readValue(message, new TypeReference<Map<String, String>>() {
            });
            String type = payload.get("type");
            String actor = payload.get("actor");
            String target = payload.get("target");

            if ("FOLLOW".equals(type)) {
                Notification notification = new Notification(target, actor + " started following u", "FOLLOW");
                notificationRepository.save(notification);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "post-events", groupId = "notification-group")
    public void handlePostEvent(String message) {
        try {
            Map<String, String> payload = objectMapper.readValue(message, new TypeReference<Map<String, String>>() {
            });
            String type = payload.get("type");
            String actor = payload.get("actor");
            String target = payload.get("target");

            String msgText = "";
            if ("LIKE".equals(type)) {
                msgText = actor + " liked ur post";
            } else if ("COMMENT".equals(type)) {
                msgText = actor + " commented on ur post";
            } else if ("SHARE".equals(type)) {
                msgText = actor + " shared your post";
            }

            if (!msgText.isEmpty()) {
                Notification notification = new Notification(target, msgText, type);
                notificationRepository.save(notification);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("Notification service is running");
    }
}

class NotificationRequest {
    private String userId;
    private String message;
    private String type;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

class PushRequest {
    private String userId;
    private String title;
    private String body;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

class AlertRequest {
    private String message;
    private String severity;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}

class NotificationResponse {
    private String id;
    private String message;
    private String type;
    private boolean readStatus;

    public NotificationResponse(String id, String message, String type, boolean readStatus) {
        this.id = id;
        this.message = message;
        this.type = type;
        this.readStatus = readStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }
}