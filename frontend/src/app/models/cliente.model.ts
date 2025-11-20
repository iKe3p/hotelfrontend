export interface Cliente {
  id?: number;
  nombres: string;
  apellidos: string;
  dni: string;
  nacionalidad?: string;
  email?: string;
  telefono?: string;
  hasActiveReservations?: boolean;
  usuario?: Usuario;
}

export interface Usuario {
  id?: number;
  username: string;
  password?: string;
  rol: string;
}

