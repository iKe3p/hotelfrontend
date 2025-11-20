import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { ClienteService } from '../../services/cliente.service';
import { Cliente } from '../../models/cliente.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-registro-cliente',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './registro-cliente.html',
  styleUrl: './registro-cliente.scss',
})
export class RegistroCliente implements OnInit {
  cliente: Cliente = {
    nombres: '',
    apellidos: '',
    dni: '',
    nacionalidad: '',
    email: '',
    telefono: ''
  };
  successMessage = '';
  errorMessage = '';
  isSubmitting = false;

  constructor(
    private clienteService: ClienteService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    if (!this.authService.hasRole('ROLE_RECEPCIONISTA') && !this.authService.hasRole('ROLE_ADMIN')) {
      this.router.navigate(['/login']);
    }
  }

  onSubmit() {
    if (this.isSubmitting) return;

    // Validar DNI
    if (!this.cliente.dni || !/^\d{8}$/.test(this.cliente.dni)) {
      this.errorMessage = 'El DNI debe tener exactamente 8 dígitos numéricos';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.clienteService.crearCliente(this.cliente).subscribe({
      next: (data) => {
        this.successMessage = 'Cliente registrado correctamente';
        this.isSubmitting = false;
        setTimeout(() => {
          this.router.navigate(['/clientes']);
        }, 1500);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al registrar cliente';
        this.isSubmitting = false;
      }
    });
  }

  onDniInput(event: any) {
    // Solo permitir números
    let value = event.target.value.replace(/[^0-9]/g, '');
    // Limitar a 8 dígitos
    if (value.length > 8) {
      value = value.substring(0, 8);
    }
    this.cliente.dni = value;
    event.target.value = value;
  }
}
