import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Link, useLocation, useParams } from "react-router-dom";
import { userSvc } from "../../api/userSvc";
import type { Propietario } from "../../types/userSvc";
import {
  btnPrimary,
  ErrorMessage,
  FormField,
  inputClass,
  PageHeader,
} from "../../components/ui";

export function PropietarioDetailPage() {
  const { id } = useParams<{ id: string }>();
  const propietarioId = Number(id);
  const location = useLocation();
  const queryClient = useQueryClient();

  // Igual que con Huesped: no existe GET /users/{id}, así que el detalle
  // llega por navegación desde el listado.
  const [propietario, setPropietario] = useState<Propietario | undefined>(
    (location.state as { propietario?: Propietario } | null)?.propietario,
  );

  const [form, setForm] = useState({
    nombre: propietario?.nombre ?? "",
    email: propietario?.email ?? "",
    telefono: propietario?.telefono ?? "",
    idHotel: propietario?.idHotel?.toString() ?? "",
  });
  const [error, setError] = useState<unknown>(null);

  const actualizar = useMutation({
    mutationFn: () =>
      userSvc.actualizarPropietario(propietarioId, {
        nombre: form.nombre,
        email: form.email,
        telefono: form.telefono,
        idHotel: form.idHotel ? Number(form.idHotel) : null,
      }),
    onSuccess: (updated) => {
      setPropietario(updated);
      queryClient.invalidateQueries({ queryKey: ["propietarios"] });
    },
    onError: setError,
  });

  if (!propietario) {
    return (
      <div>
        <PageHeader title="Propietario" />
        <p className="text-sm text-gray-500">
          No tengo los datos de este propietario a mano (se perdieron al
          refrescar la página).{" "}
          <Link
            to="/usuarios/propietarios"
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
        title={propietario.nombre}
        description={`Propietario #${propietario.id} · DNI ${propietario.dni}`}
      />

      <div className="mb-6 rounded-lg border border-gray-200 p-4 dark:border-gray-800">
        <h3 className="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-300">
          Datos
        </h3>
        <ErrorMessage error={error} />
        <div className="grid grid-cols-2 gap-x-3">
          <FormField label="Nombre">
            <input
              className={inputClass}
              value={form.nombre}
              onChange={(e) => setForm({ ...form, nombre: e.target.value })}
            />
          </FormField>
          <FormField label="Email">
            <input
              className={inputClass}
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
          </FormField>
          <FormField label="Teléfono">
            <input
              className={inputClass}
              value={form.telefono}
              onChange={(e) => setForm({ ...form, telefono: e.target.value })}
            />
          </FormField>
          <FormField
            label="ID de hotel"
            hint="Se asigna desde gestion-svc; acá solo se referencia por id."
          >
            <input
              className={inputClass}
              value={form.idHotel}
              onChange={(e) => setForm({ ...form, idHotel: e.target.value })}
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

      <div className="rounded-lg border border-gray-200 p-4 dark:border-gray-800">
        <h3 className="mb-2 text-sm font-semibold text-gray-700 dark:text-gray-300">
          Cuenta bancaria
        </h3>
        {propietario.cuentaBancaria ? (
          <dl className="grid grid-cols-2 gap-2 text-sm">
            <dt className="text-gray-500">Número</dt>
            <dd>{propietario.cuentaBancaria.numeroCuenta}</dd>
            <dt className="text-gray-500">CBU</dt>
            <dd>{propietario.cuentaBancaria.cbu}</dd>
            <dt className="text-gray-500">Alias</dt>
            <dd>{propietario.cuentaBancaria.alias}</dd>
            <dt className="text-gray-500">Banco</dt>
            <dd>{propietario.cuentaBancaria.banco?.nombre ?? "—"}</dd>
          </dl>
        ) : (
          <p className="text-sm text-gray-500">Sin cuenta bancaria.</p>
        )}
      </div>
    </div>
  );
}
