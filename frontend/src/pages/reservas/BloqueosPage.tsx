import { useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { reservasSvc } from "../../api/reservasSvc";
import { DataTable } from "../../components/DataTable";
import {
  btnPrimary,
  ErrorMessage,
  FormField,
  inputClass,
  PageHeader,
  Spinner,
} from "../../components/ui";

export function BloqueosPage() {
  const navigate = useNavigate();
  const habitacionesQuery = useQuery({
    queryKey: ["habitaciones-reservas"],
    queryFn: reservasSvc.listarHabitaciones,
  });
  const [form, setForm] = useState({
    idHabitacion: "",
    checkIn: "",
    checkOut: "",
  });
  const [error, setError] = useState<unknown>(null);

  const crear = useMutation({
    mutationFn: () =>
      reservasSvc.crearBloqueo(form.idHabitacion, form.checkIn, form.checkOut),
    onSuccess: (reserva) => navigate(`/reservas/${reserva._id}`),
    onError: setError,
  });

  return (
    <div>
      <PageHeader
        title="Bloqueos administrativos"
        description="Bloquea una habitación sin huésped ni pago (ej. mantenimiento). Se libera cancelando la reserva BLOQUEADA."
      />

      <div className="mb-6 max-w-md rounded-lg border border-gray-200 p-4 dark:border-gray-800">
        <ErrorMessage error={error} />
        <FormField label="Habitación">
          <select
            className={inputClass}
            value={form.idHabitacion}
            onChange={(e) => setForm({ ...form, idHabitacion: e.target.value })}
          >
            <option value="">Elegir…</option>
            {habitacionesQuery.data?.map((h) => (
              <option key={h.id} value={h.id}>
                {h.hotel?.nombre} · {h.tipoHabitacion} (hab. física #
                {h.habitacionId})
              </option>
            ))}
          </select>
        </FormField>
        <div className="grid grid-cols-2 gap-x-3">
          <FormField label="Check-in">
            <input
              type="date"
              className={inputClass}
              value={form.checkIn}
              onChange={(e) => setForm({ ...form, checkIn: e.target.value })}
            />
          </FormField>
          <FormField label="Check-out">
            <input
              type="date"
              className={inputClass}
              value={form.checkOut}
              onChange={(e) => setForm({ ...form, checkOut: e.target.value })}
            />
          </FormField>
        </div>
        <button
          className={btnPrimary}
          disabled={
            !form.idHabitacion ||
            !form.checkIn ||
            !form.checkOut ||
            crear.isPending
          }
          onClick={() => crear.mutate()}
        >
          Crear bloqueo
        </button>
      </div>

      {habitacionesQuery.isLoading && <Spinner />}
      {habitacionesQuery.data && (
        <>
          <h3 className="mb-2 text-sm font-semibold text-gray-700 dark:text-gray-300">
            Habitaciones (referencia)
          </h3>
          <DataTable
            columns={[
              { header: "Hotel", render: (h) => h.hotel?.nombre ?? "—" },
              { header: "Tipo", render: (h) => h.tipoHabitacion },
              {
                header: "Disponible",
                render: (h) => (h.disponible ? "Sí" : "No"),
              },
            ]}
            rows={habitacionesQuery.data}
            keyFn={(h) => h.id}
          />
        </>
      )}
    </div>
  );
}
