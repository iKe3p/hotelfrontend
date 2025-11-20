import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Reserva } from '../models/reserva.model';

@Injectable({
  providedIn: 'root'
})
export class ReservaService {
  // Base API URL - debe apuntar a /api/reservas en el backend
  private apiUrl = 'http://localhost:8084/api/reservas';

  constructor(private http: HttpClient) {}

  crearReserva(reserva: Reserva): Observable<Reserva> {
    return this.http.post<Reserva>(this.apiUrl, reserva);
  }

  obtenerTodas(): Observable<Reserva[]> {
    return this.http.get<Reserva[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Reserva> {
    return this.http.get<Reserva>(`${this.apiUrl}/${id}`);
  }

  actualizarReserva(id: number, reserva: Reserva): Observable<Reserva> {
    return this.http.put<Reserva>(`${this.apiUrl}/${id}`, reserva);
  }

  finalizarReserva(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/finalizar`, {});
  }

  cancelarReserva(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/cancelar`, {});
  }

  eliminarReserva(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  obtenerIngresosTotales(): Observable<{ ingresosTotales: number }> {
    return this.http.get<{ ingresosTotales: number }>(`${this.apiUrl}/estadisticas/ingresos`);
  }

  contarReservas(): Observable<{ totalReservas: number }> {
    return this.http.get<{ totalReservas: number }>(`${this.apiUrl}/estadisticas/contar`);
  }

  contarReservasPorEstado(estado: string): Observable<{ estado: string; cantidad: number }> {
    return this.http.get<{ estado: string; cantidad: number }>(`${this.apiUrl}/estadisticas/contar/${estado}`);
  }

  calcularCosto(habitacionId: number, fechaInicio: string, fechaFin: string): Observable<{ dias: number; total: number; error?: string }> {
  const params = new URLSearchParams();
  params.set('habitacionId', habitacionId.toString());
  params.set('fechaInicio', fechaInicio);
  params.set('fechaFin', fechaFin);

  return this.http.get<{ dias: number; total: number; error?: string }>(`${this.apiUrl}/calcular-costo?${params.toString()}`);
}

  calcularCostoCliente(habitacionId: number, fechaInicio: string, fechaFin: string): Observable<{ dias: number; total: number }> {
    const params = new URLSearchParams();
    params.set('habitacionId', habitacionId.toString());
    params.set('fechaInicio', fechaInicio);
    params.set('fechaFin', fechaFin);
    return this.http.get<{ dias: number; total: number }>(`http://localhost:8084/cliente/reservas/calcular-costo?${params.toString()}`);
  }

  asignarServicios(reservaId: number, servicioIds: number[], opciones?: string[]): Observable<Reserva> {
    return this.http.put<Reserva>(`${this.apiUrl}/${reservaId}/servicios`, {
      servicioIds,
      opciones: opciones || []
    });
  }
}

