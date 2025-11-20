package com.gestion.hotelera.restController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gestion.hotelera.model.Empleado;
import com.gestion.hotelera.service.EmpleadoService;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/empleados")
@CrossOrigin(originPatterns = "*")
public class EmpleadoRestController {

    private final EmpleadoService empleadoService;

    public EmpleadoRestController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @PostMapping
    public ResponseEntity<Empleado> registrar(@Valid @RequestBody Empleado empleado) {
        Empleado nuevoEmpleado = empleadoService.registrarRecepcionista(empleado);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEmpleado);
    }

    @GetMapping
    public ResponseEntity<List<Empleado>> listar() {
        return ResponseEntity.ok(empleadoService.obtenerTodosLosEmpleados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empleado> buscarPorId(@PathVariable Long id) {
        return empleadoService.buscarEmpleadoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empleado> actualizar(@PathVariable Long id, @Valid @RequestBody Empleado empleado) {
        empleado.setId(id);
        Empleado empleadoActualizado = empleadoService.actualizarEmpleado(empleado);
        return ResponseEntity.ok(empleadoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        boolean ok = empleadoService.eliminarEmpleado(id);
        if (ok) return ResponseEntity.noContent().build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}