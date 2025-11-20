package com.gestion.hotelera.restController;

import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.service.ReservaService;
import com.gestion.hotelera.service.HabitacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowCredentials = "true")
public class ReservaRestController {

    private final ReservaService reservaService;
    private final HabitacionService habitacionService;

    public ReservaRestController(ReservaService reservaService, HabitacionService habitacionService) {
        this.reservaService = reservaService;
        this.habitacionService = habitacionService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    @PostMapping
    public ResponseEntity<?> crearReserva(@Valid @RequestBody Reserva reserva) {
        try {
            if (reserva == null) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Datos de reserva requeridos"));
            }
            if (reserva.getCliente() == null) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Debe asociar un cliente a la reserva"));
            }
            Reserva nuevaReserva = reservaService.crearOActualizarReserva(reserva);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaReserva);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error al crear reserva"));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @GetMapping
    public ResponseEntity<List<Reserva>> listarReservas() {
        return ResponseEntity.ok(reservaService.obtenerTodasLasReservas());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "ID inválido"));
        }
        try {
            return reservaService.buscarReservaPorId(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error al buscar reserva"));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarReserva(@PathVariable Long id, @Valid @RequestBody Reserva reserva) {
        reserva.setId(id);
        Reserva reservaActualizada = reservaService.crearOActualizarReserva(reserva);
        return ResponseEntity.ok(reservaActualizada);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizarReserva(@PathVariable Long id) {
        try {
            reservaService.finalizarReserva(id);
            return ResponseEntity.ok(Collections.singletonMap("message", "Reserva finalizada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "No se pudo finalizar la reserva"));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id) {
        boolean ok = reservaService.cancelarReserva(id);
        if (ok) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Reserva cancelada exitosamente"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("error", "Reserva no encontrada"));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarReserva(@PathVariable Long id) {
        boolean ok = reservaService.eliminarReservaFisica(id);
        if (ok) return ResponseEntity.noContent().build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Reserva no encontrada"));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @GetMapping("/estadisticas/ingresos")
    public ResponseEntity<?> obtenerIngresosTotales() {
        Double ingresos = reservaService.calcularIngresosTotales();
        return ResponseEntity.ok(Collections.singletonMap("ingresosTotales", ingresos));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @GetMapping("/estadisticas/contar")
    public ResponseEntity<?> contarReservas() {
        long total = reservaService.contarReservas();
        return ResponseEntity.ok(Collections.singletonMap("totalReservas", total));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @GetMapping("/estadisticas/contar/{estado}")
    public ResponseEntity<?> contarReservasPorEstado(@PathVariable String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Estado requerido"));
        }
        try {
            String estadoSanitizado = estado.trim().toUpperCase();
            long cantidad = reservaService.contarReservasPorEstado(estadoSanitizado);
            return ResponseEntity.ok(Map.of("estado", estadoSanitizado, "cantidad", cantidad));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error al contar reservas"));
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    @GetMapping("/calcular-costo")
    public ResponseEntity<?> calcularCosto(
            @RequestParam("habitacionId") Long habitacionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        if (fechaInicio == null || fechaFin == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Las fechas no pueden ser nulas", "dias", 0, "total", 0));
        }

        if (fechaFin.isBefore(fechaInicio)) {
            return ResponseEntity.badRequest().body(Map.of("error", "La fecha de fin no puede ser anterior a la fecha de inicio", "dias", 0, "total", 0));
        }

        Optional<Habitacion> habitacionOpt = habitacionService.buscarHabitacionPorId(habitacionId);
        if (habitacionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Habitación no encontrada", "dias", 0, "total", 0));
        }

        Habitacion habitacion = habitacionOpt.get();
        if (habitacion.getPrecioPorNoche() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Precio de la habitación no definido", "dias", 0, "total", 0));
        }

        int dias = reservaService.calcularDiasEstadia(fechaInicio, fechaFin);
        if (dias <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Fechas inválidas", "dias", 0, "total", 0));
        }

        double total = reservaService.calcularTotalPagar(habitacion.getPrecioPorNoche(), dias);
        return ResponseEntity.ok(Map.of("dias", dias, "total", total));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    @PutMapping("/{id}/servicios")
    public ResponseEntity<?> asignarServicios(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "ID de reserva inválido"));
            }
            
            @SuppressWarnings("unchecked")
            List<Long> servicioIds = (List<Long>) request.get("servicioIds");
            @SuppressWarnings("unchecked")
            List<String> opciones = (List<String>) request.get("opciones");
            
            Reserva reservaActualizada = reservaService.asignarServicios(id, servicioIds, opciones);
            return ResponseEntity.ok(reservaActualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error al asignar servicios"));
        }
    }
}