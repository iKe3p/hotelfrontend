export interface Pago {
  id?: number;
  reservaId?: number;
  monto: number;
  metodoPago: string;
  fechaPago?: string;
  estado?: string;
}

export interface PagoRequest {
  reservaId: number;
  metodoPago?: string;
  metodo?: string;
  canal?: string;
  numeroTarjeta?: string;
  fechaExpiracion?: string;
  cvv?: string;
  numeroTelefono?: string;
  telefonoWallet?: string;
  titularTarjeta?: string;
  titularWallet?: string;
  tarjeta?: {
    numero?: string;
    cvv?: string;
    fechaExp?: string;
    titular?: string;
  };
  wallet?: {
    telefono?: string;
    titular?: string;
  };
}

export interface PagoResponse {
  exito?: boolean;
  mensaje: string;
  referencia?: string;
  estado?: string;
  montoBase?: number;
  montoServicios?: number;
  montoTotal?: number;
  reservaId?: number;
}

