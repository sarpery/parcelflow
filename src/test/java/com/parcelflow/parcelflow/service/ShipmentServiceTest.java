package com.parcelflow.parcelflow.service;
import com.parcelflow.parcelflow.domain.ShipmentStatus;
import com.parcelflow.parcelflow.exception.InvalidShipmentStatusException;
import org.junit.jupiter.api.Test;

import com.parcelflow.parcelflow.domain.Shipment;
import com.parcelflow.parcelflow.exception.ShipmentNotFoundException;
import com.parcelflow.parcelflow.repository.ShipmentRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShipmentServiceTest {

    private final ShipmentRepository shipmentRepository =
            mock(ShipmentRepository.class);

    private final ShipmentService shipmentService =
            new ShipmentService(shipmentRepository);

    @Test
    void shouldReturnShipmentWhenItExists() {

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber("PF-100001");

        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));

        Shipment result = shipmentService.getShipment(1L);

        assertNotNull(result);
        assertEquals("PF-100001", result.getTrackingNumber());
    }

    @Test
    void shouldThrowExceptionWhenShipmentDoesNotExist() {

        when(shipmentRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ShipmentNotFoundException.class,
                () -> shipmentService.getShipment(999L)
        );
    }

    @Test
    void shouldAllowCreatedToPickedUp() {

        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.CREATED);

        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        Shipment result = shipmentService.updateStatus(
                1L,
                ShipmentStatus.PICKED_UP
        );

        assertEquals(
                ShipmentStatus.PICKED_UP,
                result.getStatus()
        );
    }

    @Test
    void shouldRejectDeliveredToCreated(){
        Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.DELIVERED);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        assertThrows(
                InvalidShipmentStatusException.class,
                () -> shipmentService.updateStatus(
                        1L,
                        ShipmentStatus.CREATED
                )
        );

    }


}