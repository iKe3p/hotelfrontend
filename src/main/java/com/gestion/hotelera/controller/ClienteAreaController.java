package com.gestion.hotelera.controller;

import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.ReservaService;
import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Reserva;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.List;

@Controller
public class ClienteAreaController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ReservaService reservaService;

    @GetMapping("/cliente/editar")
    public String editarDatosCliente(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            String username = auth.getName();
            Cliente clienteExistente = clienteService.obtenerPorUsername(username);
            if (clienteExistente == null) {
                Optional<Cliente> clienteOpt = clienteService.buscarClientePorDni(username);
                clienteExistente = clienteOpt.orElse(null);
            }
            
            if (clienteExistente != null) {
                model.addAttribute("cliente", clienteExistente);
                return "editarCliente";
            } else {
                return "redirect:/cliente/dashboard?error=cliente-no-encontrado";
            }
        } catch (Exception e) {
            return "redirect:/cliente/dashboard?error=editar-error";
        }
    }
    
    @PostMapping("/cliente/editar")
    public String actualizarDatosCliente(@ModelAttribute Cliente cliente,
                                         Authentication auth,
                                         RedirectAttributes redirectAttributes) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            String username = auth.getName();
            Cliente clienteExistente = clienteService.obtenerPorUsername(username);
            if (clienteExistente == null) {
                Optional<Cliente> clienteOpt = clienteService.buscarClientePorDni(username);
                clienteExistente = clienteOpt.orElse(null);
            }
            
            if (clienteExistente != null) {
                cliente.setId(clienteExistente.getId());
                clienteService.actualizarCliente(cliente);
                redirectAttributes.addFlashAttribute("successMessage", "Datos actualizados correctamente.");
                return "redirect:/cliente/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Cliente no encontrado.");
                return "redirect:/cliente/dashboard";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/cliente/editar";
        }
    }

    @GetMapping("/cliente/dashboard")
    public String mostrarDashboardCliente(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            String username = auth.getName();
            // Buscar cliente por username primero, luego por DNI
            Cliente cliente = clienteService.obtenerPorUsername(username);
            Optional<Cliente> clienteOpt = cliente != null ? Optional.of(cliente) : clienteService.buscarClientePorDni(username);
            if (clienteOpt.isPresent()) {
                Cliente clienteEncontrado = clienteOpt.get();
                List<Reserva> reservas = reservaService.obtenerReservasPorClienteId(clienteEncontrado.getId());
                System.out.println("=== VERIFICANDO RESERVAS CLIENTE ID: " + clienteEncontrado.getId() + " ===");
                long reservasActivas = 0;
                long reservasFinalizadas = 0;
                
                for (Reserva r : reservas) {
                    if ("ACTIVA".equals(r.getEstadoReserva()) || "PENDIENTE".equals(r.getEstadoReserva())) {
                        reservasActivas++;
                    } else if ("FINALIZADA".equals(r.getEstadoReserva())) {
                        reservasFinalizadas++;
                    }
                }
                System.out.println("TOTAL: Activas=" + reservasActivas + ", Finalizadas=" + reservasFinalizadas);
                
                model.addAttribute("cliente", clienteEncontrado);
                model.addAttribute("reservas", reservas);
                model.addAttribute("reservasActivas", reservasActivas);
                model.addAttribute("reservasFinalizadas", reservasFinalizadas);
                model.addAttribute("esCliente", true);
            } else {
                return "redirect:/?error=cliente-no-encontrado";
            }
        } catch (Exception e) {
            return "redirect:/?error=dashboard-error";
        }
        
        return "dashboard";
    }
}