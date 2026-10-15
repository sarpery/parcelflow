package com.parcelflow.parcelflow.kafka;

import com.parcelflow.parcelflow.event.ShipmentStatusChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ShipmentStatusEventHandler {

    private final ShipmentEventProducer shipmentEventProducer;

    public ShipmentStatusEventHandler(ShipmentEventProducer shipmentEventProducer) {
        this.shipmentEventProducer = shipmentEventProducer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleShipmentStatusChanged(ShipmentStatusChangedEvent event) {
        shipmentEventProducer.publish(event);
    }
}