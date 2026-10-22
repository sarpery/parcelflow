
package com.parcelflow.parcelflow.repository;

import com.parcelflow.parcelflow.domain.Shipment;
import com.parcelflow.parcelflow.domain.ShipmentStatus;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(
        properties = "spring.jpa.hibernate.ddl-auto=create-drop",
        showSql = false
)
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@ActiveProfiles("test")
@Testcontainers
class ShipmentRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18")
                    .withDatabaseName("parcelflow_test")
                    .withUsername("test_user")
                    .withPassword("test_password");

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndFindShipmentByTrackingNumber() {

        // Arrange
        Shipment shipment = createShipment(
                "PF-TEST-001",
                ShipmentStatus.CREATED
        );

        shipmentRepository.saveAndFlush(shipment);

        // Clear Hibernate's first-level cache
        entityManager.clear();

        // Act
        Optional<Shipment> result =
                shipmentRepository.findByTrackingNumber(
                        "PF-TEST-001"
                );

        // Assert
        assertTrue(result.isPresent());

        assertEquals(
                "PF-TEST-001",
                result.get().getTrackingNumber()
        );

        assertEquals(
                ShipmentStatus.CREATED,
                result.get().getStatus()
        );

        assertEquals(
                "Alice",
                result.get().getSenderName()
        );
    }

    @Test
    void shouldFindShipmentsByStatus() {

        // Arrange
        shipmentRepository.saveAllAndFlush(List.of(
                createShipment(
                        "PF-TEST-002",
                        ShipmentStatus.IN_TRANSIT
                ),
                createShipment(
                        "PF-TEST-003",
                        ShipmentStatus.CREATED
                ),
                createShipment(
                        "PF-TEST-004",
                        ShipmentStatus.IN_TRANSIT
                )
        ));

        entityManager.clear();

        // Act
        List<Shipment> inTransitShipments =
                shipmentRepository.findByStatus(
                        ShipmentStatus.IN_TRANSIT
                );

        // Assert
        assertEquals(2, inTransitShipments.size());

        assertTrue(
                inTransitShipments.stream()
                        .allMatch(shipment ->
                                shipment.getStatus()
                                        == ShipmentStatus.IN_TRANSIT
                        )
        );
    }

    @Test
    void shouldPersistShipmentStatusChange() {

        // Arrange
        Shipment shipment = createShipment(
                "PF-TEST-005",
                ShipmentStatus.CREATED
        );

        Shipment savedShipment =
                shipmentRepository.saveAndFlush(shipment);

        Long shipmentId = savedShipment.getId();

        entityManager.clear();

        // Act
        Shipment existingShipment =
                shipmentRepository.findById(shipmentId)
                        .orElseThrow();

        existingShipment.setStatus(
                ShipmentStatus.PICKED_UP
        );

        shipmentRepository.saveAndFlush(existingShipment);

        entityManager.clear();

        // Assert
        Shipment updatedShipment =
                shipmentRepository.findById(shipmentId)
                        .orElseThrow();

        assertEquals(
                ShipmentStatus.PICKED_UP,
                updatedShipment.getStatus()
        );
    }

    private Shipment createShipment(
            String trackingNumber,
            ShipmentStatus status
    ) {
        Shipment shipment = new Shipment();

        shipment.setTrackingNumber(trackingNumber);
        shipment.setSenderName("Alice");
        shipment.setRecipientName("Bob");
        shipment.setOrigin("Maastricht");
        shipment.setDestination("Amsterdam");
        shipment.setStatus(status);

        shipment.setCreatedAt(
                LocalDateTime.of(2026, 10, 21, 10, 0)
        );

        return shipment;
    }
}
