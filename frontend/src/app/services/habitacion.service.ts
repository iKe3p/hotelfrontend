import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Habitacion } from '../models/habitacion.model';

@Injectable({
  providedIn: 'root'
})
export class HabitacionService {
  private apiUrl = 'http://localhost:8084/api/habitaciones';

  constructor(private http: HttpClient) {}

  crearHabitacion(habitacion: Habitacion): Observable<Habitacion> {
    return this.http.post<Habitacion>(this.apiUrl, habitacion);
  }

  obtenerTodas(): Observable<Habitacion[]> {
    return this.http.get<Habitacion[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Habitacion> {
    return this.http.get<Habitacion>(`${this.apiUrl}/${id}`);
  }

  listarPorEstado(estado: string): Observable<Habitacion[]> {
    return this.http.get<Habitacion[]>(`${this.apiUrl}/estado/${estado}`);
  }

  actualizarHabitacion(id: number, habitacion: Habitacion): Observable<Habitacion> {
    return this.http.put<Habitacion>(`${this.apiUrl}/${id}`, habitacion);
  }

  cambiarEstado(id: number, estado: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/estado`, null, {
      params: { estado }
    });
  }

  eliminarHabitacion(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  obtenerDisponibles(): Observable<Habitacion[]> {
    return this.listarPorEstado('DISPONIBLE');
  }

  actualizarPorTipo(tipo: string, precioPorNoche: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/tipo/${tipo}/precio`, null, {
      params: { precioPorNoche: precioPorNoche.toString() }
    });
  }
}

