package com.parcelflow.parcelflow.dto;

import com.parcelflow.parcelflow.domain.Shipment;
import com.parcelflow.parcelflow.domain.ShipmentStatus;

import java.time.LocalDateTime;

public record ShipmentResponse(
        Long id,
        String trackingNumber,
        String senderName,
        String recipientName,
        String origin,
        String destination,
        ShipmentStatus status,
        LocalDateTime createdAt,
        LocalDateTime estimatedDelivery
) {

    public static ShipmentResponse from(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getSenderName(),
                shipment.getRecipientName(),
                shipment.getOrigin(),
                shipment.getDestination(),
                shipment.getStatus(),
                shipment.getCreatedAt(),
                shipment.getEstimatedDelivery()
        );
    }
}