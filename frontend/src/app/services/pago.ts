import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PagoRequest, PagoResponse } from '../models/pago.model';

@Injectable({
  providedIn: 'root',
})
export class PagoService {
  private apiUrl = 'http://localhost:8084/api/pagos';

  constructor(private http: HttpClient) {}

  procesarPago(request: PagoRequest): Observable<PagoResponse> {
    return this.http.post<PagoResponse>(this.apiUrl, request);
  }

  obtenerPagoPorReserva(reservaId: number): Observable<PagoResponse> {
    return this.http.get<PagoResponse>(`${this.apiUrl}/reserva/${reservaId}`);
  }
}
