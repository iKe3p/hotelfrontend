import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { AuthService } from '../../services/auth.service';
import { DashboardService } from '../../services/dashboard.service';
import { ClienteService } from '../../services/cliente.service';
import { ReservaService } from '../../services/reserva.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent, DatePipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  isAdmin = false;
  isRecepcionista = false;
  isCliente = false;
  user: any = null;

  // Métricas Admin/Recepcionista
  totalHabitaciones = 0;
  totalClientes = 0;
  totalReservas = 0;
  habitacionesDisponibles = 0;
  habitacionesOcupadas = 0;
  habitacionesMantenimiento = 0;
  ingresosTotales = 0;
  checkInsHoy = 0;
  checkOutsHoy = 0;
  reservasPendientes = 0;
  reservasActivas = 0;
  totalEmpleados = 0;

  // Datos Cliente
  cliente: any = null;
  reservasCliente: any[] = [];
  reservasActivasCliente = 0;
  reservasFinalizadasCliente = 0;

  constructor(
    private authService: AuthService,
    private dashboardService: DashboardService,
    private clienteService: ClienteService,
    private reservaService: ReservaService,
    private cdr: ChangeDetectorRef // 2. Inyectar ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.user = this.authService.getUser();
    this.isAdmin = this.authService.hasRole('ROLE_ADMIN');
    this.isRecepcionista = this.authService.hasRole('ROLE_RECEPCIONISTA');
    this.isCliente = this.authService.hasRole('ROLE_CLIENTE');

    if (this.isAdmin || this.isRecepcionista) {
      this.loadAdminMetrics();
    } else if (this.isCliente) {
      this.loadClienteData();
    }
  }

  loadAdminMetrics() {
    console.log("Iniciando carga de métricas de Dashboard...");
    this.dashboardService.obtenerMetricasAdmin().subscribe({
      next: (data) => {
        console.log("✅ Datos recibidos del Backend para el Dashboard:", data);

        this.totalHabitaciones = data.totalHabitaciones || 0;
        this.totalClientes = data.totalClientes || 0;
        this.totalReservas = data.totalReservas || 0;
        this.habitacionesDisponibles = data.habitacionesDisponibles || 0;
        this.habitacionesOcupadas = data.habitacionesOcupadas || 0;
        this.habitacionesMantenimiento = data.habitacionesMantenimiento || 0;
        this.ingresosTotales = data.ingresosTotales || 0;
        this.reservasPendientes = data.reservasPendientes || 0;
        this.reservasActivas = data.reservasActivas || 0;

        // 3. CLAVE: Forzar la detección de cambios para actualizar la vista
        this.cdr.detectChanges();

      },
      error: (err) => console.error('Error cargando métricas', err)
    });
  }

  loadClienteData() {
    // Cargar datos del cliente y sus reservas
    this.reservaService.obtenerTodas().subscribe({
      next: (reservas) => {
        // Filtrar reservas del cliente actual
        this.reservasCliente = reservas.filter((r: any) =>
          r.cliente?.usuario?.username === this.user?.sub
        );
        this.reservasActivasCliente = this.reservasCliente.filter((r: any) =>
          r.estadoReserva === 'ACTIVA'
        ).length;
        this.reservasFinalizadasCliente = this.reservasCliente.filter((r: any) =>
          r.estadoReserva === 'FINALIZADA'
        ).length;
      },
      error: (err) => console.error('Error cargando reservas', err)
    });
  }
}
