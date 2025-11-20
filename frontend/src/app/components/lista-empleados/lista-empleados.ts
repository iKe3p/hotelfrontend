import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { EmpleadoService } from '../../services/empleado';
import { Empleado } from '../../models/empleado.model';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-lista-empleados',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarComponent],
  templateUrl: './lista-empleados.html',
  styleUrl: './lista-empleados.scss',
})
export class ListaEmpleados implements OnInit {
  empleados: Empleado[] = [];
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private empleadoService: EmpleadoService,
    private router: Router
  ) {}

  ngOnInit() {
    this.cargarEmpleados();
  }

  cargarEmpleados() {
    this.empleadoService.obtenerTodos().subscribe({
      next: (data) => {
        this.empleados = data;
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar empleados';
        console.error(err);
      }
    });
  }

  eliminarEmpleado(id: number | undefined) {
    if (!id) return;
    
    if (confirm('¿Estás seguro de que quieres eliminar a este empleado? Esta acción es irreversible.')) {
      this.empleadoService.eliminarEmpleado(id).subscribe({
        next: () => {
          this.successMessage = 'Empleado eliminado exitosamente.';
          this.cargarEmpleados();
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          this.errorMessage = 'Error al eliminar el empleado';
          console.error(err);
          setTimeout(() => this.errorMessage = '', 3000);
        }
      });
    }
  }

  getRolBadgeClass(rol: string | undefined): string {
    if (!rol) return 'bg-secondary';
    return rol === 'ROLE_ADMIN' ? 'bg-danger' : 'bg-secondary';
  }

  getRolDisplay(rol: string | undefined): string {
    if (!rol) return '';
    return rol.replace('ROLE_', '');
  }
}
