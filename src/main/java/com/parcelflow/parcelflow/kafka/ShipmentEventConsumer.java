package com.parcelflow.parcelflow.kafka;

import com.parcelflow.parcelflow.event.ShipmentStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ShipmentEventConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(ShipmentEventConsumer.class);

    @KafkaListener(topics = "shipment-status-changed")
    public void consume(ShipmentStatusChangedEvent event) {

        logger.info(
                "Received shipment status event: shipment={}, {} -> {} at {}",
                event.trackingNumber(),
                event.previousStatus(),
                event.newStatus(),
                event.occurredAt()
        );
    }
}