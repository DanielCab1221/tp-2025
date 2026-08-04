import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { gestionSvc } from "../../api/gestionSvc";
import { useToast } from "../../lib/toast";
import type { BuscarHabitacionesParams } from "../../types/gestionSvc";
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

export function HabitacionesPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const toast = useToast();
  const [filtros, setFiltros] = useState<BuscarHabitacionesParams>({});
  const query = useQuery({
    queryKey: ["habitaciones", filtros],
    queryFn: () => gestionSvc.listarHabitaciones(filtros),
  });
  const hotelesQuery = useQuery({
    queryKey: ["hoteles", {}],
    queryFn: () => gestionSvc.listarHoteles(),
  });
  const tiposQuery = useQuery({
    queryKey: ["tipos-habitacion"],
    queryFn: gestionSvc.listarTiposHabitacion,
  });

  const [creating, setCreating] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const [form, setForm] = useState({
    numero: "",
    piso: "",
    hotelId: "",
    tipoHabitacionId: "",
    disponible: true,
  });

  const crear = useMutation({
    mutationFn: () =>
      gestionSvc.crearHabitacion({
        numero: Number(form.numero),
        piso: Number(form.piso),
        hotel: { id: Number(form.hotelId) },
        tipoHabitacion: { id: Number(form.tipoHabitacionId) },
        disponible: form.disponible,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["habitaciones"] });
      setCreating(false);
      toast.success("Habitación creada");
    },
    onError: setError,
  });

  function openNew() {
    setForm({
      numero: "",
      piso: "",
      hotelId: "",
      tipoHabitacionId: "",
      disponible: true,
    });
    setError(null);
    setCreating(true);
  }

  return (
    <div>
      <PageHeader
        title="Habitaciones"
        description="Habitaciones por hotel y tipo (gestion-svc)."
        action={
          <button className={btnPrimary} onClick={openNew}>
            + Nueva habitación
          </button>
        }
      />

      <div className="mb-4 flex flex-wrap items-end gap-2">
        <FormField label="Hotel">
          <select
            className={inputClass}
            value={filtros.hotelId ?? ""}
            onChange={(e) =>
              setFiltros({
                ...filtros,
                hotelId: e.target.value ? Number(e.target.value) : undefined,
              })
            }
          >
            <option value="">Todos</option>
            {hotelesQuery.data?.map((h) => (
              <option key={h.id} value={h.id}>
                {h.nombre}
              </option>
            ))}
          </select>
        </FormField>
        <FormField label="Tipo">
          <select
            className={inputClass}
            value={filtros.tipoHabitacionId ?? ""}
            onChange={(e) =>
              setFiltros({
                ...filtros,
                tipoHabitacionId: e.target.value
                  ? Number(e.target.value)
                  : undefined,
              })
            }
          >
            <option value="">Todos</option>
            {tiposQuery.data?.map((t) => (
              <option key={t.id} value={t.id}>
                {t.nombre}
              </option>
            ))}
          </select>
        </FormField>
        <FormField label="Disponible">
          <select
            className={inputClass}
            value={
              filtros.disponible === undefined ? "" : String(filtros.disponible)
            }
            onChange={(e) =>
              setFiltros({
                ...filtros,
                disponible:
                  e.target.value === "" ? undefined : e.target.value === "true",
              })
            }
          >
            <option value="">Cualquiera</option>
            <option value="true">Sí</option>
            <option value="false">No</option>
          </select>
        </FormField>
      </div>

      {query.isLoading && <Spinner />}
      <ErrorMessage error={query.error} />
      {query.data && (
        <DataTable
          columns={[
            { header: "Nro", render: (h) => h.numero },
            { header: "Piso", render: (h) => h.piso },
            { header: "Hotel", render: (h) => h.hotel.nombre },
            { header: "Tipo", render: (h) => h.tipoHabitacion.nombre },
            {
              header: "Disponible",
              render: (h) => (h.disponible ? "Sí" : "No"),
            },
          ]}
          rows={query.data}
          keyFn={(h) => h.id}
          onRowClick={(h) => navigate(`/gestion/habitaciones/${h.id}`)}
        />
      )}

      <Modal
        open={creating}
        onClose={() => setCreating(false)}
        title="Nueva habitación"
      >
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
          <FormField label="Hotel">
            <select
              className={inputClass}
              value={form.hotelId}
              onChange={(e) => setForm({ ...form, hotelId: e.target.value })}
            >
              <option value="">Elegir…</option>
              {hotelesQuery.data?.map((h) => (
                <option key={h.id} value={h.id}>
                  {h.nombre}
                </option>
              ))}
            </select>
          </FormField>
          <FormField label="Tipo de habitación">
            <select
              className={inputClass}
              value={form.tipoHabitacionId}
              onChange={(e) =>
                setForm({ ...form, tipoHabitacionId: e.target.value })
              }
            >
              <option value="">Elegir…</option>
              {tiposQuery.data?.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.nombre}
                </option>
              ))}
            </select>
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
        <div className="mt-4 flex justify-end gap-2">
          <button className={btnSecondary} onClick={() => setCreating(false)}>
            Cancelar
          </button>
          <button
            className={btnPrimary}
            disabled={
              !form.numero ||
              !form.piso ||
              !form.hotelId ||
              !form.tipoHabitacionId ||
              crear.isPending
            }
            onClick={() => crear.mutate()}
          >
            Crear habitación
          </button>
        </div>
      </Modal>
    </div>
  );
}
