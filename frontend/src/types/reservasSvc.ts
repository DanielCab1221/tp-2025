export type EstadoReserva =
  | "RESERVADA"
  | "CONFIRMADA"
  | "EFECTUADA"
  | "FINALIZADA"
  | "ADEUDADA"
  | "CANCELADA"
  | "BLOQUEADA"
  | "CERRADA";

export interface GeoJsonPoint {
  x: number; // longitud
  y: number; // latitud
  type: "Point";
  coordinates: [number, number];
}

export interface HotelResumen {
  id: number | null;
  nombre: string | null;
  categoria: number | null;
  domicilio: string | null;
  cerrado: boolean | null;
  ubicacion: GeoJsonPoint | null;
}

export interface Habitacion {
  id: string; // Mongo ObjectId
  habitacionId: number | null;
  capacidad: number | null;
  precioNoche: number | null;
  disponible: boolean | null;
  amenities: string[] | null;
  hotel: HotelResumen | null;
  idTipoHabitacion: number | null;
  tipoHabitacion: string | null;
}

export interface Huesped {
  idUsuario: string | null;
  nombreApellido: string | null;
  email: string | null;
}

export interface Tarifa {
  precio: number | null;
  moneda: string | null;
}

export interface Pago {
  method: string | null;
  transactionId: string | null;
  amount: Tarifa | null;
  status: string | null;
}

export interface Review {
  rating: number;
  comment: string | null;
  createdAt: string | null;
}

export interface Reserva {
  _id: string;
  idHabitacion: string;
  hotelId: number | null;
  createdAt: string;
  checkIn: string;
  checkOut: string | null;
  precioNoche: number | null;
  precioTotal: number | null;
  huesped: Huesped | null;
  pago: Pago[] | null;
  clientReview: Review | null;
  hostReview: Review | null;
  estadoReserva: EstadoReserva | null;
}

export interface BuscarDisponibilidadParams {
  checkIn: string; // "yyyy-MM-dd"
  checkOut: string; // "yyyy-MM-dd"
  huespedes?: number;
  precioMin?: number;
  precioMax?: number;
  categoriaMinima?: number;
  amenities?: string[];
  latitud?: number;
  longitud?: number;
  distanciaMaximaKm?: number;
}

export interface CrearReservaRequest {
  idHabitacion: string;
  checkIn: string; // ISO instant, ej "2026-08-01T00:00:00Z"
  checkOut: string;
  huesped: Huesped;
}

export interface RegistrarPagoRequest {
  method: string;
  transactionId: string;
  amount: Tarifa;
  status: string;
}

export interface ReviewRequest {
  rating: number;
  comment: string;
}
