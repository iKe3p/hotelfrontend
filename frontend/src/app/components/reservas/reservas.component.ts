import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { ReservaService } from '../../services/reserva.service';
import { HabitacionService } from '../../services/habitacion.service';
import { ClienteService } from '../../services/cliente.service';
import { Reserva } from '../../models/reserva.model';
import { Habitacion } from '../../models/habitacion.model';
import { Cliente } from '../../models/cliente.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-reservas',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent, DatePipe],
  templateUrl: './reservas.component.html',
  styleUrl: './reservas.component.css'
})
export class ReservasComponent implements OnInit {
  reservas: Reserva[] = [];
  habitaciones: Habitacion[] = [];
  clientes: Cliente[] = [];
  isAdmin = false;
  isRecepcionista = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private reservaService: ReservaService,
    private habitacionService: HabitacionService,
    private clienteService: ClienteService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.isAdmin = this.authService.hasRole('ROLE_ADMIN');
    this.isRecepcionista = this.authService.hasRole('ROLE_RECEPCIONISTA');
    this.cargarReservas();
    this.cargarHabitaciones();
    this.cargarClientes();
  }

  cargarReservas() {
    this.reservaService.obtenerTodas().subscribe({
      next: (data) => {
        this.reservas = data;
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar reservas';
      }
    });
  }

  cargarHabitaciones() {
    this.habitacionService.obtenerTodas().subscribe({
      next: (data) => {
        this.habitaciones = data;
      }
    });
  }

  cargarClientes() {
    this.clienteService.obtenerTodos().subscribe({
      next: (data) => {
        this.clientes = data;
      }
    });
  }

  cancelarReserva(id: number | undefined) {
    if (!id) return;
    
    this.reservaService.cancelarReserva(id).subscribe({
      next: () => {
        this.successMessage = 'Reserva cancelada correctamente';
        this.cargarReservas();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        this.errorMessage = 'Error al cancelar reserva';
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }

  finalizarReserva(id: number | undefined) {
    if (!id) return;
    
    this.reservaService.finalizarReserva(id).subscribe({
      next: () => {
        this.successMessage = 'Reserva finalizada correctamente';
        this.cargarReservas();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        this.errorMessage = 'Error al finalizar reserva';
        setTimeout(() => this.errorMessage = '', 3000);
      }
    });
  }
}

