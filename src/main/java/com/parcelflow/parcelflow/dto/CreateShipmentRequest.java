package com.parcelflow.parcelflow.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record CreateShipmentRequest(

        @NotBlank
        String senderName,

        @NotBlank
        String recipientName,

        @NotBlank
        String origin,

        @NotBlank
        String destination,

        LocalDateTime estimatedDelivery

) {
}