import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { EmpleadoService } from '../../services/empleado';
import { Empleado } from '../../models/empleado.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-registrar-empleado',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './registrar-empleado.html',
  styleUrl: './registrar-empleado.scss',
})
export class RegistrarEmpleado implements OnInit {
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
  confirmPassword = '';
  successMessage = '';
  errorMessage = '';
  isSubmitting = false;

  constructor(
    private empleadoService: EmpleadoService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    if (!this.authService.hasRole('ROLE_ADMIN')) {
      this.router.navigate(['/dashboard']);
    }
  }

  onSubmit() {
    if (this.isSubmitting) return;

    // Validaciones
    if (!this.empleado.nombres || !this.empleado.apellidos || !this.empleado.dni || 
        !this.empleado.email || !this.empleado.usuario?.username || !this.empleado.usuario?.rol) {
      this.errorMessage = 'Por favor, complete todos los campos requeridos';
      return;
    }

    if (!/^\d{8}$/.test(this.empleado.dni)) {
      this.errorMessage = 'El DNI debe tener exactamente 8 dígitos';
      return;
    }

    if (!/^\d{9}$/.test(this.empleado.telefono || '')) {
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

    this.empleadoService.crearEmpleado(this.empleado).subscribe({
      next: (data) => {
        this.successMessage = 'Empleado registrado exitosamente';
        this.isSubmitting = false;
        setTimeout(() => {
          this.router.navigate(['/empleados/lista']);
        }, 1500);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || err.error || 'Error al registrar empleado';
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
