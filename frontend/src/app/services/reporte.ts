import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ReporteService {
  private apiUrl = 'http://localhost:8084/reportes/api';

  constructor(private http: HttpClient) {}

  obtenerIngresosPorPeriodo(fechaInicio: string, fechaFin: string): Observable<any[]> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    return this.http.get<any[]>(`${this.apiUrl}/ingresos`, { params });
  }

  obtenerMovimientoPorPeriodo(fechaInicio: string, fechaFin: string): Observable<any[]> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    return this.http.get<any[]>(`${this.apiUrl}/movimiento`, { params });
  }
}
