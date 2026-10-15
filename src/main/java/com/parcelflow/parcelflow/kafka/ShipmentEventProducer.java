package com.parcelflow.parcelflow.kafka;

import com.parcelflow.parcelflow.event.ShipmentStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ShipmentEventProducer {

    private static final Logger logger =
            LoggerFactory.getLogger(ShipmentEventProducer.class);

    private static final String TOPIC = "shipment-status-changed";

    private final KafkaTemplate<String, ShipmentStatusChangedEvent> kafkaTemplate;

    public ShipmentEventProducer(
            KafkaTemplate<String, ShipmentStatusChangedEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ShipmentStatusChangedEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event.shipmentId().toString(),
                event
        ).whenComplete((result, exception) -> {

            if (exception != null) {
                logger.error(
                        "Failed to publish shipment status event",
                        exception
                );
            } else {
                logger.info(
                        "Published shipment event: {} -> {}",
                        event.previousStatus(),
                        event.newStatus()
                );
            }
        });
    }
}