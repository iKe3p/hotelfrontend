package com.gestion.hotelera.controller;

import com.gestion.hotelera.exception.ClienteConReservasActivasException;
import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.service.ClienteService;
import com.gestion.hotelera.service.ReservaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ReservaService reservaService;

    public ClienteController(ClienteService clienteService, ReservaService reservaService) {
        this.clienteService = clienteService;
        this.reservaService = reservaService;
    }
    @GetMapping("/editar/{id}")
    public String editarCliente(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID inválido");
            return "redirect:/clientes/historial";
        }
        
        try {
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(id);
            if (clienteOpt.isPresent()) {
                model.addAttribute("cliente", clienteOpt.get());
                return "editarCliente";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cargar cliente: " + e.getMessage());
            return "redirect:/clientes/historial";
        }
        
        redirectAttributes.addFlashAttribute("errorMessage", "Cliente no encontrado");
        return "redirect:/clientes/historial";
    }

    @PostMapping("/editar/{id}")
    public String actualizarCliente(@PathVariable Long id,
                                    @ModelAttribute Cliente cliente,
                                    RedirectAttributes redirectAttributes) {
        System.out.println("=== ACTUALIZANDO CLIENTE ID: " + id + " ===");
        if (cliente == null || id == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos inválidos.");
            return "redirect:/clientes/historial";
        }
        
        cliente.setId(id);
        
        try {
            clienteService.actualizarCliente(cliente);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente actualizado correctamente.");
            return "redirect:/clientes/historial?id=" + id;
        } catch (Exception e) {
            System.out.println("Error al actualizar cliente: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/clientes/editar/" + id;
        }
    }

    @GetMapping("/registrar")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "registroCliente";
    }

    @PostMapping("/guardar")
    public String guardarCliente(@ModelAttribute("cliente") Cliente cliente,
                                 RedirectAttributes redirectAttributes) {
        try {
            Cliente guardado = clienteService.crearCliente(cliente);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente registrado correctamente: "
                    + guardado.getNombres() + " " + guardado.getApellidos());
            // Redirigir a flujo de reserva para el cliente recién creado
            return "redirect:/reservas/crear?idCliente=" + guardado.getId();
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("cliente", cliente);
            return "redirect:/clientes/registrar";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error al registrar el cliente.");
            redirectAttributes.addFlashAttribute("cliente", cliente);
            return "redirect:/clientes/registrar";
        }
    }

    @GetMapping("/dashboard")
    public String mostrarDashboardCliente(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("rol", auth.getAuthorities());
        return "cliente/dashboard";
    }

    @GetMapping("/historial")
    public String mostrarHistorialClientes(
            Model model,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", required = false, defaultValue = "asc") String sortDir,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "dni", required = false) String dni
    ) {
        if (dni != null && !dni.isBlank()) {
            // Validar que el DNI tenga exactamente 8 dígitos numéricos
            String dniLimpio = dni.trim();
            if (!dniLimpio.matches("^\\d{8}$")) {
                model.addAttribute("errorMessage", "El DNI debe contener exactamente 8 dígitos numéricos");
            } else {
                Optional<Cliente> porDni = clienteService.buscarPorDniOptional(dniLimpio);
                if (porDni.isPresent()) {
                Cliente cliente = porDni.get();
                model.addAttribute("cliente", cliente);
                List<Reserva> reservasCliente = reservaService.obtenerReservasPorClienteId(cliente.getId());
                model.addAttribute("reservasCliente", reservasCliente);
                return "historialCliente";
                } else {
                    model.addAttribute("errorMessage", "No se encontró cliente con DNI: " + dniLimpio);
                }
            }
        }

        if (id != null) {
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(id);
            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                model.addAttribute("cliente", cliente);
                List<Reserva> reservasCliente = reservaService.obtenerReservasPorClienteId(cliente.getId());
                model.addAttribute("reservasCliente", reservasCliente);
                return "historialCliente";
            } else {
                model.addAttribute("errorMessage", "Cliente no encontrado.");
            }
        }

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.max(size, 1), sort);
        Page<Cliente> clientesPage = clienteService.obtenerClientesPaginados(pageRequest, search);
        model.addAttribute("clientesPage", clientesPage);
        model.addAttribute("currentPage", clientesPage.getNumber());
        model.addAttribute("pageSize", clientesPage.getSize());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        return "historialCliente";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean eliminado = clienteService.eliminarClientePorId(id);
            if (eliminado) {
                redirectAttributes.addFlashAttribute("successMessage", "Cliente eliminado correctamente.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No se pudo eliminar el cliente.");
            }
            return "redirect:/clientes/historial";
        } catch (ClienteConReservasActivasException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se puede eliminar el cliente porque tiene reservas activas. Debe cancelar o finalizar las reservas primero.");
            redirectAttributes.addFlashAttribute("reservasBloqueo", ex.getReservasActivas());
            return "redirect:/clientes/historial?id=" + ex.getClienteId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar cliente: " + e.getMessage());
            return "redirect:/clientes/historial";
        }
    }
}