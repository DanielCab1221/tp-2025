import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import { userSvc } from "../../api/userSvc";
import { useConfirm } from "../../lib/confirm";
import { useToast } from "../../lib/toast";
import type { Huesped } from "../../types/userSvc";
import { DataTable } from "../../components/DataTable";
import {
  btnDanger,
  btnPrimary,
  btnSecondary,
  EstadoBadge,
  ErrorMessage,
  FormField,
  inputClass,
  Modal,
  PageHeader,
  Spinner,
} from "../../components/ui";

export function HuespedDetailPage() {
  const { id } = useParams<{ id: string }>();
  const huespedId = Number(id);
  const navigate = useNavigate();
  const location = useLocation();
  const queryClient = useQueryClient();
  const toast = useToast();
  const confirm = useConfirm();

  // No existe GET /users/{id} en user-svc: el detalle se recibe por navegación
  // desde el listado (ver HuespedesPage) y se refresca localmente con lo que
  // devuelve cada PATCH.
  const [huesped, setHuesped] = useState<Huesped | undefined>(
    (location.state as { huesped?: Huesped } | null)?.huesped,
  );

  const [editForm, setEditForm] = useState({
    nombre: huesped?.nombre ?? "",
    email: huesped?.email ?? "",
    telefono: huesped?.telefono ?? "",
    fechaNacimiento: huesped?.fechaNacimiento ?? "",
  });
  const [editError, setEditError] = useState<unknown>(null);

  const actualizar = useMutation({
    mutationFn: () => userSvc.actualizarHuesped(huespedId, editForm),
    onSuccess: (updated) => {
      setHuesped(updated);
      queryClient.invalidateQueries({ queryKey: ["huespedes"] });
      toast.success("Huésped actualizado");
    },
    onError: setEditError,
  });

  const eliminar = useMutation({
    mutationFn: () => userSvc.eliminarHuesped(huespedId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["huespedes"] });
      toast.success("Huésped eliminado");
      navigate("/usuarios/huespedes");
    },
  });

  const tarjetasQuery = useQuery({
    queryKey: ["tarjetas", huespedId],
    queryFn: () => userSvc.listarTarjetas(huespedId),
    enabled: Number.isFinite(huespedId),
  });

  const invalidateTarjetas = () =>
    queryClient.invalidateQueries({ queryKey: ["tarjetas", huespedId] });

  const eliminarTarjeta = useMutation({
    mutationFn: (tarjetaId: number) => userSvc.eliminarTarjeta(tarjetaId),
    onSuccess: () => {
      invalidateTarjetas();
      toast.success("Tarjeta eliminada");
    },
  });
  const cambiarPrincipal = useMutation({
    mutationFn: (tarjetaId: number) =>
      userSvc.cambiarTarjetaPrincipal(huespedId, tarjetaId),
    onSuccess: () => {
      invalidateTarjetas();
      toast.success("Tarjeta marcada como principal");
    },
  });

  const [addingTarjeta, setAddingTarjeta] = useState(false);
  const [tarjetaError, setTarjetaError] = useState<unknown>(null);
  const [tarjetaForm, setTarjetaForm] = useState({
    numero: "",
    nombreTitular: "",
    fechaVencimiento: "",
    cvc: "",
    esPrincipal: false,
    idBanco: "",
  });
  const bancosQuery = useQuery({
    queryKey: ["bancos"],
    queryFn: userSvc.listarBancos,
  });

  const agregarTarjeta = useMutation({
    mutationFn: () =>
      userSvc.agregarTarjeta(huespedId, {
        ...tarjetaForm,
        idBanco: Number(tarjetaForm.idBanco),
      }),
    onSuccess: () => {
      invalidateTarjetas();
      setAddingTarjeta(false);
      toast.success("Tarjeta agregada");
    },
    onError: setTarjetaError,
  });

  if (!huesped) {
    return (
      <div>
        <PageHeader title="Huésped" />
        <p className="text-sm text-gray-500">
          No tengo los datos de este huésped a mano (se perdieron al refrescar
          la página).{" "}
          <Link
            to="/usuarios/huespedes"
            className="text-indigo-600 hover:underline dark:text-indigo-400"
          >
            Volvé al listado
          </Link>{" "}
          y entrá de nuevo desde ahí.
        </p>
      </div>
    );
  }

  return (
    <div>
      <PageHeader
        title={huesped.nombre}
        description={`Huésped #${huesped.id} · DNI ${huesped.dni}`}
        action={
          <button
            className={btnDanger}
            disabled={eliminar.isPending}
            onClick={async () => {
              if (
                await confirm(
                  "¿Borrar este huésped? Se borran también sus tarjetas.",
                  { confirmLabel: "Borrar" },
                )
              )
                eliminar.mutate();
            }}
          >
            Borrar huésped
          </button>
        }
      />

      <div className="mb-8 rounded-lg border border-gray-200 p-4 dark:border-gray-800">
        <h3 className="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-300">
          Datos
        </h3>
        <ErrorMessage error={editError} />
        <div className="grid grid-cols-2 gap-x-3">
          <FormField label="Nombre">
            <input
              className={inputClass}
              value={editForm.nombre}
              onChange={(e) =>
                setEditForm({ ...editForm, nombre: e.target.value })
              }
            />
          </FormField>
          <FormField label="Email">
            <input
              className={inputClass}
              value={editForm.email}
              onChange={(e) =>
                setEditForm({ ...editForm, email: e.target.value })
              }
            />
          </FormField>
          <FormField label="Teléfono">
            <input
              className={inputClass}
              value={editForm.telefono}
              onChange={(e) =>
                setEditForm({ ...editForm, telefono: e.target.value })
              }
            />
          </FormField>
          <FormField label="Fecha de nacimiento">
            <input
              type="date"
              className={inputClass}
              value={editForm.fechaNacimiento}
              onChange={(e) =>
                setEditForm({ ...editForm, fechaNacimiento: e.target.value })
              }
            />
          </FormField>
        </div>
        <button
          className={btnPrimary}
          disabled={actualizar.isPending}
          onClick={() => actualizar.mutate()}
        >
          Guardar cambios
        </button>
      </div>

      <div className="mb-3 flex items-center justify-between">
        <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300">
          Tarjetas de crédito
        </h3>
        <button className={btnPrimary} onClick={() => setAddingTarjeta(true)}>
          + Agregar tarjeta
        </button>
      </div>
      <ErrorMessage error={eliminarTarjeta.error || cambiarPrincipal.error} />
      {tarjetasQuery.isLoading && <Spinner />}
      {tarjetasQuery.data && (
        <DataTable
          columns={[
            { header: "Número", render: (t) => `•••• ${t.numero.slice(-4)}` },
            { header: "Titular", render: (t) => t.nombreTitular },
            { header: "Vence", render: (t) => t.fechaVencimiento },
            { header: "Banco", render: (t) => t.banco?.nombre ?? "—" },
            {
              header: "Principal",
              render: (t) =>
                t.esPrincipal ? <EstadoBadge estado="PRINCIPAL" /> : "",
            },
            {
              header: "",
              className: "text-right",
              render: (t) => (
                <div className="flex justify-end gap-2">
                  {!t.esPrincipal && (
                    <button
                      className={btnSecondary}
                      disabled={cambiarPrincipal.isPending}
                      onClick={() => cambiarPrincipal.mutate(t.id)}
                    >
                      Hacer principal
                    </button>
                  )}
                  <button
                    className={btnDanger}
                    disabled={t.esPrincipal || eliminarTarjeta.isPending}
                    title={
                      t.esPrincipal
                        ? "No se puede borrar la tarjeta principal"
                        : undefined
                    }
                    onClick={() => eliminarTarjeta.mutate(t.id)}
                  >
                    Borrar
                  </button>
                </div>
              ),
            },
          ]}
          rows={tarjetasQuery.data}
          keyFn={(t) => t.id}
        />
      )}

      <Modal
        open={addingTarjeta}
        onClose={() => setAddingTarjeta(false)}
        title="Agregar tarjeta"
      >
        <ErrorMessage error={tarjetaError} />
        <FormField label="Número (16 dígitos)">
          <input
            className={inputClass}
            value={tarjetaForm.numero}
            maxLength={16}
            onChange={(e) =>
              setTarjetaForm({ ...tarjetaForm, numero: e.target.value })
            }
          />
        </FormField>
        <FormField label="Nombre del titular">
          <input
            className={inputClass}
            value={tarjetaForm.nombreTitular}
            onChange={(e) =>
              setTarjetaForm({ ...tarjetaForm, nombreTitular: e.target.value })
            }
          />
        </FormField>
        <div className="grid grid-cols-2 gap-x-3">
          <FormField label="Vencimiento (MM/YY)">
            <input
              className={inputClass}
              placeholder="12/27"
              value={tarjetaForm.fechaVencimiento}
              onChange={(e) =>
                setTarjetaForm({
                  ...tarjetaForm,
                  fechaVencimiento: e.target.value,
                })
              }
            />
          </FormField>
          <FormField label="CVC">
            <input
              className={inputClass}
              maxLength={4}
              value={tarjetaForm.cvc}
              onChange={(e) =>
                setTarjetaForm({ ...tarjetaForm, cvc: e.target.value })
              }
            />
          </FormField>
        </div>
        <FormField label="Banco emisor">
          <select
            className={inputClass}
            value={tarjetaForm.idBanco}
            onChange={(e) =>
              setTarjetaForm({ ...tarjetaForm, idBanco: e.target.value })
            }
          >
            <option value="">Elegir…</option>
            {bancosQuery.data?.map((b) => (
              <option key={b.id} value={b.id}>
                {b.nombre}
              </option>
            ))}
          </select>
        </FormField>
        <label className="mb-3 flex items-center gap-2 text-sm text-gray-700 dark:text-gray-300">
          <input
            type="checkbox"
            checked={tarjetaForm.esPrincipal}
            onChange={(e) =>
              setTarjetaForm({ ...tarjetaForm, esPrincipal: e.target.checked })
            }
          />
          Marcar como principal (desmarca la anterior)
        </label>
        <div className="mt-4 flex justify-end gap-2">
          <button
            className={btnSecondary}
            onClick={() => setAddingTarjeta(false)}
          >
            Cancelar
          </button>
          <button
            className={btnPrimary}
            disabled={
              !tarjetaForm.numero ||
              !tarjetaForm.nombreTitular ||
              !tarjetaForm.fechaVencimiento ||
              !tarjetaForm.cvc ||
              !tarjetaForm.idBanco ||
              agregarTarjeta.isPending
            }
            onClick={() => agregarTarjeta.mutate()}
          >
            Agregar
          </button>
        </div>
      </Modal>
    </div>
  );
}
