package com.parcelflow.parcelflow.event;

import com.parcelflow.parcelflow.domain.ShipmentStatus;

import java.time.Instant;

public record ShipmentStatusChangedEvent(
        Long shipmentId,
        String trackingNumber,
        ShipmentStatus previousStatus,
        ShipmentStatus newStatus,
        Instant occurredAt
) {}
