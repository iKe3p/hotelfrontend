package com.gestion.hotelera.restController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.service.HabitacionService;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/habitaciones")
@CrossOrigin(originPatterns = "*")
public class HabitacionRestController {

    private final HabitacionService habitacionService;

    public HabitacionRestController(HabitacionService habitacionService) {
        this.habitacionService = habitacionService;
    }

    @PostMapping
    public ResponseEntity<Habitacion> registrarHabitacion(@Valid @RequestBody Habitacion habitacion) {
        Habitacion nueva = habitacionService.crearHabitacion(habitacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @GetMapping
    public ResponseEntity<List<Habitacion>> listarHabitaciones() {
        return ResponseEntity.ok(habitacionService.obtenerTodasLasHabitaciones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habitacion> buscarPorId(@PathVariable Long id) {
        return habitacionService.buscarHabitacionPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Habitacion>> listarPorEstado(@PathVariable String estado) {
        switch (estado.toUpperCase()) {
            case "DISPONIBLE":
                return ResponseEntity.ok(habitacionService.obtenerHabitacionesDisponibles());
            case "OCUPADA":
                return ResponseEntity.ok(habitacionService.obtenerHabitacionesOcupadas());
            case "MANTENIMIENTO":
                return ResponseEntity.ok(habitacionService.obtenerHabitacionesEnMantenimiento());
            default:
                return ResponseEntity.ok(List.of());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habitacion> actualizarHabitacion(@PathVariable Long id, @Valid @RequestBody Habitacion habitacion) {
        habitacion.setId(id);
        Habitacion actualizada = habitacionService.actualizarHabitacion(habitacion);
        return ResponseEntity.ok(actualizada);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        habitacionService.actualizarEstadoHabitacion(id, estado);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarHabitacion(@PathVariable Long id) {
        try {
            habitacionService.eliminarHabitacion(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }
}