package com.gestion.hotelera.service;

import com.gestion.hotelera.dto.PagoRequest;
import com.gestion.hotelera.dto.PagoResponse;
import com.gestion.hotelera.model.Pago;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.repository.PagoRepository;
import com.gestion.hotelera.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ReservaService reservaService;
    private final ReservaRepository reservaRepository;

    public PagoService(PagoRepository pagoRepository, ReservaService reservaService, ReservaRepository reservaRepository) {
        this.pagoRepository = pagoRepository;
        this.reservaService = reservaService;
        this.reservaRepository = reservaRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public PagoResponse procesarPago(PagoRequest pagoRequest) {
        if (pagoRequest == null) {
            System.out.println("ERROR: PagoRequest es null");
            throw new IllegalArgumentException("Datos de pago inválidos");
        }
        
        System.out.println("=== INICIANDO PROCESAMIENTO PAGO ===");
        System.out.println("Reserva ID: " + pagoRequest.getReservaId());
        System.out.println("Método de pago: " + pagoRequest.getMetodoPago());
        System.out.println("Método: " + pagoRequest.getMetodo());
        
        // Validar datos básicos del pago
        if (pagoRequest.getReservaId() == null || pagoRequest.getReservaId() <= 0) {
            throw new IllegalArgumentException("ID de reserva inválido");
        }
        
        // Normalizar el método de pago
        String metodo = pagoRequest.getMetodo() != null && !pagoRequest.getMetodo().trim().isEmpty() ? 
                       pagoRequest.getMetodo() : 
                       (pagoRequest.getMetodoPago() != null && !pagoRequest.getMetodoPago().trim().isEmpty() ? 
                        pagoRequest.getMetodoPago() : "TARJETA");
        
        // Validar que el método no esté vacío
        if (metodo == null || metodo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe especificar un método de pago");
        }
        
        // Actualizar ambos campos para mantener consistencia
        pagoRequest.setMetodo(metodo);
        pagoRequest.setMetodoPago(metodo);
        
        // Obtener la reserva
        Reserva reserva = reservaService.obtenerReservaPorId(pagoRequest.getReservaId())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        
        // Verificar si ya existe un pago para esta reserva
        Optional<Pago> pagoExistente = pagoRepository.findByReservaId(reserva.getId());
        if (pagoExistente.isPresent()) {
            System.out.println("INFO: Ya existe un pago para esta reserva. ID: " + pagoExistente.get().getId());
            PagoResponse response = new PagoResponse(true, "El pago ya fue procesado anteriormente.");
            response.setReferencia(pagoExistente.get().getReferencia());
            return response;
        }
        
        // Crear el pago con todos los campos requeridos
        double montoBase = reserva.getTotalPagar() != null ? reserva.getTotalPagar() : 0.0;
        double montoServicios = reserva.calcularTotalServicios();
        double montoTotal = montoBase + montoServicios;
        
        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setMontoBase(montoBase);
        pago.setMontoServicios(montoServicios);
        pago.setMontoTotal(montoTotal);
        pago.setMetodo(metodo);
        pago.setEstado("COMPLETADO");
        pago.setReferencia("REF-" + System.currentTimeMillis() + "-" + reserva.getId());
        pago.setFechaPago(LocalDateTime.now());
        pago.setCanal(pagoRequest.getCanal() != null && !pagoRequest.getCanal().trim().isEmpty() ? 
                     pagoRequest.getCanal() : "WEB");
        
        System.out.println("=== CREANDO PAGO ===");
        System.out.println("Reserva ID: " + reserva.getId());
        System.out.println("Monto Base: " + montoBase);
        System.out.println("Monto Servicios: " + montoServicios);
        System.out.println("Monto Total: " + montoTotal);
        System.out.println("Método: " + metodo);
        
        // Guardar pago
        Pago pagoGuardado = pagoRepository.save(pago);
        
        if (pagoGuardado.getId() == null) {
            throw new RuntimeException("Error al guardar el pago: el ID no se generó correctamente");
        }
        
        System.out.println("=== PAGO GUARDADO ===");
        System.out.println("Pago ID: " + pagoGuardado.getId());
        System.out.println("Referencia: " + pagoGuardado.getReferencia());
        
        // Actualizar la reserva con el pago y estado ACTIVA
        // Usar el repositorio directamente para evitar validaciones complejas que pueden causar rollback
        reserva.setPago(pagoGuardado);
        reserva.setEstadoReserva("ACTIVA");
        
        // Guardar reserva directamente usando el repositorio
        // La reserva ya existe y ya fue validada al crearse, solo actualizamos pago y estado
        reservaRepository.save(reserva);
        System.out.println("Reserva actualizada correctamente con el pago");
        
        System.out.println("=== PAGO PROCESADO EXITOSAMENTE ===");
        System.out.println("Pago ID: " + pagoGuardado.getId());
        System.out.println("Referencia: " + pagoGuardado.getReferencia());
        
        PagoResponse response = new PagoResponse(true, "Pago procesado exitosamente.");
        response.setReferencia(pagoGuardado.getReferencia());
        return response;
    }

    public Optional<Pago> obtenerPagoPorReserva(Long reservaId) {
        return pagoRepository.findByReservaId(reservaId);
    }
}
