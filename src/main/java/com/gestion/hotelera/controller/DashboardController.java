package com.gestion.hotelera.controller;

import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.ReservaService;
import com.gestion.hotelera.service.HabitacionService;
import com.gestion.hotelera.service.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private EmpleadoService empleadoService;

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            var roles = auth.getAuthorities();
            model.addAttribute("roles", roles);

            if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_CLIENTE"))) {
                // Los clientes no tienen dashboard, van al index
                return "redirect:/";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar dashboard");
        }

        try {
            var roles = auth.getAuthorities();
            
            // Para clientes, agregar datos específicos
            if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_CLIENTE"))) {
                try {
                    var cliente = clienteService.obtenerPorUsername(auth.getName());
                    if (cliente != null) {
                        System.out.println("=== DEBUG CLIENTE DASHBOARD ===");
                        System.out.println("Cliente ID: " + cliente.getId());
                        System.out.println("Username: " + auth.getName());
                        System.out.println("Cliente DNI: " + cliente.getDni());
                        
                        var reservas = reservaService.obtenerReservasPorClienteId(cliente.getId());
                        
                        // Contar manualmente para consistencia
                        long reservasActivas = reservas.stream()
                            .filter(r -> "ACTIVA".equals(r.getEstadoReserva()) || "PENDIENTE".equals(r.getEstadoReserva()))
                            .count();
                        long reservasFinalizadas = reservas.stream()
                            .filter(r -> "FINALIZADA".equals(r.getEstadoReserva()))
                            .count();
                        
                        System.out.println("Reservas Activas: " + reservasActivas);
                        System.out.println("Reservas Finalizadas: " + reservasFinalizadas);
                        System.out.println("Total Reservas: " + reservas.size());
                        
                        model.addAttribute("cliente", cliente);
                        model.addAttribute("reservasActivas", reservasActivas);
                        model.addAttribute("reservasFinalizadas", reservasFinalizadas);
                        model.addAttribute("reservas", reservas);
                    }
                } catch (Exception e) {
                    model.addAttribute("errorMessage", "Error al cargar datos del cliente");
                }
                return "dashboard";
            }
            
            if (roles.stream().anyMatch(r ->
                    r.getAuthority().equals("ROLE_ADMIN") || r.getAuthority().equals("ROLE_RECEPCIONISTA"))) {
                long totalHabitaciones = habitacionService.contarHabitaciones();
                long totalClientes = clienteService.contarClientes();
                long totalReservas = reservaService.contarReservas();
                long habitacionesDisponibles = habitacionService.contarDisponibles();
                long habitacionesOcupadas = habitacionService.contarOcupadas();
                long habitacionesMantenimiento = habitacionService.contarEnMantenimiento();
                long totalEmpleados = empleadoService.contarEmpleados();
                double ingresosTotales = reservaService.calcularIngresosTotales();
                long reservasPendientes = reservaService.contarReservasPorEstado("PENDIENTE");
                long reservasActivas = reservaService.contarReservasPorEstado("ACTIVA");
                long checkInsHoy = reservaService.contarCheckInsHoy();
                long checkOutsHoy = reservaService.contarCheckOutsHoy();

                model.addAttribute("totalHabitaciones", totalHabitaciones);
                model.addAttribute("totalClientes", totalClientes);
                model.addAttribute("totalReservas", totalReservas);
                model.addAttribute("habitacionesDisponibles", habitacionesDisponibles);
                model.addAttribute("habitacionesOcupadas", habitacionesOcupadas);
                model.addAttribute("habitacionesMantenimiento", habitacionesMantenimiento);
                model.addAttribute("totalEmpleados", totalEmpleados);
                model.addAttribute("ingresosTotales", ingresosTotales);
                model.addAttribute("reservasPendientes", reservasPendientes);
                model.addAttribute("reservasActivas", reservasActivas);
                model.addAttribute("checkInsHoy", checkInsHoy);
                model.addAttribute("checkOutsHoy", checkOutsHoy);

                return "dashboard";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar estadísticas");
        }

        return "redirect:/login";
    }
}