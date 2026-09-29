package com.parcelflow.parcelflow.controller;

import com.parcelflow.parcelflow.domain.Shipment;
import com.parcelflow.parcelflow.domain.ShipmentStatus;
import com.parcelflow.parcelflow.dto.CreateShipmentRequest;
import com.parcelflow.parcelflow.dto.ShipmentResponse;
import com.parcelflow.parcelflow.dto.StatusUpdateRequest;
import com.parcelflow.parcelflow.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ShipmentResponse createShipment(@Valid @RequestBody CreateShipmentRequest request) {
        Shipment shipment = shipmentService.createShipment(request);
        return ShipmentResponse.from(shipment);
    }

    @GetMapping
    public List<ShipmentResponse> getAllShipments() {
        return shipmentService.getAllShipments()
                .stream()
                .map(shipment -> ShipmentResponse.from(shipment))
                .toList();

    }

    @GetMapping("/{id}")
    public ShipmentResponse getShipment(@PathVariable Long id) {
       Shipment shipment= shipmentService.getShipment(id);
       return ShipmentResponse.from(shipment);
    }


    @PatchMapping("/{id}/status")
    public ShipmentResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request){
        Shipment shipment = shipmentService.updateStatus(id, request.status());
        return ShipmentResponse.from(shipment);
    }
}

