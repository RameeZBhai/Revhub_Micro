package com.revhub.follow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class FollowProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "user-events";

    public void sendFollowEvent(String actor, String target) {
        String message = String.format("{\"type\":\"FOLLOW\", \"actor\":\"%s\", \"target\":\"%s\"}", actor, target);
        kafkaTemplate.send(TOPIC, message);
    }
}
