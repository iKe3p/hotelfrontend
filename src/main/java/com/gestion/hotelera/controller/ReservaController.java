package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.HabitacionService;
import com.gestion.hotelera.service.ReservaService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    private final ClienteService clienteService;
    private final HabitacionService habitacionService;
    private final ReservaService reservaService;

    public ReservaController(ClienteService clienteService,
                             HabitacionService habitacionService,
                             ReservaService reservaService) {
        this.clienteService = clienteService;
        this.habitacionService = habitacionService;
        this.reservaService = reservaService;
    }

    // Mostrar reservas del cliente autenticado
    @GetMapping
    public String mostrarReservasCliente(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        Cliente cliente = clienteService.obtenerPorEmail(auth.getName());
        model.addAttribute("clienteEncontrado", cliente);
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("habitacionesDisponibles",
                cliente != null
                        ? habitacionService.obtenerHabitacionesDisponiblesParaCliente(cliente.getId())
                        : habitacionService.obtenerHabitacionesDisponibles());
        return "reservas";
    }

    // Formulario para crear reserva
    @GetMapping("/crear")
    public String showCrearReservaForm(Model model,
                                       @RequestParam(name = "dni", required = false) String dni,
                                       @RequestParam(name = "idCliente", required = false) Long idCliente) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("reserva", new Reserva());

        Long clienteIdParaHabitaciones = null;
        try {
            if (idCliente != null) {
                Optional<Cliente> clientePorId = clienteService.obtenerClientePorId(idCliente);
                if (clientePorId.isPresent()) {
                    clienteIdParaHabitaciones = clientePorId.get().getId();
                }
            } else if (dni != null && !dni.trim().isEmpty() && dni.trim().matches("^\\d{8}$")) {
                Optional<Cliente> clienteOptional = clienteService.buscarClientePorDni(dni.trim());
                if (clienteOptional.isPresent()) {
                    clienteIdParaHabitaciones = clienteOptional.get().getId();
                }
            }

            model.addAttribute("habitacionesDisponibles",
                    clienteIdParaHabitaciones != null
                            ? habitacionService.obtenerHabitacionesDisponiblesParaCliente(clienteIdParaHabitaciones)
                            : habitacionService.obtenerHabitacionesDisponibles());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar habitaciones disponibles");
            return "reservas";
        }

        // Mostrar info del cliente
        if (idCliente != null) {
            clienteService.obtenerClientePorId(idCliente)
                    .ifPresentOrElse(
                            c -> model.addAttribute("clienteEncontrado", c),
                            () -> model.addAttribute("errorMessage", "Cliente no encontrado")
                    );
        } else if (dni != null && !dni.trim().isEmpty()) {
            String dniLimpio = dni.trim();
            if (!dniLimpio.matches("^\\d{8}$")) {
                model.addAttribute("errorMessage", "El DNI debe contener exactamente 8 dígitos numéricos");
            } else {
                clienteService.buscarClientePorDni(dniLimpio)
                        .ifPresentOrElse(
                                c -> model.addAttribute("clienteEncontrado", c),
                                () -> model.addAttribute("errorMessage", "Cliente no encontrado")
                        );
            }
        }

        return "reservas";
    }

    // Buscar cliente por DNI
    @PostMapping("/buscar-cliente")
    public String buscarClienteParaReserva(@RequestParam("dniBuscar") String dni, RedirectAttributes redirectAttributes) {
        String dniLimpio = dni != null ? dni.trim() : "";
        if (!dniLimpio.matches("^\\d{8}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "El DNI debe contener exactamente 8 dígitos numéricos");
            return "redirect:/reservas/crear";
        }

        Optional<Cliente> clienteOptional = clienteService.buscarClientePorDni(dniLimpio);
        if (clienteOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("successMessage", "Cliente encontrado!");
            return "redirect:/reservas/crear?dni=" + dniLimpio;
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Cliente con DNI " + dniLimpio + " no encontrado. Por favor, regístrelo primero.");
            return "redirect:/reservas/crear";
        }
    }

    // CALCULAR COSTO (JSON para Angular)
 @GetMapping("/calcular-costo")
@ResponseBody
public Map<String, Object> calcularCosto(
        @RequestParam("habitacionId") Long habitacionId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

    Map<String, Object> response = new HashMap<>();

    // 1️⃣ Validación inicial de fechas
    if (fechaInicio == null || fechaFin == null) {
        response.put("error", "Las fechas no pueden ser nulas");
        response.put("dias", 0);
        response.put("total", 0);
        return response;
    }

    if (fechaFin.isBefore(fechaInicio)) {
        response.put("error", "La fecha de fin no puede ser anterior a la fecha de inicio");
        response.put("dias", 0);
        response.put("total", 0);
        return response;
    }

    try {
        // 2️⃣ Validación de la habitación
        Optional<Habitacion> habitacionOpt = habitacionService.buscarHabitacionPorId(habitacionId);
        if (habitacionOpt.isEmpty()) {
            response.put("error", "Habitación no encontrada");
            response.put("dias", 0);
            response.put("total", 0);
            return response;
        }

        Habitacion habitacion = habitacionOpt.get();

        if (habitacion.getPrecioPorNoche() == null) {
            response.put("error", "Precio de la habitación no definido");
            response.put("dias", 0);
            response.put("total", 0);
            return response;
        }

        // 3️⃣ Calcular días de estadía
        int dias = reservaService.calcularDiasEstadia(fechaInicio, fechaFin);
        if (dias <= 0) {
            response.put("error", "Fechas inválidas");
            response.put("dias", 0);
            response.put("total", 0);
            return response;
        }

        // 4️⃣ Calcular total a pagar
        double total = reservaService.calcularTotalPagar(habitacion.getPrecioPorNoche(), dias);

        // 5️⃣ Devolver resultados
        response.put("dias", dias);
        response.put("total", total);

    } catch (Exception e) {
        e.printStackTrace();
        response.put("error", "Error interno al calcular el costo");
        response.put("dias", 0);
        response.put("total", 0);
    }

    return response;
}

    // Guardar reserva
    @PostMapping("/guardar")
    public String guardarReserva(@ModelAttribute Reserva reserva,
                                 @RequestParam("clienteDni") String clienteDni,
                                 @RequestParam("habitacionId") Long habitacionId,
                                 RedirectAttributes redirectAttributes,
                                 Authentication auth) {

        try {
            String dniLimpio = clienteDni != null ? clienteDni.trim() : "";
            if (!dniLimpio.matches("^\\d{8}$")) {
                redirectAttributes.addFlashAttribute("errorMessage", "El DNI debe contener exactamente 8 dígitos numéricos");
                return "redirect:/reservas";
            }

            Optional<Cliente> clienteOptional = clienteService.buscarClientePorDni(dniLimpio);
            if (clienteOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error: Cliente no encontrado para el DNI proporcionado.");
                return "redirect:/reservas";
            }
            reserva.setCliente(clienteOptional.get());

            Optional<Habitacion> habitacionOpt = habitacionService.buscarHabitacionPorId(habitacionId);
            if (habitacionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Habitación no encontrada.");
                return "redirect:/reservas";
            }
            reserva.setHabitacion(habitacionOpt.get());

            if (reserva.getFechaInicio().isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "La fecha de inicio de la reserva no puede ser anterior a la fecha actual.");
                return "redirect:/reservas";
            }

            reserva.setEstadoReserva("PENDIENTE");
            Reserva reservaGuardada = reservaService.crearOActualizarReserva(reserva);
            redirectAttributes.addFlashAttribute("successMessage", "Reserva creada exitosamente. Puedes añadir servicios adicionales antes del pago.");

            String returnTo = (auth != null && auth.isAuthenticated() &&
                    auth.getAuthorities().stream().anyMatch(a ->
                            "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_RECEPCIONISTA".equals(a.getAuthority())))
                    ? "historial" : null;

            String redirectUrl = "/reservas/" + reservaGuardada.getId() + "/servicios";
            if (returnTo != null) redirectUrl += "?returnTo=" + returnTo;
            return "redirect:" + redirectUrl;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear la reserva: " + e.getMessage());
            return "redirect:/reservas";
        }
    }

    // Cancelar reserva
    @PostMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes,
                                  @RequestHeader(value = "Referer", required = false) String referer,
                                  Authentication auth) {
        try {
            String userRole = auth.getAuthorities().iterator().next().getAuthority();
            if (reservaService.cancelarReserva(id, userRole)) {
                redirectAttributes.addFlashAttribute("successMessage", "Reserva cancelada exitosamente.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No se pudo cancelar la reserva.");
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    // Cancelar reserva desde cliente (sin permisos)
    @PostMapping("/cancelar-cliente/{id}")
    public String cancelarReservaCliente(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication auth) {
        redirectAttributes.addFlashAttribute("errorMessage", "Usted no tiene permisos para cancelar su reserva. Por favor, comuníquese con recepción para realizar esta acción.");
        return "redirect:/dashboard";
    }

    // Finalizar reserva
    @PostMapping("/finalizar/{id}")
    public String finalizarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes,
                                   @RequestHeader(value = "Referer", required = false) String referer) {

        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID de reserva inválido.");
            return "redirect:" + (referer != null ? referer : "/dashboard");
        }

        try {
            var reservaOpt = reservaService.obtenerReservaPorId(id);
            if (reservaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Reserva no encontrada.");
                return "redirect:" + (referer != null ? referer : "/dashboard");
            }

            var reserva = reservaOpt.get();
            if ("FINALIZADA".equals(reserva.getEstadoReserva())) {
                redirectAttributes.addFlashAttribute("errorMessage", "La reserva ya está finalizada.");
                return "redirect:" + (referer != null ? referer : "/dashboard");
            }

            reservaService.finalizarReserva(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reserva finalizada exitosamente.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al finalizar reserva: " + e.getMessage());
        }

        return "redirect:" + (referer != null ? referer : "/dashboard");
    }
}
