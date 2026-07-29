import { buildQuery, request } from "../lib/http";
import type {
  ActualizarHuespedRequest,
  ActualizarPropietarioRequest,
  Banco,
  BancoRequest,
  CrearHuespedRequest,
  CrearPropietarioRequest,
  Huesped,
  Page,
  Propietario,
  TarjetaCredito,
  TarjetaCreditoRequest,
  Usuario,
} from "../types/userSvc";

const BASE = import.meta.env.VITE_USER_SVC_URL;

export const userSvc = {
  buscarUsuarios: (nombre: string, page = 0, size = 10) =>
    request<Page<Usuario>>(
      `${BASE}/users${buildQuery({ nombre, page, size })}`,
    ),
  buscarPorDni: (dni: string, page = 0, size = 10) =>
    request<Page<Usuario>>(
      `${BASE}/users/buscar-dni${buildQuery({ dni, page, size })}`,
    ),
  obtenerPorDniExacto: (dni: string) =>
    request<Usuario>(`${BASE}/users/dni/${dni}`),
  crearHuesped: (body: CrearHuespedRequest) =>
    request<void>(`${BASE}/users/huesped`, {
      method: "POST",
      body: JSON.stringify(body),
    }),
  crearPropietario: (body: CrearPropietarioRequest) =>
    request<void>(`${BASE}/users/propietario`, {
      method: "POST",
      body: JSON.stringify(body),
    }),
  actualizarHuesped: (id: number, body: ActualizarHuespedRequest) =>
    request<Huesped>(`${BASE}/users/huesped/${id}`, {
      method: "PATCH",
      body: JSON.stringify(body),
    }),
  eliminarHuesped: (id: number) =>
    request<void>(`${BASE}/users/huesped/${id}`, { method: "DELETE" }),
  actualizarPropietario: (id: number, body: ActualizarPropietarioRequest) =>
    request<Propietario>(`${BASE}/users/propietario/${id}`, {
      method: "PATCH",
      body: JSON.stringify(body),
    }),

  listarTarjetas: (huespedId: number) =>
    request<TarjetaCredito[]>(`${BASE}/tarjetas/huesped/${huespedId}`),
  agregarTarjeta: (huespedId: number, body: TarjetaCreditoRequest) =>
    request<TarjetaCredito>(`${BASE}/tarjetas/huesped/${huespedId}`, {
      method: "POST",
      body: JSON.stringify(body),
    }),
  eliminarTarjeta: (tarjetaId: number) =>
    request<void>(`${BASE}/tarjetas/${tarjetaId}`, { method: "DELETE" }),
  cambiarTarjetaPrincipal: (huespedId: number, tarjetaId: number) =>
    request<TarjetaCredito>(
      `${BASE}/tarjetas/huesped/${huespedId}/principal/${tarjetaId}`,
      { method: "PATCH" },
    ),

  listarBancos: () => request<Banco[]>(`${BASE}/bancos`),
  obtenerBanco: (id: number) => request<Banco>(`${BASE}/bancos/${id}`),
  crearBanco: (body: BancoRequest) =>
    request<Banco>(`${BASE}/bancos`, {
      method: "POST",
      body: JSON.stringify(body),
    }),
  actualizarBanco: (id: number, body: BancoRequest) =>
    request<Banco>(`${BASE}/bancos/${id}`, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  eliminarBanco: (id: number) =>
    request<void>(`${BASE}/bancos/${id}`, { method: "DELETE" }),
};
