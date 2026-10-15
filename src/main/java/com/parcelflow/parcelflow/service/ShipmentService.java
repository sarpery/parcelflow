package com.parcelflow.parcelflow.service;
import org.springframework.context.ApplicationEventPublisher;
import com.parcelflow.parcelflow.domain.Shipment;
import com.parcelflow.parcelflow.dto.CreateShipmentRequest;
import com.parcelflow.parcelflow.event.ShipmentStatusChangedEvent;
import com.parcelflow.parcelflow.exception.InvalidShipmentStatusException;
import com.parcelflow.parcelflow.exception.ShipmentNotFoundException;
import com.parcelflow.parcelflow.kafka.ShipmentEventProducer;
import com.parcelflow.parcelflow.repository.ShipmentRepository;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.parcelflow.parcelflow.domain.ShipmentStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ShipmentService(ShipmentRepository shipmentRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.shipmentRepository = shipmentRepository;
        this.applicationEventPublisher = applicationEventPublisher;
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

    public Page<Shipment> getAllShipments(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        return shipmentRepository.findAll(pageable);
    }

    public Shipment getShipment(Long id){
        return shipmentRepository.findById(id)
                .orElseThrow(() ->
                        new ShipmentNotFoundException(id));
    }

    @Transactional
    public Shipment updateStatus(Long shipmentId, ShipmentStatus newStatus) {

        Shipment shipment = getShipment(shipmentId);

        ShipmentStatus previousStatus = shipment.getStatus();

        validateTransition(previousStatus, newStatus);

        shipment.setStatus(newStatus);

        Shipment savedShipment = shipmentRepository.save(shipment);

        ShipmentStatusChangedEvent event =
                new ShipmentStatusChangedEvent(
                        savedShipment.getId(),
                        savedShipment.getTrackingNumber(),
                        previousStatus,
                        newStatus,
                        Instant.now()
                );

        applicationEventPublisher.publishEvent(event);

        return savedShipment;
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

    public List<Shipment> getShipmentsByStatus(ShipmentStatus status){
        return shipmentRepository.findByStatus(status);
    }

    public Optional<Shipment> getShipmentByTrackingNumber(String trackingNumber){
        return shipmentRepository.findByTrackingNumber(trackingNumber);
    }

}
