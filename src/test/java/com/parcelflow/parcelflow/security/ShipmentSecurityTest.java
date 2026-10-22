
package com.parcelflow.parcelflow.security;

import com.parcelflow.parcelflow.controller.ShipmentController;
import com.parcelflow.parcelflow.domain.Shipment;
import com.parcelflow.parcelflow.domain.ShipmentStatus;
import com.parcelflow.parcelflow.service.ShipmentService;

import io.jsonwebtoken.JwtException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityConfig.class)
class ShipmentSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShipmentService shipmentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {

        mockMvc.perform(get("/api/shipments/42"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(shipmentService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldAllowUserToViewShipment() throws Exception {

        Shipment shipment = mock(Shipment.class);

        when(shipment.getId()).thenReturn(42L);
        when(shipment.getStatus())
                .thenReturn(ShipmentStatus.CREATED);

        when(shipmentService.getShipment(42L))
                .thenReturn(shipment);

        mockMvc.perform(get("/api/shipments/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.status").value("CREATED"));

        verify(shipmentService).getShipment(42L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldForbidUserFromUpdatingStatus() throws Exception {

        mockMvc.perform(
                        patch("/api/shipments/42/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"PICKED_UP\"}")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(shipmentService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToUpdateStatus() throws Exception {

        Shipment shipment = mock(Shipment.class);

        when(shipment.getId()).thenReturn(42L);
        when(shipment.getStatus())
                .thenReturn(ShipmentStatus.PICKED_UP);

        when(shipmentService.updateStatus(
                42L, ShipmentStatus.PICKED_UP
        )).thenReturn(shipment);

        mockMvc.perform(
                        patch("/api/shipments/42/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"PICKED_UP\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PICKED_UP"));

        verify(shipmentService).updateStatus(
                42L, ShipmentStatus.PICKED_UP
        );
    }

    @Test
    void shouldRejectInvalidJwt() throws Exception {

        when(jwtService.extractUsername("bad-token"))
                .thenThrow(new JwtException("Invalid test token"));

        mockMvc.perform(
                        get("/api/shipments/42")
                                .header("Authorization", "Bearer bad-token")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(shipmentService);
    }

    @Test
    void shouldAllowAdminWithBearerToken() throws Exception {

        // Arrange: simulate successful JWT verification
        when(jwtService.extractUsername("test-token"))
                .thenReturn("admin");

        when(userDetailsService.loadUserByUsername("admin"))
                .thenReturn(
                        User.withUsername("admin")
                                .password("unused")
                                .roles("ADMIN")
                                .build()
                );

        Shipment shipment = mock(Shipment.class);

        when(shipment.getId()).thenReturn(42L);
        when(shipment.getStatus())
                .thenReturn(ShipmentStatus.PICKED_UP);

        when(shipmentService.updateStatus(
                42L, ShipmentStatus.PICKED_UP
        )).thenReturn(shipment);

        // Act + Assert
        mockMvc.perform(
                        patch("/api/shipments/42/status")
                                .header(
                                        "Authorization",
                                        "Bearer test-token"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"PICKED_UP\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PICKED_UP"));

        verify(jwtService).extractUsername("test-token");

        verify(userDetailsService)
                .loadUserByUsername("admin");
    }
}
