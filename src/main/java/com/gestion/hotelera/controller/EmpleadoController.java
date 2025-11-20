package com.gestion.hotelera.controller;

import com.gestion.hotelera.model.Empleado;
import com.gestion.hotelera.model.Usuario;
import com.gestion.hotelera.service.EmpleadoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @GetMapping("/registrar")
    public String showRegistroEmpleadoForm(Model model) {
        if (!model.containsAttribute("empleado")) {
            Empleado empleado = new Empleado();
            empleado.setUsuario(new Usuario());
            model.addAttribute("empleado", empleado);
        }
        return "registrarEmpleado";
    }

    @PostMapping("/registrar")
    public String registrarEmpleado(@Valid @ModelAttribute("empleado") Empleado empleado,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.empleado", result);
            redirectAttributes.addFlashAttribute("empleado", empleado);
            redirectAttributes.addFlashAttribute("errorMessage", "Por favor, corrige los errores en el formulario.");
            return "redirect:/empleados/registrar";
        }

        try {
            Empleado nuevoEmpleado = empleadoService.registrarRecepcionista(empleado);
            redirectAttributes.addFlashAttribute("successMessage", "Recepcionista '" + nuevoEmpleado.getNombres() + " " + nuevoEmpleado.getApellidos() + "' registrado exitosamente!");
            return "redirect:/empleados/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("empleado", empleado);
            return "redirect:/empleados/registrar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error inesperado al registrar el recepcionista");
            redirectAttributes.addFlashAttribute("empleado", empleado);
            return "redirect:/empleados/registrar";
        }
    }

    @GetMapping("/lista")
    public String listarEmpleados(Model model) {
        model.addAttribute("empleados", empleadoService.obtenerTodosLosEmpleados());
        return "listaEmpleados";
    }

    @GetMapping("/editar/{id}")
    public String showEditarEmpleadoForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        if (id == null || id <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID de empleado inválido");
            return "redirect:/empleados/lista";
        }
        if (!model.containsAttribute("empleado")) {
            try {
                Optional<Empleado> empleadoOptional = empleadoService.buscarEmpleadoPorId(id);
                if (empleadoOptional.isPresent()) {
                    model.addAttribute("empleado", empleadoOptional.get());
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Empleado no encontrado");
                    return "redirect:/empleados/lista";
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error al cargar empleado");
                return "redirect:/empleados/lista";
            }
        }
        return "editarEmpleado";
    }

    @PostMapping("/actualizar")
    public String actualizarEmpleado(@Valid @ModelAttribute("empleado") Empleado empleado,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            // Ignorar errores de contraseña vacía en actualización
            result.getFieldErrors().removeIf(error -> 
                "usuario.password".equals(error.getField()) && 
                (empleado.getUsuario().getPassword() == null || empleado.getUsuario().getPassword().isEmpty())
            );
            
            if (result.hasErrors()) {
                redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.empleado", result);
                redirectAttributes.addFlashAttribute("empleado", empleado);
                redirectAttributes.addFlashAttribute("errorMessage", "Por favor, corrige los errores en el formulario.");
                return "redirect:/empleados/editar/" + empleado.getId();
            }
        }

        try {
            Empleado empleadoActualizado = empleadoService.actualizarEmpleado(empleado);
            redirectAttributes.addFlashAttribute("successMessage", "Empleado '" + empleadoActualizado.getNombres() + " " + empleadoActualizado.getApellidos() + "' actualizado exitosamente!");
            return "redirect:/empleados/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("empleado", empleado);
            return "redirect:/empleados/editar/" + empleado.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error inesperado al actualizar el empleado");
            redirectAttributes.addFlashAttribute("empleado", empleado);
            return "redirect:/empleados/editar/" + empleado.getId();
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (empleadoService.eliminarEmpleado(id)) {
                redirectAttributes.addFlashAttribute("successMessage", "Empleado eliminado exitosamente.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No se pudo eliminar el empleado. Es posible que no exista.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el empleado");
        }
        return "redirect:/empleados/lista";
    }
}