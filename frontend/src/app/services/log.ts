import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Log {
  id?: number;
  timestamp?: string;
  empleado?: any;
  tipoAccion?: string;
  detalleAccion?: string;
  entidadAfectada?: string;
  entidadAfectadaId?: number;
}

export interface LogPage {
  content: Log[];
  totalElements: number;
  totalPages: number;
  number: number;
}

@Injectable({
  providedIn: 'root',
})
export class LogService {
  private apiUrl = 'http://localhost:8084/api/auditoria';

  constructor(private http: HttpClient) {}

  obtenerLogs(page: number = 0, size: number = 20, sortBy: string = 'timestamp', sortDir: string = 'desc', dniEmpleado?: string, search?: string): Observable<LogPage> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    
    if (dniEmpleado) {
      params = params.set('dniEmpleado', dniEmpleado);
    }
    if (search) {
      params = params.set('search', search);
    }
    
    return this.http.get<LogPage>(`${this.apiUrl}/logs`, { params });
  }
}
