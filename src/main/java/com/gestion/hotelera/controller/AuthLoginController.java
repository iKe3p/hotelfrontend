package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.ReservaService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthLoginController {

    private final ClienteService clienteService;
    private final ReservaService reservaService;

    public AuthLoginController(ClienteService clienteService, ReservaService reservaService) {
        this.clienteService = clienteService;
        this.reservaService = reservaService;
    }

    @GetMapping({"/", "/index"})
    public String mostrarIndex(Model model, Authentication auth) {
        boolean isLoggedIn = false;
        String username = "";
        String rol = "";

        try {
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                isLoggedIn = true;
                username = auth.getName();
                
                // Intentar obtener información del cliente
                try {
                    Cliente cliente = clienteService.obtenerPorUsername(auth.getName());
                    if (cliente != null) {
                        username = cliente.getNombres();
                        rol = "ROLE_CLIENTE";
                        
                        // Agregar conteos de reservas para el cliente
                        long totalReservas = reservaService.contarReservasPorCliente(auth.getName());
                        long reservasActivas = reservaService.contarReservasActivasPorCliente(auth.getName());
                        long reservasFinalizadas = reservaService.contarReservasFinalizadasPorCliente(auth.getName());
                        
                        model.addAttribute("totalReservas", totalReservas);
                        model.addAttribute("reservasActivas", reservasActivas);
                        model.addAttribute("reservasFinalizadas", reservasFinalizadas);
                    }
                } catch (Exception e) {
                    // Si no es cliente, mantener el username original
                }
            }
        } catch (Exception e) {
            isLoggedIn = false;
            username = "";
            rol = "";
        }

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("username", username);
        model.addAttribute("rol", rol);
        return "index";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/registro")
    public String showRegisterPage() {
        return "register";
    }
    
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/";
    }
}