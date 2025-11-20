package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Auditoria;
import com.gestion.hotelera.model.Empleado;
import com.gestion.hotelera.repository.AuditoriaRepository;
import com.gestion.hotelera.repository.EmpleadoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final EmpleadoRepository empleadoRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository, EmpleadoRepository empleadoRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Auditoria registrarAccion(String tipoAccion, String detalleAccion, String entidadAfectada, Long entidadAfectadaId) {
        if (tipoAccion == null || tipoAccion.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de acción no puede estar vacío");
        }
        if (detalleAccion == null || detalleAccion.trim().isEmpty()) {
            throw new IllegalArgumentException("El detalle de acción no puede estar vacío");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = null;
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            currentUsername = authentication.getName();
        } else if (authentication != null && authentication.isAuthenticated() && (authentication.getPrincipal() instanceof String)) {
            currentUsername = authentication.getName();
        }

        Empleado empleado = null;
        if (currentUsername != null && !currentUsername.equals("anonymousUser")) {
            Optional<Empleado> empleadoOptional = empleadoRepository.findByUsuarioUsername(currentUsername);
            empleado = empleadoOptional.orElse(null);
        }

        Auditoria logEntry = new Auditoria();
        logEntry.setTimestamp(LocalDateTime.now());
        logEntry.setEmpleado(empleado);
        logEntry.setTipoAccion(tipoAccion);
        logEntry.setDetalleAccion(detalleAccion);
        logEntry.setEntidadAfectada(entidadAfectada);
        logEntry.setEntidadAfectadaId(entidadAfectadaId);

        return auditoriaRepository.save(logEntry);
    }

    public Page<Auditoria> obtenerTodosLosLogs(Pageable pageable) {
        if (pageable == null) {
            pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        }
        return auditoriaRepository.findAll(pageable);
    }

    public Page<Auditoria> obtenerLogsPorDniEmpleado(String dni, Pageable pageable) {
        if (dni == null || dni.trim().isEmpty()) {
            return Page.empty();
        }
        try {
            return auditoriaRepository.findByEmpleadoDni(dni.trim(), pageable);
        } catch (Exception e) {
            return Page.empty();
        }
    }

    public Page<Auditoria> searchLogs(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return obtenerTodosLosLogs(pageable);
        }
        try {
            String sanitizedKeyword = keyword.trim().substring(0, Math.min(keyword.trim().length(), 100));
            return auditoriaRepository.findByTipoAccionContainingIgnoreCaseOrDetalleAccionContainingIgnoreCase(
                sanitizedKeyword, sanitizedKeyword, pageable);
        } catch (Exception e) {
            return Page.empty();
        }
    }
}