package com.gestion.hotelera.controller;

import com.gestion.hotelera.service.ReservaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reportes")
public class ReportesController {

    private final ReservaService reservaService;

    public ReportesController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping("/generar")
    public String mostrarFormularioGenerarReporte(Model model) {
        model.addAttribute("fechaActual", LocalDate.now());
        model.addAttribute("currentPath", "/reportes/generar");
        return "generar_reporte";
    }

    @GetMapping("/api/ingresos")
    @ResponseBody
    public List<Map<String, Object>> getIngresosPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            if (fechaInicio == null || fechaFin == null) {
                return List.of();
            }
            if (fechaInicio.isAfter(fechaFin)) {
                return List.of();
            }
            return reservaService.getIngresosPorPeriodo(fechaInicio, fechaFin);
        } catch (Exception e) {
            return List.of();
        }
    }

    @GetMapping("/api/movimiento")
    @ResponseBody
    public List<Map<String, Object>> getMovimientoPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            if (fechaInicio == null || fechaFin == null) {
                return List.of();
            }
            if (fechaInicio.isAfter(fechaFin)) {
                return List.of();
            }
            return reservaService.getMovimientoPorPeriodo(fechaInicio, fechaFin);
        } catch (Exception e) {
            return List.of();
        }
    }
}