import { useState } from "react";
import { useEventBus } from "../hooks/useEventBus";
import type { TipoEventoBus } from "../types/eventBus";

const TIPO_EVENTO_COLORS: Record<string, string> = {
  CREAR: "bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-300",
  ACTUALIZAR_DATOS:
    "bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-300",
  ACTUALIZAR_PRECIO:
    "bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300",
  ELIMINAR: "bg-red-100 text-red-800 dark:bg-red-900/40 dark:text-red-300",
};

const FILTROS: { label: string; value: TipoEventoBus | "TODOS" }[] = [
  { label: "Todos", value: "TODOS" },
  { label: "Crear", value: "CREAR" },
  { label: "Actualizar datos", value: "ACTUALIZAR_DATOS" },
  { label: "Actualizar precio", value: "ACTUALIZAR_PRECIO" },
  { label: "Eliminar", value: "ELIMINAR" },
];

const STATUS_COLOR: Record<string, string> = {
  conectando: "bg-gray-300 animate-pulse",
  conectado: "bg-green-500",
  desconectado: "bg-red-500",
};

function formatHora(iso: string) {
  return new Date(iso).toLocaleTimeString("es-AR", { hour12: false });
}

export function EventBusPanel() {
  const { eventos, status } = useEventBus();
  const [open, setOpen] = useState(false);
  const [visto, setVisto] = useState(0);
  const [filtro, setFiltro] = useState<TipoEventoBus | "TODOS">("TODOS");

  const sinLeer = open ? 0 : Math.max(eventos.length - visto, 0);
  const eventosFiltrados =
    filtro === "TODOS"
      ? eventos
      : eventos.filter((e) => e.tipoEvento === filtro);

  return (
    <>
      <button
        onClick={() => {
          setOpen((v) => !v);
          setVisto(eventos.length);
        }}
        className="fixed right-4 bottom-4 z-40 flex items-center gap-2 rounded-full bg-gray-900 px-4 py-2.5 text-sm font-medium text-white shadow-lg hover:bg-gray-800 dark:bg-indigo-600 dark:hover:bg-indigo-500"
      >
        <span className={`h-2 w-2 rounded-full ${STATUS_COLOR[status]}`} />
        Bus de eventos
        {sinLeer > 0 && (
          <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-red-500 px-1 text-xs">
            {sinLeer}
          </span>
        )}
      </button>

      {open && (
        <div className="fixed right-4 bottom-20 z-40 flex max-h-[70vh] w-96 flex-col rounded-lg border border-gray-200 bg-white shadow-xl dark:border-gray-800 dark:bg-gray-900">
          <div className="border-b border-gray-200 px-4 py-3 dark:border-gray-800">
            <div className="mb-2 flex items-center justify-between">
              <div>
                <h3 className="font-semibold text-gray-900 dark:text-gray-100">
                  Bus de eventos
                </h3>
                <p className="text-xs text-gray-400">
                  Mensajería async gestion-svc → reservas-svc (RabbitMQ)
                </p>
              </div>
              <button
                onClick={() => setOpen(false)}
                className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
                aria-label="Cerrar"
              >
                ✕
              </button>
            </div>
            <select
              value={filtro}
              onChange={(e) =>
                setFiltro(e.target.value as TipoEventoBus | "TODOS")
              }
              className="w-full rounded-md border border-gray-300 bg-white px-2 py-1 text-xs text-gray-700 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-200"
            >
              {FILTROS.map((f) => (
                <option key={f.value} value={f.value}>
                  {f.label}
                </option>
              ))}
            </select>
          </div>

          <div className="flex-1 overflow-y-auto p-3">
            {eventosFiltrados.length === 0 ? (
              <p className="py-6 text-center text-sm text-gray-400">
                {eventos.length === 0
                  ? "Todavía no se recibió ningún evento. Probá crear o editar algo en Gestión Hotelera."
                  : "Ningún evento coincide con el filtro seleccionado."}
              </p>
            ) : (
              <ul className="space-y-2">
                {eventosFiltrados.map((evento, i) => (
                  <li
                    key={`${evento.timestamp}-${i}`}
                    className="rounded-md border border-gray-100 bg-gray-50 p-2.5 text-sm dark:border-gray-800 dark:bg-gray-800/50"
                  >
                    <div className="mb-1 flex items-center justify-between gap-2">
                      <span
                        className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                          TIPO_EVENTO_COLORS[evento.tipoEvento] ??
                          "bg-gray-100 text-gray-700"
                        }`}
                      >
                        {evento.tipoEvento}
                      </span>
                      <span className="text-xs text-gray-400">
                        {formatHora(evento.timestamp)}
                      </span>
                    </div>
                    <p className="mb-1 text-xs text-gray-400">
                      {evento.origen} → {evento.destino}
                    </p>
                    <p className="text-gray-700 dark:text-gray-200">
                      {evento.resumen}
                    </p>
                    <details className="mt-1">
                      <summary className="cursor-pointer text-xs text-indigo-600 dark:text-indigo-400">
                        Ver payload
                      </summary>
                      <pre className="mt-1 overflow-x-auto rounded bg-gray-900 p-2 text-xs text-gray-100">
                        {JSON.stringify(evento.payload, null, 2)}
                      </pre>
                    </details>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      )}
    </>
  );
}
