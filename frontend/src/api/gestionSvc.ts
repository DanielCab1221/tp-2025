import { buildQuery, request } from "../lib/http";
import type {
  BuscarHabitacionesParams,
  BuscarHotelesParams,
  Habitacion,
  HabitacionRequest,
  Hotel,
  HotelRequest,
  HotelUpdateRequest,
  Tarifa,
  TarifaRequest,
  TipoHabitacion,
  TipoHabitacionRequest,
  Amenity,
} from "../types/gestionSvc";

const BASE = import.meta.env.VITE_GESTION_SVC_URL;

export const gestionSvc = {
  // Hoteles
  listarHoteles: (params: BuscarHotelesParams = {}) =>
    request<Hotel[]>(`${BASE}/hoteles${buildQuery(params)}`),
  obtenerHotel: (id: number) => request<Hotel>(`${BASE}/hoteles/${id}`),
  crearHotel: (body: HotelRequest) =>
    request<Hotel>(`${BASE}/hoteles`, {
      method: "POST",
      body: JSON.stringify(body),
    }),
  actualizarHotel: (id: number, body: HotelUpdateRequest) =>
    request<Hotel>(`${BASE}/hoteles/${id}`, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  cerrarHotel: (id: number) =>
    request<Hotel>(`${BASE}/hoteles/${id}/cerrar`, { method: "PATCH" }),
  agregarAmenities: (id: number, amenities: Amenity[]) =>
    request<Hotel>(`${BASE}/hoteles/${id}/amenities`, {
      method: "PUT",
      body: JSON.stringify(amenities),
    }),
  quitarAmenity: (id: number, amenity: Amenity) =>
    request<void>(`${BASE}/hoteles/${id}/amenities/${amenity}`, {
      method: "DELETE",
    }),

  // Tipos de habitación
  listarTiposHabitacion: () =>
    request<TipoHabitacion[]>(`${BASE}/tipos-habitacion`),
  obtenerTipoHabitacion: (id: number) =>
    request<TipoHabitacion>(`${BASE}/tipos-habitacion/${id}`),
  crearTipoHabitacion: (body: TipoHabitacionRequest) =>
    request<TipoHabitacion>(`${BASE}/tipos-habitacion`, {
      method: "POST",
      body: JSON.stringify(body),
    }),
  actualizarTipoHabitacion: (id: number, body: TipoHabitacionRequest) =>
    request<TipoHabitacion>(`${BASE}/tipos-habitacion/${id}`, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  eliminarTipoHabitacion: (id: number) =>
    request<void>(`${BASE}/tipos-habitacion/${id}`, { method: "DELETE" }),

  // Habitaciones
  listarHabitaciones: (params: BuscarHabitacionesParams = {}) =>
    request<Habitacion[]>(`${BASE}/habitaciones${buildQuery(params)}`),
  obtenerHabitacion: (id: number) =>
    request<Habitacion>(`${BASE}/habitaciones/${id}`),
  obtenerTarifaVigente: (id: number) =>
    request<Tarifa>(`${BASE}/habitaciones/${id}/tarifa-vigente`),
  crearHabitacion: (body: HabitacionRequest) =>
    request<Habitacion>(`${BASE}/habitaciones`, {
      method: "POST",
      body: JSON.stringify(body),
    }),
  actualizarHabitacion: (id: number, body: HabitacionRequest) =>
    request<Habitacion>(`${BASE}/habitaciones/${id}`, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  eliminarHabitacion: (id: number) =>
    request<void>(`${BASE}/habitaciones/${id}`, { method: "DELETE" }),

  // Tarifas
  listarTarifas: () => request<Tarifa[]>(`${BASE}/tarifas`),
  obtenerTarifa: (id: number) => request<Tarifa>(`${BASE}/tarifas/${id}`),
  crearTarifa: (body: TarifaRequest) =>
    request<Tarifa>(`${BASE}/tarifas`, {
      method: "POST",
      body: JSON.stringify(body),
    }),
  eliminarTarifa: (id: number) =>
    request<void>(`${BASE}/tarifas/${id}`, { method: "DELETE" }),
};
