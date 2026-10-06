package com.parcelflow.parcelflow.exception;

import com.parcelflow.parcelflow.domain.Shipment;
import com.parcelflow.parcelflow.domain.ShipmentStatus;

public class InvalidShipmentStatusException extends RuntimeException {
    public InvalidShipmentStatusException(ShipmentStatus currentStatus, ShipmentStatus newStatus) {
        super("The attempted shipment status update is invalid. "+ currentStatus +" -> " + newStatus + " is not a valid status update.");
    }
}
