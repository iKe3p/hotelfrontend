package com.gestion.hotelera.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.hotelera.config.TestSecurityConfig;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.restController.ReservaRestController;
import com.gestion.hotelera.service.ReservaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Tests para ReservaController siguiendo TDD
 * Prueba los endpoints de la API REST
 */
@WebMvcTest(controllers = ReservaRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("ReservaController - API Tests")
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservaService reservaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Reserva reserva;
    private Cliente cliente;
    private Habitacion habitacion;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setDni("12345678");
        cliente.setNombres("Juan");
        cliente.setApellidos("Pérez");

        habitacion = new Habitacion();
        habitacion.setId(1L);
        habitacion.setNumero("101");
        habitacion.setPrecioPorNoche(150.0);

        reserva = new Reserva();
        reserva.setId(1L);
        reserva.setCliente(cliente);
        reserva.setHabitacion(habitacion);
        reserva.setFechaInicio(LocalDate.now().plusDays(1));
        reserva.setFechaFin(LocalDate.now().plusDays(3));
        reserva.setEstadoReserva("PENDIENTE");
        reserva.setTotalPagar(300.0);
    }

    @Test
    @DisplayName("GET /reservas - Debería retornar todas las reservas")
    void deberiaRetornarTodasLasReservas() throws Exception {
        // Given
        List<Reserva> reservas = Arrays.asList(reserva);
        when(reservaService.obtenerTodasLasReservas()).thenReturn(reservas);

        // When & Then
        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].estadoReserva").value("PENDIENTE"))
                .andExpect(jsonPath("$[0].totalPagar").value(300.0));
    }

    @Test
    @DisplayName("GET /reservas/{id} - Debería retornar reserva por ID")
    void deberiaRetornarReservaPorId() throws Exception {
        // Given
        when(reservaService.buscarReservaPorId(1L)).thenReturn(Optional.of(reserva));

        // When & Then
        mockMvc.perform(get("/api/reservas/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estadoReserva").value("PENDIENTE"))
                .andExpect(jsonPath("$.cliente.nombres").value("Juan"));
    }

    @Test
    @DisplayName("GET /reservas/{id} - Debería retornar 404 cuando no encuentra reserva")
    void deberiaRetornar404CuandoNoEncuentraReserva() throws Exception {
        // Given
        when(reservaService.buscarReservaPorId(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/reservas/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /reservas - Debería crear nueva reserva")
    void deberiaCrearNuevaReserva() throws Exception {
        // Given
        when(reservaService.crearOActualizarReserva(any(Reserva.class))).thenReturn(reserva);

        // When & Then
        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(reserva)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estadoReserva").value("PENDIENTE"));
    }

    @Test
    @DisplayName("PUT /reservas/{id}/cancelar - Debería cancelar reserva")
    void deberiaCancelarReserva() throws Exception {
        // Given
        when(reservaService.cancelarReserva(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(put("/api/reservas/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message").value("Reserva cancelada exitosamente"));

        verify(reservaService).cancelarReserva(1L);
    }

    @Test
    @DisplayName("PUT /reservas/{id}/cancelar - Debería retornar 404 cuando no encuentra reserva")
    void deberiaRetornar404AlCancelarReservaInexistente() throws Exception {
        // Given
        when(reservaService.cancelarReserva(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(put("/api/reservas/999/cancelar"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.error").value("Reserva no encontrada"));
    }

    @Test
    @DisplayName("PUT /reservas/{id}/finalizar - Debería finalizar reserva")
    void deberiaFinalizarReserva() throws Exception {
        // Given
        doNothing().when(reservaService).finalizarReserva(1L);

        // When & Then
        mockMvc.perform(put("/api/reservas/1/finalizar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message").value("Reserva finalizada correctamente"));

        verify(reservaService).finalizarReserva(1L);
    }

    @Test
    @DisplayName("GET /reservas/estadisticas/ingresos - Debería retornar ingresos totales")
    void deberiaRetornarIngresosTotales() throws Exception {
        // Given
        when(reservaService.calcularIngresosTotales()).thenReturn(1500.0);

        // When & Then
        mockMvc.perform(get("/api/reservas/estadisticas/ingresos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.ingresosTotales").value(1500.0));
    }

    @Test
    @DisplayName("GET /reservas/estadisticas/contar - Debería retornar conteo de reservas")
    void deberiaRetornarConteoDeReservas() throws Exception {
        // Given
        when(reservaService.contarReservas()).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/reservas/estadisticas/contar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.totalReservas").value(5));
    }

    @Test
    @DisplayName("GET /reservas/estadisticas/contar/{estado} - Debería retornar conteo por estado")
    void deberiaRetornarConteoPorEstado() throws Exception {
        // Given
        String estado = "PENDIENTE";
        when(reservaService.contarReservasPorEstado(estado)).thenReturn(3L);

        // When & Then
        mockMvc.perform(get("/api/reservas/estadisticas/contar/PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.cantidad").value(3));
    }
}
