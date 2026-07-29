export interface Banco {
  id: number;
  nombre: string;
}

export interface CuentaBancaria {
  id: number;
  numeroCuenta: string;
  cbu: string;
  alias: string;
  banco: Banco | null;
}

export interface TarjetaCredito {
  id: number;
  numero: string;
  nombreTitular: string;
  fechaVencimiento: string; // "MM/YY"
  cvc: string;
  esPrincipal: boolean;
  banco: Banco | null;
}

interface UsuarioBase {
  id: number;
  nombre: string;
  email: string;
  telefono: string;
  dni: string;
}

export interface Huesped extends UsuarioBase {
  fechaNacimiento: string; // "yyyy-MM-dd"
  tarjetaCredito: TarjetaCredito[];
}

export interface Propietario extends UsuarioBase {
  cuentaBancaria: CuentaBancaria | null;
  idHotel: number | null;
}

export type Usuario = Huesped | Propietario;

// La API no manda un discriminador de tipo: se distingue por estructura.
export function isHuesped(u: Usuario): u is Huesped {
  return "tarjetaCredito" in u;
}
export function isPropietario(u: Usuario): u is Propietario {
  return "cuentaBancaria" in u;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

export interface CrearHuespedRequest {
  nombre: string;
  email: string;
  telefono: string;
  dni: string;
  fechaNacimiento: string;
  numeroCC: string;
  nombreTitular: string;
  fechaVencimientoCC: string;
  cvcCC: string;
  esPrincipalCC: boolean;
  idBanco: number;
}

export interface CrearPropietarioRequest {
  nombre: string;
  email?: string;
  telefono: string;
  dni: string;
  idHotel?: number | null;
  cuentaBancaria: {
    numeroCuenta: string;
    cbu: string;
    alias: string;
    idBanco: number;
  };
}

export interface ActualizarHuespedRequest {
  nombre?: string;
  email?: string;
  telefono?: string;
  fechaNacimiento?: string;
}

export interface ActualizarPropietarioRequest {
  nombre?: string;
  email?: string;
  telefono?: string;
  idHotel?: number | null;
}

export interface TarjetaCreditoRequest {
  numero: string;
  nombreTitular: string;
  fechaVencimiento: string;
  cvc: string;
  esPrincipal?: boolean;
  idBanco: number;
}

export interface BancoRequest {
  nombre: string;
}
