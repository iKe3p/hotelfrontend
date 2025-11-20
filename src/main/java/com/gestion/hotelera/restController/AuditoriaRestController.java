package com.gestion.hotelera.restController;

import com.gestion.hotelera.model.Auditoria;
import com.gestion.hotelera.service.AuditoriaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auditoria")
@CrossOrigin(originPatterns = "*")
public class AuditoriaRestController {

    private final AuditoriaService auditoriaService;

    public AuditoriaRestController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<Auditoria>> obtenerLogs(
            @RequestParam(name = "dniEmpleado", required = false) String dniEmpleado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Auditoria> logsPage;

        try {
            if (dniEmpleado != null && !dniEmpleado.trim().isEmpty()) {
                String dniSanitizado = dniEmpleado.trim().replaceAll("[^0-9]", "");
                if (dniSanitizado.length() == 8) {
                    logsPage = auditoriaService.obtenerLogsPorDniEmpleado(dniSanitizado, pageable);
                } else {
                    logsPage = auditoriaService.obtenerTodosLosLogs(pageable);
                }
            } else if (search != null && !search.trim().isEmpty()) {
                String searchSanitizado = search.trim().substring(0, Math.min(search.trim().length(), 50));
                logsPage = auditoriaService.searchLogs(searchSanitizado, pageable);
            } else {
                logsPage = auditoriaService.obtenerTodosLosLogs(pageable);
            }
        } catch (Exception e) {
            logsPage = auditoriaService.obtenerTodosLosLogs(pageable);
        }

        return ResponseEntity.ok(logsPage);
    }
}

