export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  nombres: string;
  apellidos: string;
  dni: string;
  nacionalidad?: string;
  email: string;
  telefono?: string;
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}

