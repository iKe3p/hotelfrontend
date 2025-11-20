import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { HabitacionService } from '../../services/habitacion.service';
import { Habitacion } from '../../models/habitacion.model';

@Component({
  selector: 'app-habitacion-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './habitacion-form.html',
  styleUrl: './habitacion-form.scss',
})
export class HabitacionForm implements OnInit {
  habitacion: Habitacion = {
    numero: '',
    tipo: '',
    precioPorNoche: 0,
    estado: 'DISPONIBLE'
  };

  accion: 'nueva' | 'editar' = 'nueva';
  errorMessage: string = '';
  successMessage: string = '';
  // Inicializamos isLoading a true para que la carga empiece oculta
  isLoading: boolean = true;

  constructor(
    private habitacionService: HabitacionService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.accion = 'editar';
        // En modo 'editar', ya se puso isLoading = true en la declaración de la clase
        this.cargarHabitacion(Number(params['id']));
      } else {
        this.accion = 'nueva';
        // En modo 'nueva', no hay que cargar datos, se muestra inmediatamente
        this.isLoading = false;
      }
    });
  }

  cargarHabitacion(id: number) {
    // Al ser una operación asíncrona, gestionamos el fin de la carga
    this.habitacionService.buscarPorId(id).subscribe({
      next: (data) => {
        this.habitacion = data;
        // La carga terminó exitosamente, mostramos el formulario
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar habitación. Es posible que la habitación no exista.';
        console.error(err);
        // La carga falló, pero también terminamos el estado de carga
        this.isLoading = false;
      }
    });
  }

  guardarHabitacion() {
    if (!this.habitacion.numero || !this.habitacion.tipo || !this.habitacion.precioPorNoche) {
      this.errorMessage = 'Por favor, complete todos los campos requeridos';
      return;
    }

    // Opcionalmente, puedes establecer isLoading = true aquí para mostrar un cargador
    // mientras el formulario se está enviando.

    if (this.accion === 'nueva') {
      this.habitacionService.crearHabitacion(this.habitacion).subscribe({
        next: () => {
          this.successMessage = 'Habitación creada exitosamente.';
          setTimeout(() => {
            this.router.navigate(['/habitaciones']);
          }, 1500);
        },
        error: (err) => {
          this.errorMessage = err.error || 'Error al crear habitación';
          console.error(err);
        }
      });
    } else {
      if (!this.habitacion.id) {
        this.errorMessage = 'ID de habitación no válido';
        return;
      }
      this.habitacionService.actualizarHabitacion(this.habitacion.id, this.habitacion).subscribe({
        next: () => {
          this.successMessage = 'Habitación actualizada exitosamente.';
          setTimeout(() => {
            this.router.navigate(['/habitaciones']);
          }, 1500);
        },
        error: (err) => {
          this.errorMessage = err.error || 'Error al actualizar habitación';
          console.error(err);
        }
      });
    }
  }
}
