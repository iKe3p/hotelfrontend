import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { ClienteService } from '../../services/cliente.service';
import { Cliente } from '../../models/cliente.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-editar-cliente',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './editar-cliente.html',
  styleUrl: './editar-cliente.scss',
})
export class EditarCliente implements OnInit {
  cliente: Cliente = {
    nombres: '',
    apellidos: '',
    dni: '',
    nacionalidad: '',
    email: '',
    telefono: ''
  };
  clienteId: number | null = null;
  successMessage = '';
  errorMessage = '';
  isSubmitting = false;
  isCliente = false;

  constructor(
    private clienteService: ClienteService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.isCliente = this.authService.hasRole('ROLE_CLIENTE');
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.clienteId = +id;
        this.cargarCliente();
      }
    });
  }

  cargarCliente() {
    if (!this.clienteId) return;

    this.clienteService.buscarPorId(this.clienteId).subscribe({
      next: (data) => {
        this.cliente = data;
        if (data.id) {
          this.clienteId = data.id;
        }
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar cliente';
        console.error(err);
      }
    });
  }

  onSubmit() {
    if (this.isSubmitting || !this.clienteId) return;

    if (!this.cliente.dni || !/^\d{8}$/.test(this.cliente.dni)) {
      this.errorMessage = 'El DNI debe tener exactamente 8 dígitos numéricos';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.clienteService.actualizarCliente(this.clienteId, this.cliente).subscribe({
      next: (data) => {
        this.successMessage = 'Cliente actualizado correctamente';
        this.isSubmitting = false;
        setTimeout(() => {
          if (this.isCliente) {
            this.router.navigate(['/dashboard']);
          } else {
            this.router.navigate(['/clientes/historial']);
          }
        }, 1500);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al actualizar cliente';
        this.isSubmitting = false;
      }
    });
  }

  onDniInput(event: any) {
    let value = event.target.value.replace(/[^0-9]/g, '');
    if (value.length > 8) {
      value = value.substring(0, 8);
    }
    this.cliente.dni = value;
    event.target.value = value;
  }
}
