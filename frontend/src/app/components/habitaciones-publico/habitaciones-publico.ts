import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { HabitacionService } from '../../services/habitacion.service';
import { Habitacion } from '../../models/habitacion.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-habitaciones-publico',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent, DecimalPipe],
  templateUrl: './habitaciones-publico.html',
  styleUrl: './habitaciones-publico.scss',
})
export class HabitacionesPublico implements OnInit {
  habitaciones: Habitacion[] = [];
  isLoggedIn = false;
  errorMessage: string = '';

  constructor(
    private habitacionService: HabitacionService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef // <--- Inyectar aquí
  ) {}

  ngOnInit() {
    this.isLoggedIn = this.authService.isAuthenticated();
    this.cargarHabitaciones();
  }

  cargarHabitaciones() {
    console.log('Iniciando carga...');
    this.habitacionService.obtenerTodas().subscribe({
      next: (data) => {
        console.log('Datos recibidos:', data);
        this.habitaciones = data;
        this.cdr.detectChanges(); // <--- FORZAR LA ACTUALIZACIÓN DE LA PANTALLA
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Error al cargar habitaciones';
      }
    });
  }

  getTipoClass(tipo: string): string {
    const tipos: { [key: string]: string } = {
      'Simple': 'tipo-simple',
      'Doble': 'tipo-doble',
      'Suite': 'tipo-suite',
      'Familiar': 'tipo-familiar'
    };
    return tipos[tipo] || 'tipo-default';
  }
}
