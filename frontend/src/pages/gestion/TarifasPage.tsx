import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { gestionSvc } from "../../api/gestionSvc";
import { useConfirm } from "../../lib/confirm";
import { useToast } from "../../lib/toast";
import { DataTable } from "../../components/DataTable";
import {
  btnDanger,
  btnPrimary,
  btnSecondary,
  ErrorMessage,
  FormField,
  inputClass,
  Modal,
  PageHeader,
  Spinner,
} from "../../components/ui";

export function TarifasPage() {
  const queryClient = useQueryClient();
  const toast = useToast();
  const confirm = useConfirm();
  const [busqueda, setBusqueda] = useState("");
  const query = useQuery({
    queryKey: ["tarifas"],
    queryFn: gestionSvc.listarTarifas,
  });
  const tarifasFiltradas = (query.data ?? []).filter((t) =>
    t.tipoHabitacion.nombre?.toLowerCase().includes(busqueda.toLowerCase()),
  );
  const tiposQuery = useQuery({
    queryKey: ["tipos-habitacion"],
    queryFn: gestionSvc.listarTiposHabitacion,
  });

  const invalidate = () =>
    queryClient.invalidateQueries({ queryKey: ["tarifas"] });

  const eliminar = useMutation({
    mutationFn: (id: number) => gestionSvc.eliminarTarifa(id),
    onSuccess: () => {
      invalidate();
      toast.success("Tarifa eliminada");
    },
  });

  const [creating, setCreating] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const [form, setForm] = useState({
    tipoHabitacionId: "",
    fechaInicio: "",
    fechaFin: "",
    precioNoche: "",
  });

  const crear = useMutation({
    mutationFn: () =>
      gestionSvc.crearTarifa({
        tipoHabitacion: { id: Number(form.tipoHabitacionId) },
        fechaInicio: form.fechaInicio || undefined,
        fechaFin: form.fechaFin || undefined,
        precioNoche: Number(form.precioNoche),
      }),
    onSuccess: () => {
      invalidate();
      setCreating(false);
      toast.success("Tarifa creada");
    },
    onError: setError,
  });

  function openNew() {
    setForm({
      tipoHabitacionId: "",
      fechaInicio: "",
      fechaFin: "",
      precioNoche: "",
    });
    setError(null);
    setCreating(true);
  }

  return (
    <div>
      <PageHeader
        title="Tarifas"
        description="Historial de precios por tipo de habitación (gestion-svc). Sin fecha de fin = tarifa continua."
        action={
          <button className={btnPrimary} onClick={openNew}>
            + Nueva tarifa
          </button>
        }
      />
      <div className="mb-4 max-w-xs">
        <FormField label="Buscar por tipo de habitación">
          <input
            className={inputClass}
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            placeholder="ej: SINGLE"
          />
        </FormField>
      </div>
      <ErrorMessage error={eliminar.error} />
      {query.isLoading && <Spinner />}
      {query.data && (
        <DataTable
          columns={[
            { header: "Tipo", render: (t) => t.tipoHabitacion.nombre },
            {
              header: "Precio/noche",
              render: (t) => `$${t.precioNoche.toLocaleString("es-AR")}`,
            },
            { header: "Desde", render: (t) => t.fechaInicio },
            { header: "Hasta", render: (t) => t.fechaFin ?? "Continua" },
            {
              header: "",
              className: "text-right",
              render: (t) => (
                <button
                  className={btnDanger}
                  disabled={eliminar.isPending}
                  onClick={async () => {
                    if (
                      await confirm("¿Borrar esta tarifa?", {
                        confirmLabel: "Borrar",
                      })
                    )
                      eliminar.mutate(t.id);
                  }}
                >
                  Borrar
                </button>
              ),
            },
          ]}
          rows={tarifasFiltradas}
          keyFn={(t) => t.id}
          emptyMessage="Sin tarifas para esta búsqueda"
        />
      )}

      <Modal
        open={creating}
        onClose={() => setCreating(false)}
        title="Nueva tarifa"
      >
        <ErrorMessage error={error} />
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
        <div className="grid grid-cols-2 gap-x-3">
          <FormField label="Fecha inicio" hint="Vacío = hoy">
            <input
              type="date"
              className={inputClass}
              value={form.fechaInicio}
              onChange={(e) =>
                setForm({ ...form, fechaInicio: e.target.value })
              }
            />
          </FormField>
          <FormField label="Fecha fin" hint="Vacío = continua">
            <input
              type="date"
              className={inputClass}
              value={form.fechaFin}
              onChange={(e) => setForm({ ...form, fechaFin: e.target.value })}
            />
          </FormField>
        </div>
        <FormField label="Precio por noche">
          <input
            type="number"
            className={inputClass}
            value={form.precioNoche}
            onChange={(e) => setForm({ ...form, precioNoche: e.target.value })}
          />
        </FormField>
        <div className="mt-4 flex justify-end gap-2">
          <button className={btnSecondary} onClick={() => setCreating(false)}>
            Cancelar
          </button>
          <button
            className={btnPrimary}
            disabled={
              !form.tipoHabitacionId || !form.precioNoche || crear.isPending
            }
            onClick={() => crear.mutate()}
          >
            Crear tarifa
          </button>
        </div>
      </Modal>
    </div>
  );
}
