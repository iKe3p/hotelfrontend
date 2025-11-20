import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { ClienteService } from '../../services/cliente.service';
import { Cliente } from '../../models/cliente.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './clientes.component.html',
  styleUrl: './clientes.component.css'
})
export class ClientesComponent implements OnInit {
  clientes: Cliente[] = [];
  dniBuscar: string = '';
  clienteEncontrado: Cliente | null = null;
  successMessage = '';
  errorMessage = '';

  constructor(
    private clienteService: ClienteService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.cargarClientes();
  }

  cargarClientes() {
    this.clienteService.obtenerTodos().subscribe({
      next: (data) => {
        this.clientes = data;
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar clientes';
      }
    });
  }

  buscarPorDni() {
    if (!this.dniBuscar || !/^\d{8}$/.test(this.dniBuscar)) {
      this.errorMessage = 'DNI inválido (debe tener 8 dígitos)';
      return;
    }

    this.clienteService.buscarPorDni(this.dniBuscar).subscribe({
      next: (data) => {
        this.clienteEncontrado = data;
        this.errorMessage = '';
      },
      error: (err) => {
        this.errorMessage = 'Cliente no encontrado';
        this.clienteEncontrado = null;
      }
    });
  }
}
