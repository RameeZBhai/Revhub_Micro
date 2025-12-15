package com.revhub.post.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PostProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "post-events";

    public void sendEvent(String type, String actor, String targetUser, String postId) {
        // Payload: {"type": "LIKE", "actor": "user1", "target": "author", "postId":
        // "123"}
        String message = String.format("{\"type\":\"%s\", \"actor\":\"%s\", \"target\":\"%s\", \"postId\":\"%s\"}",
                type, actor, targetUser, postId);
        // Only send if actor is not target (don't notify self)
        if (!actor.equals(targetUser)) {
            try {
                kafkaTemplate.send(TOPIC, message);
            } catch (Exception e) {
                System.err.println("Error sending Kafka event: " + e.getMessage());
            }
        }
    }
}
