package com.gestion.hotelera.restController;

import com.gestion.hotelera.exception.ClienteConReservasActivasException;
import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(originPatterns = "*")
public class ClienteRestController {

    private final ClienteService clienteService;

    public ClienteRestController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @PostMapping
    public ResponseEntity<?> registrarCliente(@Valid @RequestBody Cliente cliente) {
        try {
            Cliente nuevoCliente = clienteService.crearCliente(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCliente);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar cliente.");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteService.obtenerTodosLosClientes());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    @GetMapping("/id/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID inválido");
        }
        try {
            Optional<Cliente> cliente = clienteService.buscarClientePorId(id);
            if (cliente.isPresent()) {
                return ResponseEntity.ok(cliente.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Cliente no encontrado");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al buscar cliente");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    @GetMapping("/{dni}")
    public ResponseEntity<?> buscarPorDni(@PathVariable String dni) {
        if (dni == null || !dni.matches("\\d{8}")) {
            return ResponseEntity.badRequest().body("DNI inválido");
        }
        try {
            Optional<Cliente> cliente = clienteService.buscarClientePorDni(dni);
            if (cliente.isPresent()) {
                return ResponseEntity.ok(cliente.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Cliente no encontrado");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al buscar cliente");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Cliente cliente) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID inválido");
        }
        if (cliente == null) {
            return ResponseEntity.badRequest().body("Datos de cliente requeridos");
        }
        try {
            cliente.setId(id);
            Cliente clienteActualizado = clienteService.actualizarCliente(cliente);
            return ResponseEntity.ok(clienteActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar cliente");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            boolean eliminado = clienteService.eliminarClientePorId(id);
            if (eliminado) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Cliente con ID " + id + " no encontrado para eliminar.");
            }
        } catch (ClienteConReservasActivasException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "mensaje", ex.getMessage(),
                            "clienteId", ex.getClienteId(),
                            "reservasActivas", ex.getReservasActivas()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el cliente.");
        }
    }
}