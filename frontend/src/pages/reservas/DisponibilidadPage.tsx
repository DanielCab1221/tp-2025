import { useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { reservasSvc } from "../../api/reservasSvc";
import { useToast } from "../../lib/toast";
import { AMENITIES, type Amenity } from "../../types/gestionSvc";
import type {
  BuscarDisponibilidadParams,
  Habitacion,
} from "../../types/reservasSvc";
import { DataTable } from "../../components/DataTable";
import {
  btnPrimary,
  btnSecondary,
  ErrorMessage,
  FormField,
  inputClass,
  Modal,
  PageHeader,
  Spinner,
} from "../../components/ui";

export function DisponibilidadPage() {
  const navigate = useNavigate();
  const [filtros, setFiltros] = useState<BuscarDisponibilidadParams>({
    checkIn: "",
    checkOut: "",
  });
  const [amenitiesSel, setAmenitiesSel] = useState<Amenity[]>([]);
  const [buscar, setBuscar] = useState<BuscarDisponibilidadParams | null>(null);

  const query = useQuery({
    queryKey: ["disponibilidad", buscar],
    queryFn: () =>
      reservasSvc.buscarDisponibles(buscar as BuscarDisponibilidadParams),
    enabled: buscar !== null,
  });

  function toggleAmenity(a: Amenity) {
    setAmenitiesSel((prev) =>
      prev.includes(a) ? prev.filter((x) => x !== a) : [...prev, a],
    );
  }

  function buscarDisponibilidad() {
    setBuscar({
      ...filtros,
      amenities: amenitiesSel.length ? amenitiesSel : undefined,
    });
  }

  const [reservando, setReservando] = useState<Habitacion | null>(null);

  return (
    <div>
      <PageHeader
        title="Buscar disponibilidad"
        description="Endpoint central del TP: filtra por fechas, precio, capacidad, categoría, amenities y distancia (reservas-svc)."
      />

      <div className="mb-4 rounded-lg border border-gray-200 p-4 dark:border-gray-800">
        <div className="grid grid-cols-2 gap-x-3 sm:grid-cols-4">
          <FormField label="Check-in *">
            <input
              type="date"
              className={inputClass}
              value={filtros.checkIn}
              onChange={(e) =>
                setFiltros({ ...filtros, checkIn: e.target.value })
              }
            />
          </FormField>
          <FormField label="Check-out *">
            <input
              type="date"
              className={inputClass}
              value={filtros.checkOut}
              onChange={(e) =>
                setFiltros({ ...filtros, checkOut: e.target.value })
              }
            />
          </FormField>
          <FormField label="Huéspedes">
            <input
              type="number"
              className={inputClass}
              value={filtros.huespedes ?? ""}
              onChange={(e) =>
                setFiltros({
                  ...filtros,
                  huespedes: e.target.value
                    ? Number(e.target.value)
                    : undefined,
                })
              }
            />
          </FormField>
          <FormField label="Categoría mínima">
            <input
              type="number"
              className={inputClass}
              value={filtros.categoriaMinima ?? ""}
              onChange={(e) =>
                setFiltros({
                  ...filtros,
                  categoriaMinima: e.target.value
                    ? Number(e.target.value)
                    : undefined,
                })
              }
            />
          </FormField>
          <FormField label="Precio mín.">
            <input
              type="number"
              className={inputClass}
              value={filtros.precioMin ?? ""}
              onChange={(e) =>
                setFiltros({
                  ...filtros,
                  precioMin: e.target.value
                    ? Number(e.target.value)
                    : undefined,
                })
              }
            />
          </FormField>
          <FormField label="Precio máx.">
            <input
              type="number"
              className={inputClass}
              value={filtros.precioMax ?? ""}
              onChange={(e) =>
                setFiltros({
                  ...filtros,
                  precioMax: e.target.value
                    ? Number(e.target.value)
                    : undefined,
                })
              }
            />
          </FormField>
          <FormField label="Latitud">
            <input
              className={inputClass}
              value={filtros.latitud ?? ""}
              onChange={(e) =>
                setFiltros({
                  ...filtros,
                  latitud: e.target.value ? Number(e.target.value) : undefined,
                })
              }
            />
          </FormField>
          <FormField label="Longitud">
            <input
              className={inputClass}
              value={filtros.longitud ?? ""}
              onChange={(e) =>
                setFiltros({
                  ...filtros,
                  longitud: e.target.value ? Number(e.target.value) : undefined,
                })
              }
            />
          </FormField>
        </div>
        <div className="mb-3">
          <span className="mb-1 block text-sm font-medium text-gray-700 dark:text-gray-300">
            Amenities
          </span>
          <div className="flex flex-wrap gap-2">
            {AMENITIES.map((a) => (
              <button
                key={a}
                type="button"
                onClick={() => toggleAmenity(a)}
                className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                  amenitiesSel.includes(a)
                    ? "bg-indigo-600 text-white"
                    : "bg-gray-100 text-gray-600 hover:bg-gray-200 dark:bg-gray-800 dark:text-gray-300"
                }`}
              >
                {a}
              </button>
            ))}
          </div>
        </div>
        <button
          className={btnPrimary}
          disabled={!filtros.checkIn || !filtros.checkOut}
          onClick={buscarDisponibilidad}
        >
          Buscar
        </button>
      </div>

      {query.isLoading && <Spinner />}
      <ErrorMessage error={query.error} />
      {query.data && (
        <DataTable
          columns={[
            { header: "Hotel", render: (h) => h.hotel?.nombre ?? "—" },
            {
              header: "Categoría",
              render: (h) =>
                h.hotel?.categoria ? "★".repeat(h.hotel.categoria) : "—",
            },
            { header: "Tipo", render: (h) => h.tipoHabitacion },
            { header: "Capacidad", render: (h) => h.capacidad },
            {
              header: "Precio/noche",
              render: (h) =>
                `$${h.precioNoche?.toLocaleString("es-AR") ?? "—"}`,
            },
            {
              header: "Amenities",
              render: (h) => h.amenities?.join(", ") || "—",
            },
            {
              header: "",
              className: "text-right",
              render: (h) => (
                <button className={btnPrimary} onClick={() => setReservando(h)}>
                  Reservar
                </button>
              ),
            },
          ]}
          rows={query.data}
          keyFn={(h) => h.id}
          emptyMessage="No hay habitaciones disponibles para esos filtros"
        />
      )}

      {reservando && (
        <ReservarModal
          habitacion={reservando}
          checkIn={filtros.checkIn}
          checkOut={filtros.checkOut}
          onClose={() => setReservando(null)}
          onCreated={(reservaId) => navigate(`/reservas/${reservaId}`)}
        />
      )}
    </div>
  );
}

function ReservarModal({
  habitacion,
  checkIn,
  checkOut,
  onClose,
  onCreated,
}: {
  habitacion: Habitacion;
  checkIn: string;
  checkOut: string;
  onClose: () => void;
  onCreated: (reservaId: string) => void;
}) {
  const [form, setForm] = useState({
    idUsuario: "",
    nombreApellido: "",
    email: "",
  });
  const [error, setError] = useState<unknown>(null);
  const toast = useToast();

  const crear = useMutation({
    mutationFn: () =>
      reservasSvc.crearReserva({
        idHabitacion: habitacion.id,
        checkIn: `${checkIn}T00:00:00Z`,
        checkOut: `${checkOut}T00:00:00Z`,
        huesped: form,
      }),
    onSuccess: (reserva) => {
      toast.success("Reserva creada");
      onCreated(reserva._id);
    },
    onError: setError,
  });

  return (
    <Modal
      open
      onClose={onClose}
      title={`Reservar · ${habitacion.hotel?.nombre} (${habitacion.tipoHabitacion})`}
    >
      <ErrorMessage error={error} />
      <p className="mb-3 text-sm text-gray-500">
        {checkIn} → {checkOut} · $
        {habitacion.precioNoche?.toLocaleString("es-AR")}/noche
      </p>
      <FormField
        label="ID de usuario (user-svc)"
        hint="El id numérico del huésped, como string"
      >
        <input
          className={inputClass}
          value={form.idUsuario}
          onChange={(e) => setForm({ ...form, idUsuario: e.target.value })}
        />
      </FormField>
      <FormField label="Nombre y apellido">
        <input
          className={inputClass}
          value={form.nombreApellido}
          onChange={(e) => setForm({ ...form, nombreApellido: e.target.value })}
        />
      </FormField>
      <FormField label="Email">
        <input
          className={inputClass}
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
        />
      </FormField>
      <div className="mt-4 flex justify-end gap-2">
        <button className={btnSecondary} onClick={onClose}>
          Cancelar
        </button>
        <button
          className={btnPrimary}
          disabled={!form.idUsuario || !form.nombreApellido || crear.isPending}
          onClick={() => crear.mutate()}
        >
          Confirmar reserva
        </button>
      </div>
    </Modal>
  );
}
