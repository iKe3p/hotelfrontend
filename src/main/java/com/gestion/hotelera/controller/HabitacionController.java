package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.service.HabitacionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/habitaciones")
public class HabitacionController {

    private final HabitacionService habitacionService;

    public HabitacionController(HabitacionService habitacionService) {
        this.habitacionService = habitacionService;
    }

    @GetMapping
    public String listarHabitaciones(Model model) {
        try {
            model.addAttribute("habitaciones", habitacionService.obtenerTodasLasHabitaciones());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar habitaciones");
        }
        return "habitaciones";
    }

    // Vista pública para clientes: solo tipo y precio, estilo tarjetas
    @GetMapping("/publico")
    public String listarHabitacionesPublico(Model model) {
        model.addAttribute("habitaciones", habitacionService.obtenerTodasLasHabitaciones());
        return "habitaciones_publico";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/nueva")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("habitacion", new Habitacion());
        model.addAttribute("accion", "nueva");
        return "habitacion-form";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/guardar")
    public String guardarHabitacion(@ModelAttribute Habitacion habitacion, RedirectAttributes redirectAttributes) {
        try {
            if (habitacion.getId() == null || habitacion.getId() == 0) {
                habitacionService.crearHabitacion(habitacion);
                redirectAttributes.addFlashAttribute("successMessage", "Habitación creada exitosamente.");
            } else {
                habitacionService.actualizarHabitacion(habitacion);
                redirectAttributes.addFlashAttribute("successMessage", "Habitación actualizada exitosamente.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/habitaciones";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID de habitación inválido");
            return "redirect:/habitaciones";
        }
        try {
            Optional<Habitacion> habitacionOptional = habitacionService.buscarHabitacionPorId(id);
            if (habitacionOptional.isPresent()) {
                model.addAttribute("habitacion", habitacionOptional.get());
                model.addAttribute("accion", "editar");
                return "habitacion-form";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Habitación no encontrada");
                return "redirect:/habitaciones";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cargar habitación");
            return "redirect:/habitaciones";
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/eliminar")
    public String eliminarHabitacion(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            habitacionService.eliminarHabitacion(id);
            redirectAttributes.addFlashAttribute("successMessage", "Habitación eliminada exitosamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar la habitación: " + e.getMessage());
        }
        return "redirect:/habitaciones";
    }
}