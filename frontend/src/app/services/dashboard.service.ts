import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';
import { HabitacionService } from './habitacion.service';
import { ClienteService } from './cliente.service';
import { ReservaService } from './reserva.service';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  constructor(
    private http: HttpClient,
    private habitacionService: HabitacionService,
    private clienteService: ClienteService,
    private reservaService: ReservaService
  ) {}

  obtenerMetricasAdmin(): Observable<any> {
    return forkJoin({
      totalHabitaciones: this.habitacionService.obtenerTodas().pipe(map(arr => arr.length)),
      totalClientes: this.clienteService.obtenerTodos().pipe(map(arr => arr.length)),
      totalReservas: this.reservaService.contarReservas().pipe(map(obj => obj.totalReservas)),
      habitacionesDisponibles: this.habitacionService.listarPorEstado('DISPONIBLE').pipe(map(arr => arr.length)),
      habitacionesOcupadas: this.habitacionService.listarPorEstado('OCUPADA').pipe(map(arr => arr.length)),
      habitacionesMantenimiento: this.habitacionService.listarPorEstado('MANTENIMIENTO').pipe(map(arr => arr.length)),
      ingresosTotales: this.reservaService.obtenerIngresosTotales().pipe(map(obj => obj.ingresosTotales)),
      reservasPendientes: this.reservaService.contarReservasPorEstado('PENDIENTE').pipe(map(obj => obj.cantidad)),
      reservasActivas: this.reservaService.contarReservasPorEstado('ACTIVA').pipe(map(obj => obj.cantidad))
    });
  }

  obtenerMetricasCliente(clienteId: number): Observable<any> {
    return forkJoin({
      reservas: this.reservaService.obtenerTodas(),
      cliente: this.clienteService.buscarPorDni('') // Ajustar según necesidad
    });
  }
}

