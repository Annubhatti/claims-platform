package com.chubb.claims.claim.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ClaimEventPublisher {

    private static final String CLAIM_EVENTS_TOPIC = "claim-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ClaimEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ClaimEvent event) {

        kafkaTemplate.send(
                CLAIM_EVENTS_TOPIC,
                event.claimId().toString(),
                event
        );
    }
}