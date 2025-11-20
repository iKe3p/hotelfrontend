import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ClienteService } from '../../services/cliente.service';
import { ReservaService } from '../../services/reserva.service';
import { Cliente } from '../../models/cliente.model';
import { Reserva } from '../../models/reserva.model';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-historial-cliente',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent],
  templateUrl: './historial-cliente.html',
  styleUrl: './historial-cliente.scss',
})
export class HistorialCliente implements OnInit {
  cliente: Cliente | null = null;
  clientes: Cliente[] = [];
  reservasCliente: Reserva[] = [];
  reservasBloqueo: any[] = [];

  // Búsqueda y paginación
  searchTerm: string = '';
  dniSearch: string = '';
  currentPage: number = 0;
  pageSize: number = 10;
  totalPages: number = 0;
  sortBy: string = 'id';
  sortDir: string = 'asc';

  successMessage: string = '';
  errorMessage: string = '';

  constructor(
    private clienteService: ClienteService,
    private reservaService: ReservaService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      const id = params['id'];
      const dni = params['dni'];
      const search = params['search'];
      const page = params['page'];
      const sortByParam = params['sortBy'];
      const sortDirParam = params['sortDir'];

      if (dni) {
        this.dniSearch = dni;
        this.buscarPorDni(dni);
      } else if (id) {
        this.cargarClientePorId(+id);
      } else {
        this.searchTerm = search || '';
        this.currentPage = page ? +page : 0;
        this.sortBy = sortByParam || 'id';
        this.sortDir = sortDirParam || 'asc';
        this.cargarListaClientes();
      }
    });
  }

  cargarListaClientes() {
    this.clienteService.obtenerTodos().subscribe({
      next: (data) => {
        let clientesFiltrados = data;

        // Filtrar por término de búsqueda
        if (this.searchTerm && this.searchTerm.trim()) {
          const term = this.searchTerm.toLowerCase();
          clientesFiltrados = clientesFiltrados.filter(
            (c) =>
              c.dni?.toLowerCase().includes(term) ||
              c.nombres?.toLowerCase().includes(term) ||
              c.apellidos?.toLowerCase().includes(term)
          );
        }

        // Ordenar
        clientesFiltrados.sort((a, b) => {
          let aValue: any;
          let bValue: any;

          switch (this.sortBy) {
            case 'dni':
              aValue = a.dni || '';
              bValue = b.dni || '';
              break;
            case 'nombres':
              aValue = a.nombres || '';
              bValue = b.nombres || '';
              break;
            case 'apellidos':
              aValue = a.apellidos || '';
              bValue = b.apellidos || '';
              break;
            default:
              aValue = a.id || 0;
              bValue = b.id || 0;
          }

          if (aValue < bValue) return this.sortDir === 'asc' ? -1 : 1;
          if (aValue > bValue) return this.sortDir === 'asc' ? 1 : -1;
          return 0;
        });

        // Verificar reservas activas para cada cliente
        this.reservaService.obtenerTodas().subscribe({
          next: (reservas) => {
            clientesFiltrados.forEach((cliente) => {
              cliente.hasActiveReservations = reservas.some(
                (r) =>
                  r.cliente?.id === cliente.id &&
                  (r.estadoReserva === 'ACTIVA' || r.estadoReserva === 'PENDIENTE')
              );
            });

            // Paginación
            this.totalPages = Math.ceil(clientesFiltrados.length / this.pageSize);
            const start = this.currentPage * this.pageSize;
            const end = start + this.pageSize;
            this.clientes = clientesFiltrados.slice(start, end);
          },
          error: (err) => {
            console.error('Error al cargar reservas:', err);
            this.totalPages = Math.ceil(clientesFiltrados.length / this.pageSize);
            const start = this.currentPage * this.pageSize;
            const end = start + this.pageSize;
            this.clientes = clientesFiltrados.slice(start, end);
          },
        });
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar la lista de clientes.';
        console.error(err);
      },
    });
  }

  buscar() {
    this.currentPage = 0;
    this.router.navigate(['/clientes/historial'], {
      queryParams: {
        search: this.searchTerm || null,
        page: this.currentPage,
        sortBy: this.sortBy,
        sortDir: this.sortDir,
      },
    });
  }

  buscarPorDni(dni: string) {
    if (!dni || !dni.match(/^\d{8}$/)) {
      this.errorMessage = 'El DNI debe contener exactamente 8 dígitos numéricos';
      return;
    }

    this.clienteService.buscarPorDni(dni).subscribe({
      next: (data) => {
        this.cliente = data;
        this.cargarReservasCliente(data.id!);
      },
      error: (err) => {
        this.errorMessage = 'No se encontró cliente con DNI: ' + dni;
        this.cliente = null;
      },
    });
  }

  buscarDetallePorDni() {
    if (!this.dniSearch || !this.dniSearch.match(/^\d{8}$/)) {
      this.errorMessage = 'El DNI debe contener exactamente 8 dígitos numéricos';
      return;
    }

    this.router.navigate(['/clientes/historial'], {
      queryParams: { dni: this.dniSearch },
    });
  }

  cargarClientePorId(id: number) {
    this.clienteService.buscarPorId(id).subscribe({
      next: (cliente) => {
        this.cliente = cliente;
        this.cargarReservasCliente(id);
      },
      error: (err) => {
        this.errorMessage = 'Cliente no encontrado.';
        this.cliente = null;
        console.error(err);
      },
    });
  }

  cargarReservasCliente(clienteId: number) {
    this.reservaService.obtenerTodas().subscribe({
      next: (reservas) => {
        this.reservasCliente = reservas.filter(
          (r) => r.cliente?.id === clienteId
        );

        // Verificar reservas activas para bloqueo de eliminación
        this.reservasBloqueo = this.reservasCliente
          .filter(
            (r) =>
              r.estadoReserva === 'ACTIVA' || r.estadoReserva === 'PENDIENTE'
          )
          .map((r) => ({
            id: r.id,
            habitacion: r.habitacion
              ? `${r.habitacion.numero} (${r.habitacion.tipo})`
              : 'Sin asignar',
            fechaInicio: r.fechaInicio,
            fechaFin: r.fechaFin,
            estado: r.estadoReserva,
          }));
      },
      error: (err) => {
        this.errorMessage = 'Error al cargar reservas del cliente.';
        console.error(err);
      },
    });
  }

  ordenar(campo: string) {
    if (this.sortBy === campo) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = campo;
      this.sortDir = 'asc';
    }
    this.router.navigate(['/clientes/historial'], {
      queryParams: {
        search: this.searchTerm || null,
        page: this.currentPage,
        sortBy: this.sortBy,
        sortDir: this.sortDir,
      },
    });
  }

  cambiarPagina(page: number) {
    this.currentPage = page;
    this.router.navigate(['/clientes/historial'], {
      queryParams: {
        search: this.searchTerm || null,
        page: this.currentPage,
        sortBy: this.sortBy,
        sortDir: this.sortDir,
      },
    });
  }

  verDetalle(clienteId: number) {
    this.router.navigate(['/clientes/historial'], {
      queryParams: { id: clienteId },
    });
  }

  editarCliente(clienteId: number) {
    this.router.navigate(['/clientes/editar', clienteId]);
  }

  eliminarCliente(clienteId: number) {
    if (
      !confirm(
        '¿Estás seguro de que quieres eliminar a este cliente? Esta acción es irreversible y podría afectar reservas.'
      )
    ) {
      return;
    }

    this.clienteService.eliminarCliente(clienteId).subscribe({
      next: () => {
        this.successMessage = 'Cliente eliminado correctamente.';
        setTimeout(() => {
          this.router.navigate(['/clientes/historial']);
        }, 2000);
      },
      error: (err) => {
        if (err.error && err.error.reservasActivas) {
          this.reservasBloqueo = err.error.reservasActivas.map((r: any) => ({
            id: r.id,
            habitacion: r.habitacion || 'Sin asignar',
            fechaInicio: r.fechaInicio,
            fechaFin: r.fechaFin,
            estado: r.estado,
          }));
          this.errorMessage =
            'No puedes eliminar a este cliente porque mantiene reservas activas.';
        } else {
          this.errorMessage =
            err.error?.mensaje || err.error || 'Error al eliminar cliente.';
        }
      },
    });
  }

  cancelarReserva(reservaId: number) {
    if (
      !confirm(
        '¿Estás seguro de que quieres CANCELAR esta reserva? Esta acción liberará la habitación.'
      )
    ) {
      return;
    }

    this.reservaService.cancelarReserva(reservaId).subscribe({
      next: () => {
        this.successMessage = 'Reserva cancelada correctamente.';
        if (this.cliente) {
          this.cargarReservasCliente(this.cliente.id!);
        }
      },
      error: (err) => {
        this.errorMessage =
          err.error?.error || 'Error al cancelar la reserva.';
      },
    });
  }

  finalizarReserva(reservaId: number) {
    if (
      !confirm(
        '¿Estás seguro de que quieres FINALIZAR esta reserva (realizar Check-out)?'
      )
    ) {
      return;
    }

    this.reservaService.finalizarReserva(reservaId).subscribe({
      next: () => {
        this.successMessage = 'Reserva finalizada correctamente.';
        if (this.cliente) {
          this.cargarReservasCliente(this.cliente.id!);
        }
      },
      error: (err) => {
        this.errorMessage =
          err.error?.error || 'Error al finalizar la reserva.';
      },
    });
  }

  calcularTotalServicios(reserva: Reserva): number {
    if (!reserva.servicios || reserva.servicios.length === 0) {
      return 0;
    }
    return reserva.servicios.reduce(
      (total, servicio) => total + (servicio.precio || 0),
      0
    );
  }

  calcularTotalFinal(reserva: Reserva): number {
    const totalHabitacion = reserva.totalPagar || 0;
    const totalServicios = this.calcularTotalServicios(reserva);
    return totalHabitacion + totalServicios;
  }

  tienePago(reserva: Reserva): boolean {
    // Asumimos que si no hay información de pago, no tiene pago
    // Esto podría necesitar ajustes según el modelo real
    return false;
  }

  volverALista() {
    this.cliente = null;
    this.reservasCliente = [];
    this.reservasBloqueo = [];
    this.router.navigate(['/clientes/historial']);
  }

  getPaginasArray(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }
}
