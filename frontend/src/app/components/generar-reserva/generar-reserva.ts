import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { ReservaService } from '../../services/reserva.service';
import { HabitacionService } from '../../services/habitacion.service';
import { ClienteService } from '../../services/cliente.service';
import { Reserva } from '../../models/reserva.model';
import { Habitacion } from '../../models/habitacion.model';
import { Cliente } from '../../models/cliente.model';

@Component({
  selector: 'app-generar-reserva',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './generar-reserva.html',
  styleUrl: './generar-reserva.scss',
})
export class GenerarReserva implements OnInit {
  reserva: Reserva = {
    fechaInicio: '',
    fechaFin: '',
    horaEntrada: '14:00',
    horaSalida: '12:00',
    diasEstadia: 0,
    totalPagar: 0,
    estadoReserva: 'PENDIENTE'
  };

  habitacionesDisponibles: Habitacion[] = [];
  habitacionSeleccionadaId: number | null = null;
  cliente: Cliente | null = null;
  dniBuscar: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  diasEstadia: number = 0;
  totalPagar: number = 0;

  constructor(
    private reservaService: ReservaService,
    private habitacionService: HabitacionService,
    private clienteService: ClienteService,
    private router: Router,
    private route: ActivatedRoute,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.cargarHabitaciones();
    this.route.queryParams.subscribe(params => {
      if (params['dni']) {
        this.dniBuscar = params['dni'];
        this.buscarCliente();
      }
      if (params['idCliente']) {
        this.cargarClientePorId(Number(params['idCliente']));
      }
    });
  }

  cargarHabitaciones() {
    this.habitacionService.obtenerDisponibles().subscribe({
      next: (data) => {
        this.habitacionesDisponibles = data;
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar habitaciones';
        console.error(err);
      }
    });
  }

  buscarCliente() {
    if (!this.dniBuscar || !/^\d{8}$/.test(this.dniBuscar)) {
      this.errorMessage = 'El DNI debe contener exactamente 8 dígitos numéricos';
      return;
    }

    this.clienteService.buscarPorDni(this.dniBuscar).subscribe({
      next: (data) => {
        this.cliente = data;
        this.reserva.cliente = data;
        this.errorMessage = '';
      },
      error: (err) => {
        this.errorMessage = 'Cliente no encontrado. Por favor, regístrelo primero.';
        this.cliente = null;
      }
    });
  }

  cargarClientePorId(id: number) {
    // This would need a method in ClienteService to get by ID
    // For now, we'll use the DNI search
  }

  calcularCosto() {
  if (!this.habitacionSeleccionadaId || !this.reserva.fechaInicio || !this.reserva.fechaFin) {
    return;
  }

  this.reservaService.calcularCosto(
    this.habitacionSeleccionadaId,
    this.reserva.fechaInicio,
    this.reserva.fechaFin
  ).subscribe({
    next: (data) => {
      if (data.error) {
        this.errorMessage = data.error;
        this.diasEstadia = 0;
        this.totalPagar = 0;
        this.reserva.diasEstadia = 0;
        this.reserva.totalPagar = 0;
      } else {
        this.errorMessage = '';
        this.diasEstadia = data.dias;
        this.totalPagar = data.total;
        this.reserva.diasEstadia = data.dias;
        this.reserva.totalPagar = data.total;
      }
    },
    error: (err) => {
      this.errorMessage = 'Error al calcular el costo';
      this.diasEstadia = 0;
      this.totalPagar = 0;
      this.reserva.diasEstadia = 0;
      this.reserva.totalPagar = 0;
      console.error(err);
    }
  });
}

  onHabitacionChange() {
    const habitacion = this.habitacionesDisponibles.find(h => h.id === this.habitacionSeleccionadaId);
    if (habitacion) {
      this.reserva.habitacion = habitacion;
      this.calcularCosto();
    }
  }

  onFechaChange() {
    if (this.reserva.fechaInicio && this.reserva.fechaFin) {
      this.calcularCosto();
    }
  }

  guardarReserva() {
    if (!this.reserva.cliente) {
      this.errorMessage = 'Debe buscar y seleccionar un cliente primero';
      return;
    }

    if (!this.habitacionSeleccionadaId) {
      this.errorMessage = 'Debe seleccionar una habitación';
      return;
    }

    if (!this.reserva.fechaInicio || !this.reserva.fechaFin) {
      this.errorMessage = 'Debe completar las fechas de entrada y salida';
      return;
    }

    // Ensure habitacion is set
    const habitacion = this.habitacionesDisponibles.find(h => h.id === this.habitacionSeleccionadaId);
    if (habitacion) {
      this.reserva.habitacion = habitacion;
    }

    // Construir payload explícito para evitar problemas de deserialización en el backend
    const payload: any = {
      cliente: this.reserva.cliente && this.reserva.cliente.id ? { id: this.reserva.cliente.id } : this.reserva.cliente,
      habitacion: { id: this.habitacionSeleccionadaId },
      fechaInicio: this.reserva.fechaInicio, // formato YYYY-MM-DD
      fechaFin: this.reserva.fechaFin,       // formato YYYY-MM-DD
      // Asegurar formato de hora con segundos para mapear a LocalTime en backend
      horaEntrada: this.reserva.horaEntrada && this.reserva.horaEntrada.length === 5 ? this.reserva.horaEntrada + ':00' : this.reserva.horaEntrada,
      horaSalida: this.reserva.horaSalida && this.reserva.horaSalida.length === 5 ? this.reserva.horaSalida + ':00' : this.reserva.horaSalida,
      diasEstadia: this.reserva.diasEstadia,
      totalPagar: this.reserva.totalPagar,
      estadoReserva: this.reserva.estadoReserva,
      opcionesServicios: this.reserva.opcionesServicios || {}
    };

    this.reservaService.crearReserva(payload).subscribe({
      next: (data: any) => {
        this.successMessage = 'Reserva creada exitosamente.';
        setTimeout(() => {
          this.router.navigate(['/reservas']);
        }, 800);
      },
      error: (err) => {
        this.errorMessage = err.error?.error || 'Error al crear la reserva';
        console.error(err);
      }
    });
  }
}
