package com.parcelflow.parcelflow.dto;

import com.parcelflow.parcelflow.domain.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull ShipmentStatus status) {
}
