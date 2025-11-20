import { Cliente } from './cliente.model';
import { Habitacion } from './habitacion.model';
import { Servicio } from './servicio.model';

export interface Reserva {
  id?: number;
  cliente?: Cliente;
  habitacion?: Habitacion;
  fechaInicio: string; // ISO date string
  fechaFin: string; // ISO date string
  horaEntrada: string; // HH:mm format
  horaSalida: string; // HH:mm format
  diasEstadia: number;
  totalPagar: number;
  estadoReserva: string;
  fechaSalidaReal?: string;
  servicios?: Servicio[];
  opcionesServicios?: { [key: string]: string };
}

