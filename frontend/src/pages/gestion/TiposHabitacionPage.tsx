import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { gestionSvc } from "../../api/gestionSvc";
import { useConfirm } from "../../lib/confirm";
import { useToast } from "../../lib/toast";
import type { TipoHabitacion } from "../../types/gestionSvc";
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

export function TiposHabitacionPage() {
  const queryClient = useQueryClient();
  const toast = useToast();
  const confirm = useConfirm();
  const query = useQuery({
    queryKey: ["tipos-habitacion"],
    queryFn: gestionSvc.listarTiposHabitacion,
  });
  const [busqueda, setBusqueda] = useState("");
  const tiposFiltrados = (query.data ?? []).filter((t) => {
    const q = busqueda.toLowerCase();
    return (
      t.nombre?.toLowerCase().includes(q) ||
      t.descripcion?.toLowerCase().includes(q)
    );
  });
  const [editing, setEditing] = useState<TipoHabitacion | "new" | null>(null);
  const [form, setForm] = useState({
    id: "",
    nombre: "",
    descripcion: "",
    capacidad: "",
  });
  const [error, setError] = useState<unknown>(null);

  const invalidate = () =>
    queryClient.invalidateQueries({ queryKey: ["tipos-habitacion"] });

  const crear = useMutation({
    mutationFn: () =>
      gestionSvc.crearTipoHabitacion({
        id: Number(form.id),
        nombre: form.nombre,
        descripcion: form.descripcion,
        capacidad: form.capacidad ? Number(form.capacidad) : null,
      }),
    onSuccess: () => {
      invalidate();
      setEditing(null);
      toast.success("Tipo de habitación creado");
    },
    onError: setError,
  });
  const actualizar = useMutation({
    mutationFn: (id: number) =>
      gestionSvc.actualizarTipoHabitacion(id, {
        nombre: form.nombre,
        descripcion: form.descripcion,
        capacidad: form.capacidad ? Number(form.capacidad) : null,
      }),
    onSuccess: () => {
      invalidate();
      setEditing(null);
      toast.success("Tipo de habitación actualizado");
    },
    onError: setError,
  });
  const eliminar = useMutation({
    mutationFn: (id: number) => gestionSvc.eliminarTipoHabitacion(id),
    onSuccess: () => {
      invalidate();
      toast.success("Tipo de habitación eliminado");
    },
    onError: setError,
  });

  function openNew() {
    setForm({ id: "", nombre: "", descripcion: "", capacidad: "" });
    setError(null);
    setEditing("new");
  }
  function openEdit(tipo: TipoHabitacion) {
    setForm({
      id: String(tipo.id),
      nombre: tipo.nombre ?? "",
      descripcion: tipo.descripcion ?? "",
      capacidad: tipo.capacidad?.toString() ?? "",
    });
    setError(null);
    setEditing(tipo);
  }

  return (
    <div>
      <PageHeader
        title="Tipos de Habitación"
        description="Catálogo fijo (gestion-svc). El id se asigna a mano al crear, no es autogenerado."
        action={
          <button className={btnPrimary} onClick={openNew}>
            + Nuevo tipo
          </button>
        }
      />
      <div className="mb-4 max-w-xs">
        <FormField label="Buscar">
          <input
            className={inputClass}
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            placeholder="Nombre o descripción"
          />
        </FormField>
      </div>
      <ErrorMessage error={eliminar.error} />
      {query.isLoading && <Spinner />}
      {query.data && (
        <DataTable
          columns={[
            { header: "ID", render: (t) => t.id },
            { header: "Nombre", render: (t) => t.nombre },
            { header: "Descripción", render: (t) => t.descripcion },
            { header: "Capacidad", render: (t) => t.capacidad },
            {
              header: "",
              className: "text-right",
              render: (t) => (
                <div className="flex justify-end gap-2">
                  <button className={btnSecondary} onClick={() => openEdit(t)}>
                    Editar
                  </button>
                  <button
                    className={btnDanger}
                    disabled={eliminar.isPending}
                    onClick={async () => {
                      if (
                        await confirm(
                          `¿Borrar el tipo "${t.nombre}"? Fallará si hay habitaciones/tarifas que lo usan.`,
                          { confirmLabel: "Borrar" },
                        )
                      )
                        eliminar.mutate(t.id);
                    }}
                  >
                    Borrar
                  </button>
                </div>
              ),
            },
          ]}
          rows={tiposFiltrados}
          keyFn={(t) => t.id}
          emptyMessage="Sin tipos de habitación para esta búsqueda"
        />
      )}

      <Modal
        open={editing !== null}
        onClose={() => setEditing(null)}
        title={
          editing === "new"
            ? "Nuevo tipo de habitación"
            : "Editar tipo de habitación"
        }
      >
        <ErrorMessage error={error} />
        {editing === "new" && (
          <FormField
            label="ID"
            hint="Numérico, elegido a mano (no autogenerado)"
          >
            <input
              className={inputClass}
              value={form.id}
              onChange={(e) => setForm({ ...form, id: e.target.value })}
            />
          </FormField>
        )}
        <FormField label="Nombre">
          <input
            className={inputClass}
            value={form.nombre}
            onChange={(e) => setForm({ ...form, nombre: e.target.value })}
          />
        </FormField>
        <FormField label="Descripción">
          <input
            className={inputClass}
            value={form.descripcion}
            onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
          />
        </FormField>
        <FormField label="Capacidad">
          <input
            type="number"
            className={inputClass}
            value={form.capacidad}
            onChange={(e) => setForm({ ...form, capacidad: e.target.value })}
          />
        </FormField>
        <div className="mt-4 flex justify-end gap-2">
          <button className={btnSecondary} onClick={() => setEditing(null)}>
            Cancelar
          </button>
          <button
            className={btnPrimary}
            disabled={
              !form.nombre ||
              (editing === "new" && !form.id) ||
              crear.isPending ||
              actualizar.isPending
            }
            onClick={() =>
              editing === "new"
                ? crear.mutate()
                : actualizar.mutate((editing as TipoHabitacion).id)
            }
          >
            Guardar
          </button>
        </div>
      </Modal>
    </div>
  );
}
