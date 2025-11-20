import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Servicio } from '../models/servicio.model';

@Injectable({
  providedIn: 'root'
})
export class ServicioService {
  private apiUrl = 'http://localhost:8084/api/servicios';

  constructor(private http: HttpClient) {}

  obtenerTodos(): Observable<Servicio[]> {
    return this.http.get<Servicio[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<Servicio> {
    return this.http.get<Servicio>(`${this.apiUrl}/${id}`);
  }
}

