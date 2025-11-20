package com.gestion.hotelera.restController;

import com.gestion.hotelera.dto.PagoRequest;
import com.gestion.hotelera.dto.PagoResponse;
import com.gestion.hotelera.model.Pago;
import com.gestion.hotelera.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(originPatterns = "*")
public class PagoRestController {

    private final PagoService pagoService;

    public PagoRestController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    public ResponseEntity<?> procesarPago(@Valid @RequestBody PagoRequest request) {
        try {
            PagoResponse response = pagoService.procesarPago(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No fue posible procesar el pago.");
        }
    }

    @GetMapping("/reserva/{reservaId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    public ResponseEntity<?> obtenerPagoPorReserva(@PathVariable Long reservaId) {
        Optional<Pago> pago = pagoService.obtenerPagoPorReserva(reservaId);
        if (pago.isPresent()) {
            Pago encontrado = pago.get();
            PagoResponse response = new PagoResponse(
                    encontrado.getEstado(),
                    "Pago recuperado correctamente.",
                    encontrado.getReferencia(),
                    encontrado.getMontoBase(),
                    encontrado.getMontoServicios(),
                    encontrado.getMontoTotal(),
                    encontrado.getReserva().getId()
            );
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("No existe pago registrado para la reserva " + reservaId);
    }
}

