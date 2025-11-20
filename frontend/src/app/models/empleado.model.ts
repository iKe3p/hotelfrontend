export interface Empleado {
  id?: number;
  nombres: string;
  apellidos: string;
  dni: string;
  email: string;
  telefono?: string;
  usuario?: {
    id?: number;
    username: string;
    rol: string;
  };
}

