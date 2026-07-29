import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { userSvc } from "../../api/userSvc";
import type { Banco } from "../../types/userSvc";
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

export function BancosPage() {
  const queryClient = useQueryClient();
  const bancosQuery = useQuery({
    queryKey: ["bancos"],
    queryFn: userSvc.listarBancos,
  });
  const [editing, setEditing] = useState<Banco | "new" | null>(null);
  const [nombre, setNombre] = useState("");
  const [error, setError] = useState<unknown>(null);

  const invalidate = () =>
    queryClient.invalidateQueries({ queryKey: ["bancos"] });

  const crear = useMutation({
    mutationFn: () => userSvc.crearBanco({ nombre }),
    onSuccess: () => {
      invalidate();
      setEditing(null);
    },
    onError: setError,
  });
  const actualizar = useMutation({
    mutationFn: (id: number) => userSvc.actualizarBanco(id, { nombre }),
    onSuccess: () => {
      invalidate();
      setEditing(null);
    },
    onError: setError,
  });
  const eliminar = useMutation({
    mutationFn: (id: number) => userSvc.eliminarBanco(id),
    onSuccess: invalidate,
    onError: setError,
  });

  function openNew() {
    setNombre("");
    setError(null);
    setEditing("new");
  }
  function openEdit(banco: Banco) {
    setNombre(banco.nombre);
    setError(null);
    setEditing(banco);
  }
  function submit() {
    if (editing === "new") crear.mutate();
    else if (editing) actualizar.mutate(editing.id);
  }

  return (
    <div>
      <PageHeader
        title="Bancos"
        description="CRUD de bancos (user-svc). Los usados por tarjetas/cuentas bancarias no se pueden borrar."
        action={
          <button className={btnPrimary} onClick={openNew}>
            + Nuevo banco
          </button>
        }
      />
      <ErrorMessage error={eliminar.error} />
      {bancosQuery.isLoading && <Spinner />}
      {bancosQuery.data && (
        <DataTable
          columns={[
            { header: "ID", render: (b) => b.id },
            { header: "Nombre", render: (b) => b.nombre },
            {
              header: "",
              className: "text-right",
              render: (b) => (
                <div className="flex justify-end gap-2">
                  <button className={btnSecondary} onClick={() => openEdit(b)}>
                    Editar
                  </button>
                  <button
                    className={btnDanger}
                    disabled={eliminar.isPending}
                    onClick={() => {
                      if (confirm(`¿Borrar el banco "${b.nombre}"?`))
                        eliminar.mutate(b.id);
                    }}
                  >
                    Borrar
                  </button>
                </div>
              ),
            },
          ]}
          rows={bancosQuery.data}
          keyFn={(b) => b.id}
        />
      )}

      <Modal
        open={editing !== null}
        onClose={() => setEditing(null)}
        title={editing === "new" ? "Nuevo banco" : "Editar banco"}
      >
        <ErrorMessage error={error} />
        <FormField label="Nombre">
          <input
            className={inputClass}
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            autoFocus
          />
        </FormField>
        <div className="mt-4 flex justify-end gap-2">
          <button className={btnSecondary} onClick={() => setEditing(null)}>
            Cancelar
          </button>
          <button
            className={btnPrimary}
            disabled={!nombre.trim() || crear.isPending || actualizar.isPending}
            onClick={submit}
          >
            Guardar
          </button>
        </div>
      </Modal>
    </div>
  );
}
