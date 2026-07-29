import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useNavigate, useParams } from "react-router-dom";
import { gestionSvc } from "../../api/gestionSvc";
import { ApiError } from "../../lib/http";
import {
  btnDanger,
  btnPrimary,
  ErrorMessage,
  FormField,
  inputClass,
  PageHeader,
  Spinner,
} from "../../components/ui";

export function HabitacionDetailPage() {
  const { id } = useParams<{ id: string }>();
  const habitacionId = Number(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const query = useQuery({
    queryKey: ["habitacion", habitacionId],
    queryFn: () => gestionSvc.obtenerHabitacion(habitacionId),
  });
  const tarifaQuery = useQuery({
    queryKey: ["tarifa-vigente", habitacionId],
    queryFn: () => gestionSvc.obtenerTarifaVigente(habitacionId),
    retry: false,
  });

  const [form, setForm] = useState({ numero: "", piso: "", disponible: true });
  const [synced, setSynced] = useState(false);
  if (query.data && !synced) {
    setForm({
      numero: String(query.data.numero),
      piso: String(query.data.piso),
      disponible: query.data.disponible ?? true,
    });
    setSynced(true);
  }
  const [error, setError] = useState<unknown>(null);

  const actualizar = useMutation({
    mutationFn: () => {
      if (!query.data) throw new Error("no data");
      return gestionSvc.actualizarHabitacion(habitacionId, {
        numero: Number(form.numero),
        piso: Number(form.piso),
        disponible: form.disponible,
        hotel: { id: query.data.hotel.id },
        tipoHabitacion: { id: query.data.tipoHabitacion.id },
      });
    },
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ["habitacion", habitacionId] }),
    onError: setError,
  });

  const eliminar = useMutation({
    mutationFn: () => gestionSvc.eliminarHabitacion(habitacionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["habitaciones"] });
      navigate("/gestion/habitaciones");
    },
  });

  if (query.isLoading) return <Spinner />;
  if (!query.data) return <ErrorMessage error={query.error} />;
  const h = query.data;
  const sinTarifa =
    tarifaQuery.error instanceof ApiError && tarifaQuery.error.status === 404;

  return (
    <div>
      <PageHeader
        title={`Habitación ${h.numero} · Piso ${h.piso}`}
        description={`${h.hotel.nombre} · ${h.tipoHabitacion.nombre}`}
        action={
          <button
            className={btnDanger}
            disabled={eliminar.isPending}
            onClick={() => {
              if (confirm("¿Borrar esta habitación?")) eliminar.mutate();
            }}
          >
            Borrar
          </button>
        }
      />

      <div className="mb-6 rounded-lg border border-gray-200 p-4 dark:border-gray-800">
        <h3 className="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-300">
          Datos
        </h3>
        <ErrorMessage error={error} />
        <div className="grid grid-cols-2 gap-x-3">
          <FormField label="Número">
            <input
              type="number"
              className={inputClass}
              value={form.numero}
              onChange={(e) => setForm({ ...form, numero: e.target.value })}
            />
          </FormField>
          <FormField label="Piso">
            <input
              type="number"
              className={inputClass}
              value={form.piso}
              onChange={(e) => setForm({ ...form, piso: e.target.value })}
            />
          </FormField>
        </div>
        <label className="mb-3 flex items-center gap-2 text-sm text-gray-700 dark:text-gray-300">
          <input
            type="checkbox"
            checked={form.disponible}
            onChange={(e) => setForm({ ...form, disponible: e.target.checked })}
          />
          Disponible
        </label>
        <button
          className={btnPrimary}
          disabled={actualizar.isPending}
          onClick={() => actualizar.mutate()}
        >
          Guardar cambios
        </button>
      </div>

      <div className="rounded-lg border border-gray-200 p-4 dark:border-gray-800">
        <h3 className="mb-2 text-sm font-semibold text-gray-700 dark:text-gray-300">
          Tarifa vigente
        </h3>
        {tarifaQuery.isLoading && <Spinner />}
        {sinTarifa && (
          <p className="text-sm text-gray-500">
            Este tipo de habitación no tiene tarifa vigente.
          </p>
        )}
        {tarifaQuery.data && (
          <p className="text-lg font-semibold text-gray-900 dark:text-gray-100">
            ${tarifaQuery.data.precioNoche.toLocaleString("es-AR")} / noche
            <span className="ml-2 text-xs font-normal text-gray-400">
              desde {tarifaQuery.data.fechaInicio}
              {tarifaQuery.data.fechaFin
                ? ` hasta ${tarifaQuery.data.fechaFin}`
                : " (continua)"}
            </span>
          </p>
        )}
      </div>
    </div>
  );
}
