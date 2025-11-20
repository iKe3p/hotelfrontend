import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { ReservaService } from '../../services/reserva.service';
import { PagoService } from '../../services/pago';
import { Reserva } from '../../models/reserva.model';
import { PagoRequest, PagoResponse } from '../../models/pago.model';

@Component({
  selector: 'app-pago-reserva',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './pago-reserva.html',
  styleUrl: './pago-reserva.scss',
})
export class PagoReserva implements OnInit {
  reserva: Reserva | null = null;
  reservaId: number | null = null;
  pagoRequest: PagoRequest = {
    reservaId: 0,
    metodoPago: 'EFECTIVO'
  };
  metodoPago = 'EFECTIVO';
  errorMessage = '';
  successMessage = '';
  pagoCompletado = false;
  pagoResponse: PagoResponse | null = null;

  constructor(
    private reservaService: ReservaService,
    private pagoService: PagoService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.reservaId = Number(params['id']);
      if (this.reservaId) {
        this.cargarReserva();
        this.verificarPagoExistente();
      }
    });
  }

  cargarReserva() {
    if (!this.reservaId) return;
    
    this.reservaService.buscarPorId(this.reservaId).subscribe({
      next: (data) => {
        this.reserva = data;
        this.pagoRequest.reservaId = this.reservaId!;
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar reserva';
        console.error(err);
      }
    });
  }

  verificarPagoExistente() {
    if (!this.reservaId) return;
    
    this.pagoService.obtenerPagoPorReserva(this.reservaId).subscribe({
      next: (data) => {
        this.pagoCompletado = true;
        this.pagoResponse = data;
      },
      error: (err) => {
        // No hay pago previo, continuar normalmente
      }
    });
  }

  onMetodoPagoChange() {
    this.pagoRequest.metodoPago = this.metodoPago;
    this.pagoRequest.metodo = this.metodoPago;
    
    // Limpiar campos específicos según el método
    if (this.metodoPago === 'EFECTIVO') {
      delete this.pagoRequest.numeroTarjeta;
      delete this.pagoRequest.numeroTelefono;
    }
  }

  procesarPago() {
    if (!this.reservaId) {
      this.errorMessage = 'ID de reserva inválido';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    this.pagoRequest.reservaId = this.reservaId;
    this.pagoRequest.metodoPago = this.metodoPago;
    this.pagoRequest.metodo = this.metodoPago;

    // Configurar campos según el método de pago
    if (this.metodoPago === 'TARJETA') {
      if (!this.pagoRequest.numeroTarjeta || !this.pagoRequest.fechaExpiracion || !this.pagoRequest.cvv) {
        this.errorMessage = 'Complete todos los campos de la tarjeta';
        return;
      }
    } else if (this.metodoPago === 'YAPE' || this.metodoPago === 'PLIN') {
      if (!this.pagoRequest.numeroTelefono) {
        this.errorMessage = 'Ingrese el número de teléfono';
        return;
      }
      this.pagoRequest.telefonoWallet = this.pagoRequest.numeroTelefono;
    }

    this.pagoService.procesarPago(this.pagoRequest).subscribe({
      next: (data) => {
        this.pagoCompletado = true;
        this.pagoResponse = data;
        this.successMessage = data.mensaje || 'Pago procesado exitosamente';
        setTimeout(() => {
          this.router.navigate(['/reservas']);
        }, 2000);
      },
      error: (err) => {
        this.errorMessage = err.error || 'Error al procesar el pago';
        console.error(err);
      }
    });
  }

  calcularTotal(): number {
    if (!this.reserva) return 0;
    const totalHabitacion = this.reserva.totalPagar || 0;
    const totalServicios = this.reserva.servicios?.reduce((sum, s) => sum + (s.precio || 0), 0) || 0;
    return totalHabitacion + totalServicios;
  }
}
