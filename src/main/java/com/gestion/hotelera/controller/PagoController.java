package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.service.ReservaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.gestion.hotelera.dto.PagoRequest;
import com.gestion.hotelera.dto.PagoResponse;
import com.gestion.hotelera.service.PagoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/pago")
public class PagoController {

    private final ReservaService reservaService;
    private final PagoService pagoService;

    public PagoController(ReservaService reservaService, PagoService pagoService) {
        this.reservaService = reservaService;
        this.pagoService = pagoService;
    }

    @GetMapping("/{reservaId}")
    public String mostrarFormularioPago(@PathVariable Long reservaId, Model model) {
        if (reservaId == null || reservaId <= 0) {
            model.addAttribute("errorMessage", "ID de reserva inválido");
            return "redirect:/";
        }
        try {
            Optional<Reserva> reservaOptional = reservaService.obtenerReservaPorId(reservaId);
            if (reservaOptional.isEmpty()) {
                model.addAttribute("errorMessage", "Reserva no encontrada");
                return "redirect:/";
            }
            model.addAttribute("reserva", reservaOptional.get());
            return "pago";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar formulario de pago");
            return "redirect:/";
        }
    }

    @PostMapping("/{reservaId}")
    public String procesarPago(@PathVariable Long reservaId, 
                              @RequestParam("metodoPago") String metodoPago,
                              RedirectAttributes redirectAttributes) {
        if (reservaId == null || reservaId <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID de reserva inválido");
            return "redirect:/";
        }
        
        try {
            PagoRequest pagoRequest = new PagoRequest();
            pagoRequest.setReservaId(reservaId);
            pagoRequest.setMetodoPago(metodoPago != null ? metodoPago : "TARJETA");
            
            PagoResponse pagoResponse = pagoService.procesarPago(pagoRequest);
            if (pagoResponse != null && pagoResponse.isExito()) {
                redirectAttributes.addFlashAttribute("successMessage", "¡Pago realizado correctamente!");
                return "redirect:/pago/" + reservaId + "/resumen";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Error en el procesamiento del pago");
                return "redirect:/pago/" + reservaId;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar el pago");
            return "redirect:/pago/" + reservaId;
        }
    }

    @GetMapping("/{reservaId}/resumen")
    public String mostrarResumenPago(@PathVariable Long reservaId, Model model) {
        Optional<Reserva> reservaOptional = reservaService.obtenerReservaPorId(reservaId);
        if (reservaOptional.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("reserva", reservaOptional.get());
        return "resumen-pago";
    }
}
