package com.gestion.hotelera.service;

import com.gestion.hotelera.exception.ClienteConReservasActivasException;
import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.repository.ClienteRepository;
import com.gestion.hotelera.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final AuditoriaService auditoriaService;
    private final ReservaRepository reservaRepository;

    @Autowired
    public ClienteService(ClienteRepository clienteRepository, AuditoriaService auditoriaService, ReservaRepository reservaRepository) {
        this.clienteRepository = clienteRepository;
        this.auditoriaService = auditoriaService;
        this.reservaRepository = reservaRepository;
    }

    // Constructor de compatibilidad para tests existentes
    public ClienteService(ClienteRepository clienteRepository, AuditoriaService auditoriaService) {
        this.clienteRepository = clienteRepository;
        this.auditoriaService = auditoriaService;
        this.reservaRepository = null;
    }

    @Transactional
    public Cliente guardar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente crearCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        if (cliente.getDni() == null || cliente.getDni().trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI no puede estar vacío");
        }
        if (cliente.getNombres() == null || cliente.getNombres().trim().isEmpty()) {
            throw new IllegalArgumentException("Los nombres no pueden estar vacíos");
        }
        if (cliente.getApellidos() == null || cliente.getApellidos().trim().isEmpty()) {
            throw new IllegalArgumentException("Los apellidos no pueden estar vacíos");
        }
        
        try {
            if (clienteRepository.findByDni(cliente.getDni()).isPresent()) {
                throw new IllegalArgumentException("Ya existe un cliente con el DNI '" + cliente.getDni() + "'.");
            }
            if (cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty()
                    && clienteRepository.findByEmail(cliente.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Ya existe un cliente con el email '" + cliente.getEmail() + "'.");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al validar datos del cliente: " + e.getMessage(), e);
        }

        Cliente nuevoCliente = clienteRepository.save(cliente);

        if (nuevoCliente.getId() != null) {
            auditoriaService.registrarAccion(
                    "CREACION_CLIENTE",
                    "Nuevo cliente registrado: " + nuevoCliente.getNombres() + " " + nuevoCliente.getApellidos() +
                            " (DNI: " + nuevoCliente.getDni() + ")",
                    "Cliente",
                    nuevoCliente.getId());
        }

        return nuevoCliente;
    }

    @Transactional
    public Cliente actualizarCliente(Cliente clienteActualizado) {
        if (clienteActualizado.getId() == null) {
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo para actualizar.");
        }
        
        return clienteRepository.findById(clienteActualizado.getId())
                .map(clienteExistente -> {
                    if (!clienteExistente.getDni().equals(clienteActualizado.getDni())
                            && clienteRepository.findByDni(clienteActualizado.getDni()).isPresent()) {
                        throw new IllegalArgumentException(
                                "El DNI '" + clienteActualizado.getDni() + "' ya está en uso por otro cliente.");
                    }
                    if (clienteActualizado.getEmail() != null && !clienteActualizado.getEmail().isEmpty()
                            && !clienteActualizado.getEmail().equals(clienteExistente.getEmail())
                            && clienteRepository.findByEmail(clienteActualizado.getEmail()).isPresent()) {
                        throw new IllegalArgumentException(
                                "El Email '" + clienteActualizado.getEmail() + "' ya está en uso por otro cliente.");
                    }

                    clienteExistente.setNombres(clienteActualizado.getNombres());
                    clienteExistente.setApellidos(clienteActualizado.getApellidos());
                    clienteExistente.setDni(clienteActualizado.getDni());
                    clienteExistente.setNacionalidad(clienteActualizado.getNacionalidad());
                    clienteExistente.setEmail(clienteActualizado.getEmail());
                    clienteExistente.setTelefono(clienteActualizado.getTelefono());

                    Cliente clienteGuardado = clienteRepository.save(clienteExistente);

                    if (clienteGuardado.getId() != null) {
                        auditoriaService.registrarAccion(
                                "ACTUALIZACION_CLIENTE",
                                "Cliente '" + clienteGuardado.getNombres() + " " + clienteGuardado.getApellidos()
                                        + "' (ID: " + clienteGuardado.getId() + ") actualizado.",
                                "Cliente",
                                clienteGuardado.getId());
                    }

                    return clienteGuardado;
                })
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente con ID " + clienteActualizado.getId() + " no encontrado para actualizar."));
    }

    public Optional<Cliente> buscarClientePorDni(String dni) {
        return clienteRepository.findByDni(dni);
    }

    public Optional<Cliente> buscarPorDniOptional(String dni) {
        return clienteRepository.findByDni(dni);
    }

    public boolean existeClientePorDni(String dni) {
        return clienteRepository.findByDni(dni).isPresent();
    }

    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarClientePorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return clienteRepository.findById(id);
    }

    public Optional<Cliente> obtenerClientePorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return clienteRepository.findById(id);
    }

    public long contarClientes() {
        return clienteRepository.count();
    }

    @Transactional
    public boolean eliminarClientePorId(Long id) {
        if (id == null) {
            return false;
        }
        Optional<Cliente> clienteOptional = clienteRepository.findById(id);
        if (clienteOptional.isPresent()) {
            Cliente cliente = clienteOptional.get();
            // eliminar reservas asociadas primero (si el repo está disponible)
            if (reservaRepository != null) {
                var reservas = reservaRepository.findByCliente(cliente);
                var estadosBloqueo = List.of("ACTIVA", "PENDIENTE");
                List<ClienteConReservasActivasException.ReservaActivaResumen> reservasActivas = reservas.stream()
                        .filter(reserva -> reserva.getEstadoReserva() != null
                                && estadosBloqueo.contains(reserva.getEstadoReserva().toUpperCase(Locale.ROOT)))
                        .map(this::mapearResumenReservaActiva)
                        .collect(Collectors.toList());

                if (!reservasActivas.isEmpty()) {
                    throw new ClienteConReservasActivasException(cliente.getId(), reservasActivas);
                }

                if (!reservas.isEmpty()) {
                    reservaRepository.deleteAll(reservas);
                }
            }
            clienteRepository.deleteById(id);
            if (cliente.getId() != null) {
                auditoriaService.registrarAccion(
                        "ELIMINACION_CLIENTE",
                        "Cliente '" + cliente.getNombres() + " " + cliente.getApellidos() +
                                "' (ID: " + cliente.getId() + ") eliminado.",
                        "Cliente",
                        cliente.getId());
            }
            return true;
        }
        return false;
    }

    private ClienteConReservasActivasException.ReservaActivaResumen mapearResumenReservaActiva(Reserva reserva) {
        String habitacion = reserva.getHabitacion() != null ? reserva.getHabitacion().getNumero() : "Sin asignar";
        return new ClienteConReservasActivasException.ReservaActivaResumen(
                reserva.getId(),
                habitacion,
                reserva.getEstadoReserva(),
                reserva.getFechaInicio(),
                reserva.getFechaFin());
    }

    public Page<Cliente> findAllClientes(Pageable pageable) {
        if (pageable == null) {
            pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        }
        return clienteRepository.findAll(pageable);
    }

    public Page<Cliente> searchClientes(String searchTerm, Pageable pageable) {
        if (pageable == null) {
            pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        }
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return clienteRepository.findAll(pageable);
        }
        return clienteRepository
                .findByDniContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
                        searchTerm, searchTerm, searchTerm, pageable);
    }

    public Page<Cliente> obtenerClientesPaginados(Pageable pageable, String search) {
        return searchClientes(search, pageable);
    }

    public Cliente obtenerPorEmail(String email) {
        return clienteRepository.findByEmail(email).orElse(null);
    }

    public Cliente obtenerPorUsername(String username) {
        return clienteRepository.findByUsuarioUsername(username).orElse(null);
    }
}