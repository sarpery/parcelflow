package com.parcelflow.parcelflow.service;

import com.parcelflow.parcelflow.domain.Shipment;
import com.parcelflow.parcelflow.dto.CreateShipmentRequest;
import com.parcelflow.parcelflow.exception.InvalidShipmentStatusException;
import com.parcelflow.parcelflow.exception.ShipmentNotFoundException;
import com.parcelflow.parcelflow.repository.ShipmentRepository;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import com.parcelflow.parcelflow.domain.ShipmentStatus;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    public ShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    public Shipment createShipment(CreateShipmentRequest request){
        Shipment shipment = new Shipment();

        shipment.setTrackingNumber(generateTrackingNumber());
        shipment.setSenderName(request.senderName());
        shipment.setRecipientName(request.recipientName());
        shipment.setOrigin(request.origin());
        shipment.setDestination(request.destination());
        shipment.setEstimatedDelivery(request.estimatedDelivery());

        shipment.setStatus(ShipmentStatus.CREATED);
        shipment.setCreatedAt(LocalDateTime.now());

        return shipmentRepository.save(shipment);
    }

    private String generateTrackingNumber() {
        return "PF-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    public List<Shipment> getAllShipments(){
        return shipmentRepository.findAll();
    }

    public Shipment getShipment(Long id){
        return shipmentRepository.findById(id)
                .orElseThrow(() ->
                        new ShipmentNotFoundException(id));
    }

    public Shipment updateStatus(Long shipmentId, ShipmentStatus newStatus) {

        Shipment shipment = getShipment(shipmentId);

        validateTransition(shipment.getStatus(), newStatus);

        shipment.setStatus(newStatus);

        return shipmentRepository.save(shipment);
    }

    private void validateTransition(ShipmentStatus currentStatus, ShipmentStatus newStatus) {
           boolean valid = switch (currentStatus){
                case CREATED -> newStatus == ShipmentStatus.PICKED_UP || newStatus == ShipmentStatus.CANCELLED;
               case PICKED_UP -> newStatus == ShipmentStatus.IN_TRANSIT;
               case IN_TRANSIT -> newStatus == ShipmentStatus.OUT_FOR_DELIVERY || newStatus == ShipmentStatus.EXCEPTION;
               case OUT_FOR_DELIVERY -> newStatus == ShipmentStatus.DELIVERED || newStatus == ShipmentStatus.EXCEPTION;
               case EXCEPTION -> newStatus == ShipmentStatus.IN_TRANSIT;
               case DELIVERED, CANCELLED -> false;

           };

           if (valid == false){
               throw new InvalidShipmentStatusException(currentStatus,newStatus);
           }

    }

}
