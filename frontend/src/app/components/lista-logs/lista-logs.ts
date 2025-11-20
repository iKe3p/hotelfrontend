import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { LogService, Log, LogPage } from '../../services/log';

@Component({
  selector: 'app-lista-logs',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent, DatePipe],
  templateUrl: './lista-logs.html',
  styleUrl: './lista-logs.scss',
})
export class ListaLogs implements OnInit {
  logsPage: LogPage | null = null;
  currentPage = 0;
  pageSize = 20;
  sortBy = 'timestamp';
  sortDir = 'desc';
  dniEmpleado = '';
  search = '';
  errorMessage = '';

  constructor(private logService: LogService) {}

  ngOnInit() {
    this.cargarLogs();
  }

  cargarLogs() {
    this.logService.obtenerLogs(
      this.currentPage,
      this.pageSize,
      this.sortBy,
      this.sortDir,
      this.dniEmpleado || undefined,
      this.search || undefined
    ).subscribe({
      next: (data) => {
        this.logsPage = data;
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar logs';
        console.error(err);
      }
    });
  }

  cambiarPagina(page: number) {
    this.currentPage = page;
    this.cargarLogs();
  }

  cambiarOrdenamiento(campo: string) {
    if (this.sortBy === campo) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = campo;
      this.sortDir = 'asc';
    }
    this.currentPage = 0;
    this.cargarLogs();
  }

  buscar() {
    this.currentPage = 0;
    this.cargarLogs();
  }

  limpiarFiltros() {
    this.dniEmpleado = '';
    this.search = '';
    this.currentPage = 0;
    this.cargarLogs();
  }

  getTotalPages(): number {
    return this.logsPage?.totalPages || 0;
  }

  getLogs(): Log[] {
    return this.logsPage?.content || [];
  }
}
