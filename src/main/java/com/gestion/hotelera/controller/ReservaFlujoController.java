package com.gestion.hotelera.controller;

import com.gestion.hotelera.dto.PagoRequest;
import com.gestion.hotelera.dto.PagoResponse;
import com.gestion.hotelera.model.Servicio;
import com.gestion.hotelera.service.PagoService;
import com.gestion.hotelera.service.ReservaService;
import com.gestion.hotelera.service.ServicioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reservas")
public class ReservaFlujoController {

    private final ReservaService reservaService;
    private final ServicioService servicioService;
    private final PagoService pagoService;

    public ReservaFlujoController(ReservaService reservaService,
                                  ServicioService servicioService,
                                  PagoService pagoService) {
        this.reservaService = reservaService;
        this.servicioService = servicioService;
        this.pagoService = pagoService;
    }

    @GetMapping("/{id}/servicios")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    public String mostrarSelectorServicios(@PathVariable Long id,
                                           @RequestParam(value = "returnTo", required = false) String returnTo,
                                           Model model,
                                           RedirectAttributes redirectAttributes) {
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID de reserva inválido");
            return "redirect:/dashboard";
        }
        try {
            return reservaService.obtenerReservaPorId(id)
                    .map(reserva -> {
                        List<Servicio> serviciosActivos = servicioService.listarServiciosActivos();
                        Set<Long> serviciosSeleccionados = reserva.getServicios()
                                .stream()
                                .map(Servicio::getId)
                                .collect(Collectors.toSet());

                        model.addAttribute("reserva", reserva);
                        model.addAttribute("serviciosDisponibles", serviciosActivos);
                        model.addAttribute("serviciosSeleccionados", serviciosSeleccionados);
                        model.addAttribute("returnTo", returnTo);
                        return "seleccionarServicios";
                    })
                    .orElseGet(() -> {
                        redirectAttributes.addFlashAttribute("errorMessage", "Reserva no encontrada");
                        return "redirect:/dashboard";
                    });
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cargar servicios");
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/{id}/servicios")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    public String guardarServicios(@PathVariable Long id,
                                   @RequestParam(value = "servicioIds", required = false) List<Long> servicioIds,
                                   @RequestParam(value = "returnTo", required = false) String returnTo,
                                   RedirectAttributes redirectAttributes) {
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID de reserva inválido");
            return "redirect:/dashboard";
        }
        try {
            reservaService.asignarServicios(id, servicioIds, null);
            redirectAttributes.addFlashAttribute("successMessage", "Servicios actualizados correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar servicios");
            return "redirect:/reservas/" + id + "/servicios";
        }
        String destino = returnTo != null ? returnTo : "pago";
        if ("historial".equalsIgnoreCase(destino)) {
            return "redirect:/reservas/" + id + "/pago?returnTo=historial";
        }
        if ("dashboard".equalsIgnoreCase(destino)) {
            return "redirect:/reservas/" + id + "/pago?returnTo=dashboard";
        }
        return "redirect:/reservas/" + id + "/pago";
    }

    @GetMapping("/{id}/pago")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    public String mostrarPago(@PathVariable Long id,
                              @RequestParam(value = "returnTo", required = false) String returnTo,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        return reservaService.obtenerReservaPorId(id)
                .map(reserva -> {
                    double montoServicios = reserva.calcularTotalServicios();
                    double montoBase = reserva.getTotalPagar() != null ? reserva.getTotalPagar() : 0.0;
                    double montoTotal = montoBase + montoServicios;

                    model.addAttribute("reserva", reserva);
                    model.addAttribute("montoBase", montoBase);
                    model.addAttribute("montoServicios", montoServicios);
                    model.addAttribute("montoTotal", montoTotal);
                    model.addAttribute("returnTo", returnTo);
                    model.addAttribute("pagoProcesado", reserva.getPago());
                    return "pagoReserva";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "La reserva solicitada no existe.");
                    return "redirect:/dashboard";
                });
    }

    @PostMapping("/{id}/pago")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    public String procesarPago(@PathVariable Long id,
                               @RequestParam("metodo") String metodo,
                               @RequestParam(value = "numeroTarjeta", required = false) String numeroTarjeta,
                               @RequestParam(value = "cvv", required = false) String cvv,
                               @RequestParam(value = "fechaExp", required = false) String fechaExp,
                               @RequestParam(value = "titularTarjeta", required = false) String titularTarjeta,
                               @RequestParam(value = "telefonoWallet", required = false) String telefonoWallet,
                               @RequestParam(value = "titularWallet", required = false) String titularWallet,
                               @RequestParam(value = "returnTo", required = false) String returnTo,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            PagoRequest request = new PagoRequest();
            request.setReservaId(id);
            String metodoNormalizado = metodo != null ? metodo.toUpperCase() : "TARJETA";
            request.setMetodo(metodoNormalizado);
            request.setMetodoPago(metodoNormalizado); // Asegurar que ambos campos estén establecidos
            request.setCanal("WEB");

            if ("TARJETA".equalsIgnoreCase(metodo)) {
                PagoRequest.DatosTarjeta datosTarjeta = new PagoRequest.DatosTarjeta();
                datosTarjeta.setNumero(numeroTarjeta);
                datosTarjeta.setCvv(cvv);
                datosTarjeta.setFechaExp(fechaExp);
                datosTarjeta.setTitular(titularTarjeta);
                request.setTarjeta(datosTarjeta);
                request.setWallet(null);
            } else {
                PagoRequest.DatosWallet datosWallet = new PagoRequest.DatosWallet();
                datosWallet.setTelefono(telefonoWallet);
                datosWallet.setTitular(titularWallet);
                request.setWallet(datosWallet);
                request.setTarjeta(null);
            }

            // Validar que el método no sea null o vacío antes de crear el request
            if (metodo == null || metodo.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Debe seleccionar un método de pago");
                return "redirect:/reservas/" + id + "/pago" + (returnTo != null ? "?returnTo=" + returnTo : "");
            }
            
            PagoResponse response = pagoService.procesarPago(request);
            
            // Verificar si el pago fue exitoso
            if (response == null || !response.isExito()) {
                String mensajeError = response != null && response.getMensaje() != null ? 
                                    response.getMensaje() : "No fue posible procesar el pago. Inténtalo nuevamente.";
                redirectAttributes.addFlashAttribute("errorMessage", mensajeError);
                return "redirect:/reservas/" + id + "/pago" + (returnTo != null ? "?returnTo=" + returnTo : "");
            }
            
            redirectAttributes.addFlashAttribute("successMessage",
                    "Pago procesado correctamente. Código de referencia: " + response.getReferencia());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/reservas/" + id + "/pago" + (returnTo != null ? "?returnTo=" + returnTo : "");
        } catch (Exception ex) {
            String mensajeError = ex.getMessage() != null ? ex.getMessage() : "No fue posible procesar el pago. Inténtalo nuevamente.";
            redirectAttributes.addFlashAttribute("errorMessage", mensajeError);
            System.out.println("ERROR al procesar pago: " + ex.getMessage());
            ex.printStackTrace();
            return "redirect:/reservas/" + id + "/pago" + (returnTo != null ? "?returnTo=" + returnTo : "");
        }

        if ("historial".equalsIgnoreCase(returnTo)) {
            return reservaService.obtenerReservaPorId(id)
                    .map(reserva -> "redirect:/clientes/historial?id=" + reserva.getCliente().getId())
                    .orElse("redirect:/clientes/historial");
        }

        if ("dashboard".equalsIgnoreCase(returnTo)) {
            return "redirect:/dashboard";
        }

        if (authentication != null) {
            boolean esCliente = authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(rol -> "ROLE_CLIENTE".equals(rol));
            if (esCliente) {
                return "redirect:/dashboard";
            }
        }

        return reservaService.obtenerReservaPorId(id)
                .map(reserva -> "redirect:/clientes/historial?id=" + reserva.getCliente().getId())
                .orElse("redirect:/clientes/historial");
    }
}

