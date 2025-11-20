import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { EmpleadoService } from '../../services/empleado';
import { Empleado } from '../../models/empleado.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-editar-empleado',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './editar-empleado.html',
  styleUrl: './editar-empleado.scss',
})
export class EditarEmpleado implements OnInit {
  empleado: Empleado = {
    nombres: '',
    apellidos: '',
    dni: '',
    email: '',
    telefono: '',
    usuario: {
      username: '',
      rol: 'ROLE_RECEPCIONISTA'
    }
  };
  empleadoId: number | null = null;
  successMessage = '';
  errorMessage = '';
  isSubmitting = false;

  constructor(
    private empleadoService: EmpleadoService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    if (!this.authService.hasRole('ROLE_ADMIN')) {
      this.router.navigate(['/dashboard']);
      return;
    }

    this.route.params.subscribe(params => {
      const id = params['id'];
      if (id) {
        this.empleadoId = +id;
        this.cargarEmpleado();
      }
    });
  }

  cargarEmpleado() {
    if (!this.empleadoId) return;

    this.empleadoService.buscarPorId(this.empleadoId).subscribe({
      next: (data) => {
        this.empleado = data;
        if (!this.empleado.usuario) {
          this.empleado.usuario = {
            username: '',
            rol: 'ROLE_RECEPCIONISTA'
          };
        }
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar empleado';
        console.error(err);
      }
    });
  }

  onSubmit() {
    if (this.isSubmitting || !this.empleadoId) return;

    // Validaciones
    if (!this.empleado.nombres || !this.empleado.apellidos || !this.empleado.dni || 
        !this.empleado.email) {
      this.errorMessage = 'Por favor, complete todos los campos requeridos';
      return;
    }

    if (!/^\d{8}$/.test(this.empleado.dni)) {
      this.errorMessage = 'El DNI debe tener exactamente 8 dígitos';
      return;
    }

    if (this.empleado.telefono && !/^\d{9}$/.test(this.empleado.telefono)) {
      this.errorMessage = 'El teléfono debe tener 9 dígitos';
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.empleado.email)) {
      this.errorMessage = 'Ingrese un email válido';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.empleadoService.actualizarEmpleado(this.empleadoId, this.empleado).subscribe({
      next: (data) => {
        this.successMessage = 'Empleado actualizado exitosamente';
        this.isSubmitting = false;
        setTimeout(() => {
          this.router.navigate(['/empleados/lista']);
        }, 1500);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || err.error || 'Error al actualizar empleado';
        this.isSubmitting = false;
        console.error(err);
      }
    });
  }

  onDniInput(event: any) {
    let value = event.target.value.replace(/[^0-9]/g, '');
    if (value.length > 8) {
      value = value.substring(0, 8);
    }
    this.empleado.dni = value;
    event.target.value = value;
  }

  onTelefonoInput(event: any) {
    let value = event.target.value.replace(/[^0-9]/g, '');
    if (value.length > 9) {
      value = value.substring(0, 9);
    }
    this.empleado.telefono = value;
    event.target.value = value;
  }
}
