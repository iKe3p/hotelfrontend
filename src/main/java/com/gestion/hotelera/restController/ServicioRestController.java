package com.gestion.hotelera.restController;

import com.gestion.hotelera.model.Servicio;
import com.gestion.hotelera.service.ServicioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@CrossOrigin(originPatterns = "*")
public class ServicioRestController {

    private final ServicioService servicioService;

    public ServicioRestController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    public ResponseEntity<List<Servicio>> listarServicios() {
        return ResponseEntity.ok(servicioService.listarServiciosActivos());
    }

    @GetMapping("/todos")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    public ResponseEntity<List<Servicio>> listarTodos() {
        return ResponseEntity.ok(servicioService.listarTodos());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    public ResponseEntity<Servicio> crearServicio(@Valid @RequestBody Servicio servicio) {
        Servicio guardado = servicioService.guardar(servicio);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    public ResponseEntity<?> actualizarServicio(@PathVariable Long id, @Valid @RequestBody Servicio servicio) {
        var existenteOpt = servicioService.buscarPorId(id);
        if (existenteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Servicio con ID " + id + " no encontrado.");
        }

        Servicio existente = existenteOpt.get();
        servicio.setId(id);
        servicio.setReservas(existente.getReservas());
        Servicio actualizado = servicioService.guardar(servicio);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    public ResponseEntity<?> eliminarServicio(@PathVariable Long id) {
        return servicioService.buscarPorId(id)
                .map(servicio -> {
                    servicioService.eliminar(id);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Servicio con ID " + id + " no encontrado."));
    }
}

