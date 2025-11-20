import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { HabitacionService } from '../../services/habitacion.service';
import { Habitacion } from '../../models/habitacion.model';
import { AuthService } from '../../services/auth.service';
import { Observable } from 'rxjs'; // 👈 Asegúrate de importar Observable

@Component({
  selector: 'app-habitaciones',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent],
  templateUrl: './habitaciones.component.html',
  styleUrl: './habitaciones.component.css'
})
export class HabitacionesComponent implements OnInit {
  // 1. Cambiar el tipo a Observable y usar el sufijo $
  habitaciones$: Observable<Habitacion[]> | undefined;
  isAdmin = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private habitacionService: HabitacionService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.isAdmin = this.authService.hasRole('ROLE_ADMIN');
    this.cargarHabitaciones();
  }

  cargarHabitaciones() {
    // 2. Asignar el Observable directamente, sin hacer .subscribe()
    this.habitaciones$ = this.habitacionService.obtenerTodas();
  }

  eliminarHabitacion(id: number | undefined) {
    if (!id) return;

    if (confirm('¿Estás seguro de que quieres eliminar esta habitación?')) {
      this.habitacionService.eliminarHabitacion(id).subscribe({
        next: () => {
          this.successMessage = 'Habitación eliminada correctamente';
          // 3. Volver a llamar a cargarHabitaciones para obtener el nuevo Observable
          this.cargarHabitaciones();
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          this.errorMessage = 'Error al eliminar habitación';
          setTimeout(() => this.errorMessage = '', 3000);
        }
      });
    }
  }
}
