import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { ReservaService } from '../../services/reserva.service';
import { ServicioService } from '../../services/servicio.service';
import { Reserva } from '../../models/reserva.model';
import { Servicio } from '../../models/servicio.model';

@Component({
  selector: 'app-seleccionar-servicios',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './seleccionar-servicios.html',
  styleUrl: './seleccionar-servicios.scss',
})
export class SeleccionarServicios implements OnInit {
  reserva: Reserva | null = null;
  servicios: Servicio[] = [];
  serviciosSeleccionados: number[] = [];
  opcionesServicios: { [key: number]: string } = {};
  reservaId: number | null = null;
  errorMessage = '';
  successMessage = '';
  totalServicios = 0;

  constructor(
    private reservaService: ReservaService,
    private servicioService: ServicioService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.reservaId = Number(params['id']);
      if (this.reservaId) {
        this.cargarReserva();
        this.cargarServicios();
      }
    });
  }

  cargarReserva() {
    if (!this.reservaId) return;
    
    this.reservaService.buscarPorId(this.reservaId).subscribe({
      next: (data) => {
        this.reserva = data;
        if (data.servicios) {
          this.serviciosSeleccionados = data.servicios.map(s => s.id!).filter(id => id !== undefined) as number[];
        }
        this.calcularTotalServicios();
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar reserva';
        console.error(err);
      }
    });
  }

  cargarServicios() {
    this.servicioService.obtenerTodos().subscribe({
      next: (data) => {
        this.servicios = data;
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar servicios';
        console.error(err);
      }
    });
  }

  toggleServicio(servicioId: number) {
    const index = this.serviciosSeleccionados.indexOf(servicioId);
    if (index > -1) {
      this.serviciosSeleccionados.splice(index, 1);
      delete this.opcionesServicios[servicioId];
    } else {
      this.serviciosSeleccionados.push(servicioId);
    }
    this.calcularTotalServicios();
  }

  estaSeleccionado(servicioId: number): boolean {
    return this.serviciosSeleccionados.includes(servicioId);
  }

  calcularTotalServicios() {
    this.totalServicios = this.servicios
      .filter(s => this.serviciosSeleccionados.includes(s.id!))
      .reduce((sum, s) => sum + (s.precio || 0), 0);
  }

  guardarServicios() {
    if (!this.reservaId) return;

    this.errorMessage = '';
    this.successMessage = '';

    const opciones = this.serviciosSeleccionados
      .map(id => this.opcionesServicios[id])
      .filter(op => op !== undefined);

    this.reservaService.asignarServicios(this.reservaId, this.serviciosSeleccionados, opciones).subscribe({
      next: (data) => {
        this.successMessage = 'Servicios asignados correctamente';
        setTimeout(() => {
          this.router.navigate(['/reservas', this.reservaId, 'pago']);
        }, 1500);
      },
      error: (err) => {
        this.errorMessage = err.error?.error || 'Error al asignar servicios';
        console.error(err);
      }
    });
  }

  omitirServicios() {
    if (!this.reservaId) return;
    this.router.navigate(['/reservas', this.reservaId, 'pago']);
  }
}
