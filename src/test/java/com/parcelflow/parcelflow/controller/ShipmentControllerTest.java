
package com.parcelflow.parcelflow.controller;

import com.parcelflow.parcelflow.domain.Shipment;
import com.parcelflow.parcelflow.domain.ShipmentStatus;
import com.parcelflow.parcelflow.exception.InvalidShipmentStatusException;
import com.parcelflow.parcelflow.exception.ShipmentNotFoundException;
import com.parcelflow.parcelflow.service.ShipmentService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShipmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShipmentService shipmentService;

    @Test
    void shouldReturnShipmentWithHttp200() throws Exception {

        // Arrange
        Shipment shipment = mock(Shipment.class);

        when(shipment.getId()).thenReturn(42L);
        when(shipment.getTrackingNumber()).thenReturn("PF-0042");
        when(shipment.getStatus()).thenReturn(ShipmentStatus.CREATED);

        when(shipmentService.getShipment(42L))
                .thenReturn(shipment);

        // Act + Assert
        mockMvc.perform(get("/api/shipments/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.trackingNumber").value("PF-0042"))
                .andExpect(jsonPath("$.status").value("CREATED"));

        verify(shipmentService).getShipment(42L);
    }

    @Test
    void shouldReturn404WhenShipmentDoesNotExist() throws Exception {

        // Arrange
        when(shipmentService.getShipment(999L))
                .thenThrow(new ShipmentNotFoundException(999L));

        // Act + Assert
        mockMvc.perform(get("/api/shipments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Shipment Not Found"))
                .andExpect(jsonPath("$.path").value("/api/shipments/999"));
    }

    @Test
    void shouldReturn200ForValidStatusUpdate() throws Exception {

        // Arrange
        Shipment updatedShipment = mock(Shipment.class);

        when(updatedShipment.getId()).thenReturn(42L);
        when(updatedShipment.getStatus())
                .thenReturn(ShipmentStatus.PICKED_UP);

        when(shipmentService.updateStatus(
                42L, ShipmentStatus.PICKED_UP
        )).thenReturn(updatedShipment);

        // Act + Assert
        mockMvc.perform(
                        patch("/api/shipments/42/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"PICKED_UP\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.status").value("PICKED_UP"));

        verify(shipmentService).updateStatus(
                42L, ShipmentStatus.PICKED_UP
        );
    }

    @Test
    void shouldReturn409ForInvalidStatusTransition() throws Exception {

        // Arrange
        when(shipmentService.updateStatus(
                42L, ShipmentStatus.DELIVERED
        )).thenThrow(
                new InvalidShipmentStatusException(
                        ShipmentStatus.CREATED,
                        ShipmentStatus.DELIVERED
                )
        );

        // Act + Assert
        mockMvc.perform(
                        patch("/api/shipments/42/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"DELIVERED\"}")
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Invalid Operation"));
    }

    @Test
    void shouldReturn400WhenStatusIsMissing() throws Exception {

        // Act + Assert
        mockMvc.perform(
                        patch("/api/shipments/42/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }
}
