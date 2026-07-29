export type Amenity =
  | "PILETA"
  | "SAUNA"
  | "GIMNASIO"
  | "RESTAURANTE"
  | "BAR"
  | "ESTACIONAMIENTO"
  | "WIFI"
  | "AIRE_ACONDICIONADO"
  | "CALENTADOR"
  | "TV_CABLE"
  | "SERVICIO_HABITACIONES"
  | "LIMPIEZA_DIARIA"
  | "PISCINA_CUBIERTA"
  | "PISCINA_DESCUBIERTA"
  | "SPA"
  | "SALA_JUEGOS"
  | "SALA_REUNIONES"
  | "TRANSPORTE_AEROPUERTO";

export const AMENITIES: Amenity[] = [
  "PILETA",
  "SAUNA",
  "GIMNASIO",
  "RESTAURANTE",
  "BAR",
  "ESTACIONAMIENTO",
  "WIFI",
  "AIRE_ACONDICIONADO",
  "CALENTADOR",
  "TV_CABLE",
  "SERVICIO_HABITACIONES",
  "LIMPIEZA_DIARIA",
  "PISCINA_CUBIERTA",
  "PISCINA_DESCUBIERTA",
  "SPA",
  "SALA_JUEGOS",
  "SALA_REUNIONES",
  "TRANSPORTE_AEROPUERTO",
];

export interface AmenityHotel {
  id: number;
  amenity: Amenity;
}

export interface Hotel {
  id: number;
  nombre: string;
  cuit: string;
  domicilio: string;
  latitud: number | null;
  longitud: number | null;
  telefono: string | null;
  correoContacto: string | null;
  categoria: number | null;
  cerrado: boolean | null;
  amenities: AmenityHotel[] | null;
}

export interface TipoHabitacion {
  id: number;
  nombre: string | null;
  descripcion: string | null;
  capacidad: number | null;
}

export interface Habitacion {
  id: number;
  numero: number;
  piso: number;
  tipoHabitacion: TipoHabitacion;
  hotel: Hotel;
  disponible: boolean | null;
}

export interface Tarifa {
  id: number;
  fechaInicio: string; // "yyyy-MM-dd"
  fechaFin: string | null;
  tipoHabitacion: TipoHabitacion;
  precioNoche: number;
}

// ---- Request (creación/edición) ----

export interface HotelRequest {
  nombre: string;
  cuit: string;
  domicilio: string;
  latitud?: number | null;
  longitud?: number | null;
  telefono?: string | null;
  correoContacto?: string | null;
  categoria?: number | null;
  cerrado: boolean;
}

export interface HotelUpdateRequest {
  categoria?: number | null;
  telefono?: string | null;
  correoContacto?: string | null;
}

export interface TipoHabitacionRequest {
  id?: number; // requerido solo al crear
  nombre: string | null;
  descripcion: string | null;
  capacidad: number | null;
}

export interface HabitacionRequest {
  numero: number;
  piso: number;
  tipoHabitacion: { id: number };
  hotel: { id: number };
  disponible: boolean;
}

export interface TarifaRequest {
  tipoHabitacion: { id: number };
  fechaInicio?: string | null;
  fechaFin?: string | null;
  precioNoche: number;
}

export interface BuscarHotelesParams {
  nombre?: string;
  categoriaMinima?: number;
  domicilio?: string;
  cerrado?: boolean;
  amenity?: Amenity;
}

export interface BuscarHabitacionesParams {
  tipoHabitacionId?: number;
  capacidadMinima?: number;
  disponible?: boolean;
  hotelId?: number;
  precioMin?: number;
  precioMax?: number;
}
