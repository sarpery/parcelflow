
package com.parcelflow.parcelflow.service;

import com.parcelflow.parcelflow.domain.Shipment;
import com.parcelflow.parcelflow.domain.ShipmentStatus;
import com.parcelflow.parcelflow.event.ShipmentStatusChangedEvent;
import com.parcelflow.parcelflow.exception.InvalidShipmentStatusException;
import com.parcelflow.parcelflow.repository.ShipmentRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ShipmentService shipmentService;

    @Test
    void shouldReturnShipmentWhenItExists() {

        // Arrange
        Long shipmentId = 42L;

        Shipment expectedShipment = mock(Shipment.class);

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(expectedShipment));

        // Act
        Shipment actualShipment =
                shipmentService.getShipment(shipmentId);

        // Assert
        assertSame(expectedShipment, actualShipment);

        verify(shipmentRepository).findById(shipmentId);
    }

    @ParameterizedTest
    @MethodSource("validTransitions")
    void shouldUpdateShipmentForValidTransition(
            ShipmentStatus currentStatus,
            ShipmentStatus newStatus
    ) {
        // Arrange
        Long shipmentId = 42L;

        Shipment shipment = mock(Shipment.class);

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        when(shipment.getStatus())
                .thenReturn(currentStatus);

        when(shipment.getId())
                .thenReturn(shipmentId);

        when(shipment.getTrackingNumber())
                .thenReturn("PF-0042");

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        // Act
        Shipment result =
                shipmentService.updateStatus(shipmentId, newStatus);

        // Assert: shipment updated and saved
        assertSame(shipment, result);

        verify(shipment).setStatus(newStatus);
        verify(shipmentRepository).save(shipment);

        // Assert: an application event was published
        ArgumentCaptor<ShipmentStatusChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        ShipmentStatusChangedEvent.class
                );

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ShipmentStatusChangedEvent event = eventCaptor.getValue();

        assertEquals(shipmentId, event.shipmentId());
        assertEquals("PF-0042", event.trackingNumber());
        assertEquals(currentStatus, event.previousStatus());
        assertEquals(newStatus, event.newStatus());
        assertNotNull(event.occurredAt());
    }

    static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(
                        ShipmentStatus.CREATED,
                        ShipmentStatus.PICKED_UP
                ),
                Arguments.of(
                        ShipmentStatus.CREATED,
                        ShipmentStatus.CANCELLED
                ),
                Arguments.of(
                        ShipmentStatus.PICKED_UP,
                        ShipmentStatus.IN_TRANSIT
                ),
                Arguments.of(
                        ShipmentStatus.IN_TRANSIT,
                        ShipmentStatus.OUT_FOR_DELIVERY
                ),
                Arguments.of(
                        ShipmentStatus.IN_TRANSIT,
                        ShipmentStatus.EXCEPTION
                ),
                Arguments.of(
                        ShipmentStatus.OUT_FOR_DELIVERY,
                        ShipmentStatus.DELIVERED
                ),
                Arguments.of(
                        ShipmentStatus.OUT_FOR_DELIVERY,
                        ShipmentStatus.EXCEPTION
                ),
                Arguments.of(
                        ShipmentStatus.EXCEPTION,
                        ShipmentStatus.IN_TRANSIT
                )
        );
    }


    @ParameterizedTest
    @MethodSource("invalidTransitions")
    void shouldRejectInvalidStatusTransition(
            ShipmentStatus currentStatus,
            ShipmentStatus newStatus
    ) {
        // Arrange
        Long shipmentId = 42L;

        Shipment shipment = mock(Shipment.class);

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        when(shipment.getStatus())
                .thenReturn(currentStatus);

        // Act + Assert
        InvalidShipmentStatusException exception =
                assertThrows(
                        InvalidShipmentStatusException.class,
                        () -> shipmentService.updateStatus(
                                shipmentId,
                                newStatus
                        )
                );

        assertEquals(
                "The attempted shipment status update is invalid. "+ currentStatus +" -> " + newStatus + " is not a valid status update.",
                exception.getMessage()
        );

        // The shipment must not be changed
        verify(shipment, never())
                .setStatus(any(ShipmentStatus.class));

        // No database save should occur
        verify(shipmentRepository, never())
                .save(any(Shipment.class));

        // No event should be published
        verifyNoInteractions(eventPublisher);
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(
                        ShipmentStatus.CREATED,
                        ShipmentStatus.DELIVERED
                ),
                Arguments.of(
                        ShipmentStatus.PICKED_UP,
                        ShipmentStatus.CANCELLED
                ),
                Arguments.of(
                        ShipmentStatus.IN_TRANSIT,
                        ShipmentStatus.DELIVERED
                ),
                Arguments.of(
                        ShipmentStatus.OUT_FOR_DELIVERY,
                        ShipmentStatus.IN_TRANSIT
                ),
                Arguments.of(
                        ShipmentStatus.EXCEPTION,
                        ShipmentStatus.DELIVERED
                ),
                Arguments.of(
                        ShipmentStatus.DELIVERED,
                        ShipmentStatus.IN_TRANSIT
                ),
                Arguments.of(
                        ShipmentStatus.CANCELLED,
                        ShipmentStatus.PICKED_UP
                )
        );
    }




}


