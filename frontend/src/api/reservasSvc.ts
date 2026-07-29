import { buildQuery, request } from "../lib/http";
import type {
  BuscarDisponibilidadParams,
  CrearReservaRequest,
  Habitacion,
  Pago,
  RegistrarPagoRequest,
  Reserva,
  ReviewRequest,
} from "../types/reservasSvc";

const BASE = import.meta.env.VITE_RESERVAS_SVC_URL;

export const reservasSvc = {
  listarHabitaciones: () => request<Habitacion[]>(`${BASE}/habitaciones`),
  obtenerHabitacion: (id: string) =>
    request<Habitacion>(`${BASE}/habitaciones/${id}`),
  buscarDisponibles: (params: BuscarDisponibilidadParams) =>
    request<Habitacion[]>(
      `${BASE}/habitaciones/disponibles${buildQuery(params)}`,
    ),

  listarReservas: (idUsuario?: string) =>
    request<Reserva[]>(`${BASE}/reservas${buildQuery({ idUsuario })}`),
  obtenerReserva: (id: string) => request<Reserva>(`${BASE}/reservas/${id}`),
  crearReserva: (body: CrearReservaRequest) =>
    request<Reserva>(`${BASE}/reservas`, {
      method: "POST",
      body: JSON.stringify(body),
    }),
  crearBloqueo: (idHabitacion: string, checkIn: string, checkOut: string) =>
    request<Reserva>(
      `${BASE}/reservas/bloqueos${buildQuery({ idHabitacion, checkIn, checkOut })}`,
      { method: "POST" },
    ),
  cancelar: (id: string) =>
    request<Reserva>(`${BASE}/reservas/${id}/cancelar`, { method: "PATCH" }),
  checkin: (id: string) =>
    request<Reserva>(`${BASE}/reservas/${id}/checkin`, { method: "PATCH" }),
  checkout: (id: string) =>
    request<Reserva>(`${BASE}/reservas/${id}/checkout`, { method: "PATCH" }),
  registrarPago: (id: string, body: RegistrarPagoRequest) =>
    request<Reserva>(`${BASE}/reservas/${id}/pagos`, {
      method: "POST",
      body: JSON.stringify(body),
    }),
  listarPagos: (id: string) => request<Pago[]>(`${BASE}/reservas/${id}/pagos`),
  reviewCliente: (id: string, body: ReviewRequest) =>
    request<Reserva>(`${BASE}/reservas/${id}/review-cliente`, {
      method: "POST",
      body: JSON.stringify(body),
    }),
  reviewHotel: (id: string, body: ReviewRequest) =>
    request<Reserva>(`${BASE}/reservas/${id}/review-hotel`, {
      method: "POST",
      body: JSON.stringify(body),
    }),
};
