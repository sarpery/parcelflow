package com.parcelflow.parcelflow.repository;

import com.parcelflow.parcelflow.domain.Shipment;
import com.parcelflow.parcelflow.domain.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    List<Shipment> findByStatus(ShipmentStatus status);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}
