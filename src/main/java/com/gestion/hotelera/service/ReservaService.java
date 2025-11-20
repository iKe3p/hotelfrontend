package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.model.Servicio;
import com.gestion.hotelera.repository.ReservaRepository;
import com.gestion.hotelera.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final AuditoriaService auditoriaService;
    private final ServicioRepository servicioRepository;
    private final HabitacionService habitacionService;

    @Autowired
    public ReservaService(ReservaRepository reservaRepository,
                          AuditoriaService auditoriaService,
                          ServicioRepository servicioRepository,
                          HabitacionService habitacionService) {
        this.reservaRepository = reservaRepository;
        this.auditoriaService = auditoriaService;
        this.servicioRepository = servicioRepository;
        this.habitacionService = habitacionService;
    }

    // Constructor alternativo para tests
    public ReservaService(ReservaRepository reservaRepository, AuditoriaService auditoriaService) {
        this(reservaRepository, auditoriaService, null, null);
    }

    @Transactional
    public Reserva crearOActualizarReserva(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }
        if (reserva.getCliente() == null) {
            throw new IllegalArgumentException("La reserva debe tener un cliente asignado");
        }
        if (reserva.getHabitacion() == null) {
            throw new IllegalArgumentException("La reserva debe tener una habitación asignada");
        }
        if (reserva.getFechaInicio() == null || reserva.getFechaFin() == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }
        
        // Validar que las fechas sean correctas
        if (reserva.getFechaInicio().isAfter(reserva.getFechaFin()) || reserva.getFechaInicio().equals(reserva.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
        
        Long habitacionId = reserva.getHabitacion().getId();
        Long reservaId = reserva.getId(); // Puede ser null si es nueva reserva
        
        // Validación 1: Verificar que la habitación exista y esté disponible
        if (habitacionService != null) {
            var habitacionOpt = habitacionService.buscarHabitacionPorId(habitacionId);
            if (habitacionOpt.isEmpty()) {
                throw new IllegalArgumentException("La habitación seleccionada no existe");
            }
            
            var habitacion = habitacionOpt.get();
            // Si la habitación está en mantenimiento, no se puede reservar
            if ("MANTENIMIENTO".equalsIgnoreCase(habitacion.getEstado())) {
                throw new IllegalArgumentException("La habitación está en mantenimiento y no puede ser reservada");
            }
        }
        
        // Validación 2: Verificar disponibilidad en tiempo real - validar conflictos de fechas
        // Usar el método del servicio de habitaciones que también considera el estado
        if (habitacionService != null && !habitacionService.estaDisponible(
                habitacionId, reserva.getFechaInicio(), reserva.getFechaFin())) {
            // Verificar si el conflicto es por reservas activas o por estado
            boolean existeConflicto = reservaRepository.existeReservaEnRangoFechas(
                habitacionId,
                reserva.getFechaInicio(),
                reserva.getFechaFin(),
                reservaId // Excluir la reserva actual si se está actualizando
            );
            
            if (existeConflicto) {
                List<Reserva> reservasConflictivas = reservaRepository.findReservasConflictivas(
                    habitacionId,
                    reserva.getFechaInicio(),
                    reserva.getFechaFin(),
                    reservaId
                );
                
                StringBuilder mensaje = new StringBuilder("La habitación ya está reservada en el rango de fechas seleccionado. ");
                if (!reservasConflictivas.isEmpty()) {
                    Reserva conflicto = reservasConflictivas.get(0);
                    mensaje.append("Ya existe una reserva del ").append(conflicto.getFechaInicio())
                           .append(" al ").append(conflicto.getFechaFin())
                           .append(" con estado: ").append(conflicto.getEstadoReserva());
                }
                throw new IllegalArgumentException(mensaje.toString());
            } else {
                // El conflicto es por estado de la habitación (mantenimiento, etc.)
                var habitacionOpt = habitacionService.buscarHabitacionPorId(habitacionId);
                if (habitacionOpt.isPresent()) {
                    String estado = habitacionOpt.get().getEstado();
                    if ("MANTENIMIENTO".equalsIgnoreCase(estado)) {
                        throw new IllegalArgumentException("La habitación está en mantenimiento y no puede ser reservada");
                    } else if ("OCUPADA".equalsIgnoreCase(estado)) {
                        throw new IllegalArgumentException("La habitación está ocupada y no está disponible en el rango de fechas seleccionado");
                    }
                }
                throw new IllegalArgumentException("La habitación no está disponible en el rango de fechas seleccionado");
            }
        }
        
        try {
            Reserva guardada = reservaRepository.save(reserva);
            
            // Actualizar estado de habitación según el estado de la reserva y las fechas
            if (habitacionService != null && guardada.getHabitacion() != null) {
                Long habitacionIdGuardada = guardada.getHabitacion().getId();
                LocalDate hoy = LocalDate.now();
                
                if ("ACTIVA".equalsIgnoreCase(guardada.getEstadoReserva()) || 
                    "PENDIENTE".equalsIgnoreCase(guardada.getEstadoReserva())) {
                    // Si la reserva es activa o pendiente y la fecha de inicio es hoy o pasada, marcar como ocupada
                    if (guardada.getFechaInicio() != null && !guardada.getFechaInicio().isAfter(hoy)) {
                        // La reserva ya está en curso o inicia hoy - marcar habitación como ocupada
                        habitacionService.actualizarEstadoHabitacion(habitacionIdGuardada, "OCUPADA");
                    } else if (guardada.getFechaInicio() != null && guardada.getFechaInicio().isAfter(hoy)) {
                        // La reserva es futura - mantener disponible pero la validación de fechas la protegerá
                        // La tarea programada se encargará de marcarla como ocupada cuando llegue la fecha
                        // Solo actualizar si realmente no está en mantenimiento
                        var habitacionOpt = habitacionService.buscarHabitacionPorId(habitacionIdGuardada);
                        if (habitacionOpt.isPresent() && !"MANTENIMIENTO".equalsIgnoreCase(habitacionOpt.get().getEstado())) {
                            habitacionService.actualizarEstadoHabitacion(habitacionIdGuardada, "DISPONIBLE");
                        }
                    }
                } else if ("FINALIZADA".equalsIgnoreCase(guardada.getEstadoReserva()) ||
                           "CANCELADA".equalsIgnoreCase(guardada.getEstadoReserva())) {
                    // Si la reserva está finalizada o cancelada, liberar la habitación
                    habitacionService.actualizarEstadoHabitacion(habitacionIdGuardada, "DISPONIBLE");
                }
            }
            
            reservaId = guardada.getId();
            if (reservaId != null && guardada.getCliente() != null) {
                auditoriaService.registrarAccion("CREACION_O_ACTUALIZACION_RESERVA",
                        "Reserva creada o actualizada (ID: " + reservaId + ") para cliente " + guardada.getCliente().getNombres(),
                        "Reserva", reservaId);
            }
            return guardada;
        } catch (IllegalArgumentException e) {
            // Re-lanzar excepciones de validación
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar la reserva: " + e.getMessage(), e);
        }
    }

    public List<Reserva> obtenerTodasLasReservas() {
        return reservaRepository.findAll();
    }

    public Optional<Reserva> buscarReservaPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return reservaRepository.findById(id);
    }

    public List<Reserva> obtenerReservasPorCliente(Cliente cliente) {
        return reservaRepository.findByCliente(cliente);
    }

    public List<Reserva> obtenerReservasPorClienteId(Long clienteId) {
        if (clienteId == null) {
            return new ArrayList<>();
        }
        List<Reserva> todas = reservaRepository.findAll();
        return todas.stream()
                .filter(r -> r.getCliente() != null && Objects.equals(r.getCliente().getId(), clienteId))
                .collect(java.util.stream.Collectors.toList());
    }

    public long contarReservas() {
        long count = reservaRepository.count();
        System.out.println("=== CONTEO DE RESERVAS ===");
        System.out.println("Total reservas en BD: " + count);
        return count;
    }

    @Transactional
    public boolean cancelarReserva(Long id, String userRole) {
        if (id == null) {
            return false;
        }
        Optional<Reserva> opt = reservaRepository.findById(id);
        if (opt.isPresent()) {
            Reserva reserva = opt.get();
            
            // Los clientes no pueden cancelar sus propias reservas
            if ("ROLE_CLIENTE".equals(userRole)) {
                throw new IllegalStateException("Usted no puede cancelar su reserva. Acérquese a recepción para generar su cancelación de reserva.");
            }
            
            // Verificar si la reserva tiene pago - no se puede cancelar si ya está pagada
            if (reserva.getPago() != null) {
                throw new IllegalStateException("No se puede cancelar una reserva que ya tiene pago. Use 'Finalizar' en su lugar.");
            }
            
            // Cambiar estado a CANCELADA y guardar con flush para asegurar persistencia
            reserva.setEstadoReserva("CANCELADA");
            Reserva reservaCancelada = reservaRepository.saveAndFlush(reserva);
            
            // Verificar que el estado se guardó correctamente
            if (!"CANCELADA".equalsIgnoreCase(reservaCancelada.getEstadoReserva())) {
                System.out.println("ERROR: El estado no se guardó correctamente");
                // Intentar nuevamente
                reservaCancelada.setEstadoReserva("CANCELADA");
                reservaRepository.saveAndFlush(reservaCancelada);
            }
            
            // Liberar habitación cuando se cancela la reserva
            if (habitacionService != null && reservaCancelada.getHabitacion() != null) {
                habitacionService.actualizarEstadoHabitacion(reservaCancelada.getHabitacion().getId(), "DISPONIBLE");
            }
            
            Long reservaId = reservaCancelada.getId();
            if (reservaId != null) {
                auditoriaService.registrarAccion("CANCELACION_RESERVA",
                        "Reserva cancelada por " + userRole + " (ID: " + reservaId + ").", "Reserva", reservaId);
            }
            
            
            System.out.println("=== RESERVA CANCELADA ===");
            System.out.println("Reserva ID: " + reservaId);
            System.out.println("Estado: " + reservaCancelada.getEstadoReserva());
            
            return true;
        }
        return false;
    }

    // Método de compatibilidad para tests existentes
    @Transactional
    public boolean cancelarReserva(Long id) {
        return cancelarReserva(id, "ROLE_ADMIN");
    }

    @Transactional
    public boolean eliminarReservaFisica(Long id) {
        if (id == null) {
            return false;
        }
        Optional<Reserva> opt = reservaRepository.findById(id);
        if (opt.isPresent()) {
            reservaRepository.deleteById(id);
            auditoriaService.registrarAccion("ELIMINACION_RESERVA",
                    "Reserva (ID: " + id + ") eliminada físicamente.", "Reserva", id);
            return true;
        }
        return false;
    }

    @Transactional
    public void finalizarReserva(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la reserva no puede ser nulo");
        }
        Optional<Reserva> opt = reservaRepository.findById(id);
        if (opt.isPresent()) {
            Reserva reserva = opt.get();
            
            // Solo finalizar si no está ya finalizada
            if ("FINALIZADA".equalsIgnoreCase(reserva.getEstadoReserva())) {
                return;
            }
            
            String estadoAnterior = reserva.getEstadoReserva();
            reserva.setEstadoReserva("FINALIZADA");
            
            // Registrar la fecha de salida real si no está establecida
            if (reserva.getFechaSalidaReal() == null) {
                reserva.setFechaSalidaReal(LocalDate.now());
            }
            
            reservaRepository.save(reserva);
            
            // Liberar habitación
            if (habitacionService != null && reserva.getHabitacion() != null) {
                habitacionService.actualizarEstadoHabitacion(reserva.getHabitacion().getId(), "DISPONIBLE");
            }
            
            auditoriaService.registrarAccion("FINALIZACION_RESERVA",
                    "Reserva finalizada (ID: " + reserva.getId() + ") - Estado anterior: " + estadoAnterior, 
                    "Reserva", reserva.getId());
        }
    }

    public Integer calcularDiasEstadia(LocalDate inicio, LocalDate fin) {
        long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fin);
        // Si es el mismo día, contar como 1 día mínimo
        return dias == 0 ? 1 : (int) dias;
    }

    public Double calcularTotalPagar(Double precioPorNoche, Integer dias) {
        return precioPorNoche * dias;
    }

    public double calcularIngresosTotales() {
        List<Reserva> reservas = reservaRepository.findAll();
        return reservas.stream()
                .filter(r -> "FINALIZADA".equalsIgnoreCase(r.getEstadoReserva()))
                .mapToDouble(Reserva::getTotalPagar)
                .sum();
    }

    public List<Map<String, Object>> getIngresosPorPeriodo(LocalDate inicio, LocalDate fin) {
        List<Reserva> reservas = reservaRepository.findAll();
        Map<LocalDate, Double> ingresosPorFecha = new HashMap<>();
        
        // Inicializar todas las fechas del período con 0
        LocalDate fecha = inicio;
        while (!fecha.isAfter(fin)) {
            ingresosPorFecha.put(fecha, 0.0);
            fecha = fecha.plusDays(1);
        }
        
        // Sumar ingresos por fecha
        for (Reserva r : reservas) {
            if (r.getFechaInicio() != null && !r.getFechaInicio().isBefore(inicio) && !r.getFechaInicio().isAfter(fin)) {
                if ("FINALIZADA".equalsIgnoreCase(r.getEstadoReserva()) || "ACTIVA".equalsIgnoreCase(r.getEstadoReserva())) {
                    double total = r.getTotalPagar() != null ? r.getTotalPagar() : 0.0;
                    total += r.calcularTotalServicios();
                    ingresosPorFecha.merge(r.getFechaInicio(), total, (a, b) -> (a != null ? a : 0.0) + (b != null ? b : 0.0));
                }
            }
        }
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map.Entry<LocalDate, Double> entry : ingresosPorFecha.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("fecha", entry.getKey().toString());
            map.put("ingresos", entry.getValue());
            resultado.add(map);
        }
        
        resultado.sort((a, b) -> ((String) a.get("fecha")).compareTo((String) b.get("fecha")));
        return resultado;
    }

    public List<Map<String, Object>> getMovimientoPorPeriodo(LocalDate inicio, LocalDate fin) {
        List<Reserva> reservas = reservaRepository.findAll();
        Map<LocalDate, Map<String, Integer>> movimientosPorFecha = new HashMap<>();
        
        // Inicializar todas las fechas del período
        LocalDate fecha = inicio;
        while (!fecha.isAfter(fin)) {
            Map<String, Integer> movimientos = new HashMap<>();
            movimientos.put("checkIns", 0);
            movimientos.put("checkOuts", 0);
            movimientosPorFecha.put(fecha, movimientos);
            fecha = fecha.plusDays(1);
        }
        
        // Contar check-ins y check-outs
        for (Reserva r : reservas) {
            // Check-ins
            if (r.getFechaInicio() != null && !r.getFechaInicio().isBefore(inicio) && !r.getFechaInicio().isAfter(fin)) {
                movimientosPorFecha.get(r.getFechaInicio()).merge("checkIns", 1, (a, b) -> (a != null ? a : 0) + (b != null ? b : 0));
            }
            // Check-outs
            if (r.getFechaFin() != null && !r.getFechaFin().isBefore(inicio) && !r.getFechaFin().isAfter(fin)) {
                movimientosPorFecha.get(r.getFechaFin()).merge("checkOuts", 1, (a, b) -> (a != null ? a : 0) + (b != null ? b : 0));
            }
        }
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map.Entry<LocalDate, Map<String, Integer>> entry : movimientosPorFecha.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("fecha", entry.getKey().toString());
            map.put("checkIns", entry.getValue().get("checkIns"));
            map.put("checkOuts", entry.getValue().get("checkOuts"));
            resultado.add(map);
        }
        
        resultado.sort((a, b) -> ((String) a.get("fecha")).compareTo((String) b.get("fecha")));
        return resultado;
    }

    public long contarReservasPorEstado(String estado) {
        return reservaRepository.countByEstadoReservaIgnoreCase(estado);
    }

    public long contarCheckInsHoy() {
        return reservaRepository.countByFechaInicio(LocalDate.now());
    }

    public long contarCheckOutsHoy() {
        return reservaRepository.countByFechaFin(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public Optional<Reserva> obtenerReservaPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return reservaRepository.findById(id);
    }

    @Transactional
    public Reserva asignarServicios(Long reservaId, List<Long> servicioIds, List<String> opciones) {
        if (reservaId == null) {
            throw new IllegalArgumentException("El ID de la reserva no puede ser nulo");
        }
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada para el ID " + reservaId));
        if (servicioRepository == null) {
            throw new IllegalStateException("Repositorio de servicios no disponible en este contexto.");
        }

        List<Long> idsNormalizados = servicioIds != null ? servicioIds : Collections.emptyList();
        Set<Servicio> serviciosSeleccionados = idsNormalizados.isEmpty()
                ? new HashSet<>()
                : new HashSet<>(servicioRepository.findAllById(idsNormalizados));

        reserva.getServicios().clear();
        reserva.getServicios().addAll(serviciosSeleccionados);

        if (opciones != null && !opciones.isEmpty()) {
            int i = 0;
            for (Servicio servicio : serviciosSeleccionados) {
                if (i < opciones.size()) {
                    String opcion = opciones.get(i);
                    if (opcion != null && !opcion.isEmpty()) {
                        reserva.getOpcionesServicios().put(servicio.getNombre(), opcion);
                    }
                    i++;
                }
            }
        }

        Reserva actualizada = reservaRepository.save(reserva);

        auditoriaService.registrarAccion("ASIGNACION_SERVICIOS_RESERVA",
                "Servicios actualizados para la reserva ID: " + reservaId,
                "Reserva",
                reservaId);

        return actualizada;
    }

    public double calcularTotalConServicios(Reserva reserva) {
        double base = reserva.getTotalPagar() != null ? reserva.getTotalPagar() : 0.0;
        double extras = reserva.calcularTotalServicios();
        return base + extras;
    }

    public long contarReservasPorCliente(String username) {
        return reservaRepository.findAll().stream()
                .filter(r -> r.getCliente() != null && 
                        (username.equals(r.getCliente().getDni()) || 
                         (r.getCliente().getUsuario() != null && username.equals(r.getCliente().getUsuario().getUsername()))))
                .count();
    }

    public long contarReservasActivasPorCliente(String username) {
        return reservaRepository.findAll().stream()
                .filter(r -> r.getCliente() != null && 
                        (username.equals(r.getCliente().getDni()) || 
                         (r.getCliente().getUsuario() != null && username.equals(r.getCliente().getUsuario().getUsername()))) &&
                        ("ACTIVA".equalsIgnoreCase(r.getEstadoReserva()) || "PENDIENTE".equalsIgnoreCase(r.getEstadoReserva())))
                .count();
    }
    
    public long contarReservasFinalizadasPorCliente(String username) {
        return reservaRepository.findAll().stream()
                .filter(r -> r.getCliente() != null && 
                        (username.equals(r.getCliente().getDni()) || 
                         (r.getCliente().getUsuario() != null && username.equals(r.getCliente().getUsuario().getUsername()))) &&
                        "FINALIZADA".equalsIgnoreCase(r.getEstadoReserva()))
                .count();
    }
}
