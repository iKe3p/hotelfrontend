package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.repository.HabitacionRepository;
import com.gestion.hotelera.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class HabitacionService {
    private final HabitacionRepository habitacionRepository;
    private final AuditoriaService auditoriaService;
    private final ReservaRepository reservaRepository;

    @Autowired
    public HabitacionService(HabitacionRepository habitacionRepository, AuditoriaService auditoriaService, ReservaRepository reservaRepository) {
        this.habitacionRepository = habitacionRepository;
        this.auditoriaService = auditoriaService;
        this.reservaRepository = reservaRepository;
    }

    // Constructor de compatibilidad para tests existentes
    public HabitacionService(HabitacionRepository habitacionRepository, AuditoriaService auditoriaService) {
        this.habitacionRepository = habitacionRepository;
        this.auditoriaService = auditoriaService;
        this.reservaRepository = null;
    }

    public long contarHabitaciones() {
        return habitacionRepository.count();
    }

    public long contarDisponibles() {
        // Contar habitaciones realmente disponibles (excluyendo las que tienen reservas activas)
        if (reservaRepository != null) {
            LocalDate hoy = java.time.LocalDate.now();
            long total = habitacionRepository.count();
            long ocupadas = contarOcupadas();
            long mantenimiento = contarEnMantenimiento();
            return total - ocupadas - mantenimiento;
        }
        return habitacionRepository.countByEstado("DISPONIBLE");
    }

    public long contarOcupadas() {
        // Contar habitaciones ocupadas, incluyendo las que tienen reservas activas/pendientes
        if (reservaRepository != null) {
            LocalDate hoy = java.time.LocalDate.now();
            // Obtener todas las habitaciones
            List<Habitacion> todas = habitacionRepository.findAll();
            
            long contador = todas.stream()
                    .filter(habitacion -> {
                        // Si está marcada como ocupada o en mantenimiento
                        if ("OCUPADA".equalsIgnoreCase(habitacion.getEstado())) {
                            return true;
                        }
                        if ("MANTENIMIENTO".equalsIgnoreCase(habitacion.getEstado())) {
                            return false; // Mantenimiento no cuenta como ocupada para disponibilidad
                        }
                        
                        // Verificar si tiene reservas activas o pendientes en el rango de fechas actual
                        List<com.gestion.hotelera.model.Reserva> reservasActivas = reservaRepository.findAll().stream()
                                .filter(r -> r.getHabitacion() != null && 
                                       r.getHabitacion().getId().equals(habitacion.getId()) &&
                                       (r.getEstadoReserva().equals("ACTIVA") || r.getEstadoReserva().equals("PENDIENTE")) &&
                                       r.getFechaInicio() != null &&
                                       r.getFechaFin() != null &&
                                       !r.getFechaInicio().isAfter(hoy) &&
                                       !r.getFechaFin().isBefore(hoy))
                                .toList();
                        return !reservasActivas.isEmpty();
                    })
                    .count();
            
            return contador;
        }
        return habitacionRepository.countByEstado("OCUPADA");
    }

    public long contarEnMantenimiento() {
        return habitacionRepository.countByEstado("MANTENIMIENTO");
    }

    public List<Habitacion> obtenerTodasLasHabitaciones() {
        return habitacionRepository.findAll();
    }

    public Optional<Habitacion> buscarHabitacionPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return habitacionRepository.findById(id);
    }

    public Optional<Habitacion> buscarHabitacionPorNumero(String numero) {
        return habitacionRepository.findByNumero(numero);
    }

    public List<Habitacion> obtenerHabitacionesDisponibles() {
        // Obtener habitaciones marcadas como disponibles
        List<Habitacion> habitacionesDisponiblesPorEstado = habitacionRepository.findByEstado("DISPONIBLE");
        
        // Validar adicionalmente que no tengan reservas activas/pendientes en el rango de fechas actual
        if (reservaRepository != null) {
            LocalDate hoy = java.time.LocalDate.now();
            return habitacionesDisponiblesPorEstado.stream()
                    .filter(habitacion -> {
                        // Verificar que no tenga reservas activas o pendientes que se solapen con hoy
                        List<com.gestion.hotelera.model.Reserva> reservasActivas = reservaRepository.findAll().stream()
                                .filter(r -> r.getHabitacion() != null && 
                                       r.getHabitacion().getId().equals(habitacion.getId()) &&
                                       (r.getEstadoReserva().equals("ACTIVA") || r.getEstadoReserva().equals("PENDIENTE")) &&
                                       r.getFechaInicio() != null &&
                                       r.getFechaFin() != null &&
                                       !r.getFechaInicio().isAfter(hoy) &&
                                       !r.getFechaFin().isBefore(hoy))
                                .toList();
                        return reservasActivas.isEmpty();
                    })
                    .toList();
        }
        
        return habitacionesDisponiblesPorEstado;
    }

    public long contarTotalHabitaciones() {
        return habitacionRepository.count();
    }

    public List<Habitacion> obtenerHabitacionesOcupadas() {
        // Obtener habitaciones marcadas como ocupadas
        List<Habitacion> habitacionesOcupadasPorEstado = habitacionRepository.findByEstado("OCUPADA");
        
        // Incluir también habitaciones que tienen reservas activas/pendientes aunque estén marcadas como disponibles
        if (reservaRepository != null) {
            LocalDate hoy = java.time.LocalDate.now();
            List<Habitacion> habitacionesConReservasActivas = habitacionRepository.findAll().stream()
                    .filter(habitacion -> {
                        // Si ya está en la lista por estado, no duplicar
                        if (habitacionesOcupadasPorEstado.stream()
                                .anyMatch(h -> h.getId().equals(habitacion.getId()))) {
                            return false;
                        }
                        
                        // Verificar si tiene reservas activas o pendientes
                        List<com.gestion.hotelera.model.Reserva> reservasActivas = reservaRepository.findAll().stream()
                                .filter(r -> r.getHabitacion() != null && 
                                       r.getHabitacion().getId().equals(habitacion.getId()) &&
                                       (r.getEstadoReserva().equals("ACTIVA") || r.getEstadoReserva().equals("PENDIENTE")) &&
                                       r.getFechaInicio() != null &&
                                       r.getFechaFin() != null &&
                                       !r.getFechaInicio().isAfter(hoy) &&
                                       !r.getFechaFin().isBefore(hoy))
                                .toList();
                        return !reservasActivas.isEmpty();
                    })
                    .toList();
            
            // Combinar ambas listas sin duplicados
            java.util.Set<Long> idsAgregados = new java.util.HashSet<>();
            List<Habitacion> resultado = new java.util.ArrayList<>();
            
            for (Habitacion h : habitacionesOcupadasPorEstado) {
                if (!idsAgregados.contains(h.getId())) {
                    resultado.add(h);
                    idsAgregados.add(h.getId());
                }
            }
            
            for (Habitacion h : habitacionesConReservasActivas) {
                if (!idsAgregados.contains(h.getId()) && !"MANTENIMIENTO".equalsIgnoreCase(h.getEstado())) {
                    resultado.add(h);
                    idsAgregados.add(h.getId());
                }
            }
            
            return resultado;
        }
        
        return habitacionesOcupadasPorEstado;
    }

    public List<Habitacion> obtenerHabitacionesEnMantenimiento() {
        return habitacionRepository.findByEstado("MANTENIMIENTO");
    }

    @Transactional
    public Habitacion crearHabitacion(Habitacion habitacion) {
        if (habitacionRepository.findByNumero(habitacion.getNumero()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una habitación con el número '" + habitacion.getNumero() + "'.");
        }
        Habitacion nuevaHabitacion = habitacionRepository.save(habitacion);
        Long habitacionId = nuevaHabitacion.getId();
        if (habitacionId != null) {
            auditoriaService.registrarAccion(
                    "CREACION_HABITACION",
                    "Nueva habitación registrada: #" + nuevaHabitacion.getNumero() + " (" + nuevaHabitacion.getTipo() + ", $" + nuevaHabitacion.getPrecioPorNoche() + ")",
                    "Habitacion",
                    Objects.requireNonNull(habitacionId)
            );
        }
        return nuevaHabitacion;
    }

    @Transactional
    public Habitacion actualizarHabitacion(Habitacion habitacionActualizada) {
        if (habitacionActualizada == null || habitacionActualizada.getId() == null) {
            throw new IllegalArgumentException("La habitación y su ID no pueden ser nulos");
        }
        return habitacionRepository.findById(habitacionActualizada.getId())
                .map(habitacionExistente -> {
                    // Validar número solo si es diferente
                    if (!habitacionExistente.getNumero().equals(habitacionActualizada.getNumero())) {
                        Optional<Habitacion> existeOtra = habitacionRepository.findByNumero(habitacionActualizada.getNumero());
                        if (existeOtra.isPresent() && !existeOtra.get().getId().equals(habitacionExistente.getId())) {
                            throw new IllegalArgumentException("El número de habitación '" + habitacionActualizada.getNumero() + "' ya está en uso.");
                        }
                    }
                    
                    habitacionExistente.setNumero(habitacionActualizada.getNumero());
                    habitacionExistente.setTipo(habitacionActualizada.getTipo());
                    habitacionExistente.setPrecioPorNoche(habitacionActualizada.getPrecioPorNoche());
                    habitacionExistente.setEstado(habitacionActualizada.getEstado());

                    Habitacion habitacionGuardada = habitacionRepository.save(habitacionExistente);
                    Long habitacionGuardadaId = habitacionGuardada.getId();
                    if (habitacionGuardadaId != null) {
                        auditoriaService.registrarAccion(
                                "ACTUALIZACION_HABITACION",
                                "Habitación #" + habitacionGuardada.getNumero() + " (ID: " + habitacionGuardadaId + ") actualizada. Nuevo estado: " + habitacionGuardada.getEstado(),
                                "Habitacion",
                                Objects.requireNonNull(habitacionGuardadaId)
                        );
                    }
                    return habitacionGuardada;
                })
                .orElseThrow(() -> new IllegalArgumentException("Habitación con ID " + habitacionActualizada.getId() + " no encontrada para actualizar."));
    }

    @Transactional
    public void actualizarEstadoHabitacion(Long id, String nuevoEstado) {
        if (id == null) {
            return;
        }
        habitacionRepository.findById(id).ifPresent(habitacion -> {
            String estadoAnterior = habitacion.getEstado();
            habitacion.setEstado(nuevoEstado);
            habitacionRepository.save(habitacion);
            if (habitacion.getId() != null) {
                auditoriaService.registrarAccion(
                        "CAMBIO_ESTADO_HABITACION",
                        "Estado de habitación #" + habitacion.getNumero() + " (ID: " + habitacion.getId() + ") cambiado de '" + estadoAnterior + "' a '" + nuevoEstado + "'.",
                        "Habitacion",
                        habitacion.getId()
                );
            }
        });
    }

    public void inicializarHabitacionesSiNoExisten() {
        if (habitacionRepository.count() == 0) {
            crearHabitacion(new Habitacion("101", "Simple", 50.0, "DISPONIBLE"));
            crearHabitacion(new Habitacion("102", "Doble", 80.0, "DISPONIBLE"));
            crearHabitacion(new Habitacion("103", "Suite", 150.0, "DISPONIBLE"));
            crearHabitacion(new Habitacion("201", "Simple", 55.0, "DISPONIBLE"));
            crearHabitacion(new Habitacion("202", "Doble", 85.0, "OCUPADA"));
            crearHabitacion(new Habitacion("203", "Suite", 160.0, "MANTENIMIENTO"));
            System.out.println("Habitaciones inicializadas en la base de datos.");
        }
    }

    public boolean estaDisponible(Long habitacionId) {
        return estaDisponible(habitacionId, null, null);
    }

    /**
     * Verifica si una habitación está disponible, considerando reservas activas en un rango de fechas.
     * @param habitacionId ID de la habitación
     * @param fechaInicio Fecha de inicio de la reserva (opcional, si es null solo verifica estado)
     * @param fechaFin Fecha de fin de la reserva (opcional, si es null solo verifica estado)
     * @return true si la habitación está disponible
     */
    public boolean estaDisponible(Long habitacionId, LocalDate fechaInicio, LocalDate fechaFin) {
        if (habitacionId == null) {
            return false;
        }
        
        Optional<Habitacion> habitacionOpt = habitacionRepository.findById(habitacionId);
        if (habitacionOpt.isEmpty()) {
            return false;
        }
        
        Habitacion habitacion = habitacionOpt.get();
        
        // Si está en mantenimiento, no está disponible
        if ("MANTENIMIENTO".equalsIgnoreCase(habitacion.getEstado())) {
            return false;
        }
        
        // Si se proporcionan fechas, verificar conflictos con reservas activas
        if (fechaInicio != null && fechaFin != null && reservaRepository != null) {
            boolean tieneConflicto = reservaRepository.existeReservaEnRangoFechas(
                habitacionId, fechaInicio, fechaFin, null
            );
            return !tieneConflicto;
        }
        
        // Si no se proporcionan fechas, verificar estado actual y reservas activas hoy
        if (reservaRepository != null) {
            LocalDate hoy = java.time.LocalDate.now();
            List<com.gestion.hotelera.model.Reserva> reservasActivas = reservaRepository.findAll().stream()
                    .filter(r -> r.getHabitacion() != null && 
                           r.getHabitacion().getId().equals(habitacionId) &&
                           (r.getEstadoReserva().equals("ACTIVA") || r.getEstadoReserva().equals("PENDIENTE")) &&
                           r.getFechaInicio() != null &&
                           r.getFechaFin() != null &&
                           !r.getFechaInicio().isAfter(hoy) &&
                           !r.getFechaFin().isBefore(hoy))
                    .toList();
            
            // Si tiene reservas activas hoy, no está disponible
            if (!reservasActivas.isEmpty()) {
                return false;
            }
        }
        
        // Si está marcada como disponible y no tiene reservas activas, está disponible
        return "DISPONIBLE".equalsIgnoreCase(habitacion.getEstado());
    }

    public List<Habitacion> obtenerHabitacionesDisponiblesParaCliente(Long clienteId) {
        if (clienteId == null || reservaRepository == null) {
            return obtenerHabitacionesDisponibles();
        }
        
        List<Habitacion> todasDisponibles = obtenerHabitacionesDisponibles();
        List<Long> habitacionesReservadas = reservaRepository.findHabitacionesReservadasPorCliente(clienteId);
        
        return todasDisponibles.stream()
                .filter(habitacion -> !habitacionesReservadas.contains(habitacion.getId()))
                .toList();
    }

    @Transactional
    public void eliminarHabitacion(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la habitación no puede ser nulo");
        }
        Optional<Habitacion> habitacionOptional = habitacionRepository.findById(id);
        if (habitacionOptional.isPresent()) {
            Habitacion habitacion = habitacionOptional.get();
            // eliminar reservas asociadas primero (si el repo está disponible)
            if (reservaRepository != null) {
                var reservas = reservaRepository.findByHabitacion(habitacion);
                if (reservas != null && !reservas.isEmpty()) {
                    reservaRepository.deleteAll(reservas);
                }
            }
            habitacionRepository.deleteById(id);
            if (habitacion.getId() != null) {
                auditoriaService.registrarAccion(
                        "ELIMINACION_HABITACION",
                        "Habitación #" + habitacion.getNumero() + " (ID: " + habitacion.getId() + ") eliminada.",
                        "Habitacion",
                        habitacion.getId()
                );
            }
        } else {
            throw new IllegalArgumentException("Habitación con ID " + id + " no encontrada para eliminar.");
        }
    }
}