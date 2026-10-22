
package com.parcelflow.parcelflow.event;

import com.parcelflow.parcelflow.domain.ShipmentStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ShipmentStatusChangedEventTest {

    @Test
    void shouldContainCorrectShipmentEventDetails() {

        // Arrange
        Instant occurredAt =
                Instant.parse("2026-10-21T10:00:00Z");

        // Act
        ShipmentStatusChangedEvent event =
                new ShipmentStatusChangedEvent(
                        42L,
                        "PF-0042",
                        ShipmentStatus.CREATED,
                        ShipmentStatus.PICKED_UP,
                        occurredAt
                );

        // Assert
        assertAll(
                () -> assertEquals(42L, event.shipmentId()),
                () -> assertEquals("PF-0042", event.trackingNumber()),
                () -> assertEquals(
                        ShipmentStatus.CREATED,
                        event.previousStatus()
                ),
                () -> assertEquals(
                        ShipmentStatus.PICKED_UP,
                        event.newStatus()
                ),
                () -> assertEquals(occurredAt, event.occurredAt())
        );
    }
}
