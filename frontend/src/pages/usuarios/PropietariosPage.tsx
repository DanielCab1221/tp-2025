import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { userSvc } from "../../api/userSvc";
import { useToast } from "../../lib/toast";
import { isPropietario } from "../../types/userSvc";
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

const PAGE_SIZE = 10;

export function PropietariosPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const toast = useToast();
  const [buscarPor, setBuscarPor] = useState<"nombre" | "dni">("nombre");
  const [texto, setTexto] = useState("");
  const [page, setPage] = useState(0);

  const query = useQuery({
    queryKey: ["propietarios", buscarPor, texto, page],
    queryFn: () =>
      buscarPor === "nombre"
        ? userSvc.buscarUsuarios(texto, page, PAGE_SIZE)
        : userSvc.buscarPorDni(texto, page, PAGE_SIZE),
  });
  const propietarios = query.data?.content.filter(isPropietario) ?? [];

  const [creating, setCreating] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const bancosQuery = useQuery({
    queryKey: ["bancos"],
    queryFn: userSvc.listarBancos,
  });
  const [form, setForm] = useState({
    nombre: "",
    email: "",
    telefono: "",
    dni: "",
    numeroCuenta: "",
    cbu: "",
    alias: "",
    idBanco: "",
  });

  const crear = useMutation({
    mutationFn: () =>
      userSvc.crearPropietario({
        nombre: form.nombre,
        email: form.email,
        telefono: form.telefono,
        dni: form.dni,
        idHotel: null,
        cuentaBancaria: {
          numeroCuenta: form.numeroCuenta,
          cbu: form.cbu,
          alias: form.alias,
          idBanco: Number(form.idBanco),
        },
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["propietarios"] });
      setCreating(false);
      toast.success("Propietario creado");
    },
    onError: setError,
  });

  function openNew() {
    setForm({
      nombre: "",
      email: "",
      telefono: "",
      dni: "",
      numeroCuenta: "",
      cbu: "",
      alias: "",
      idBanco: "",
    });
    setError(null);
    setCreating(true);
  }

  const formValid =
    form.nombre &&
    form.telefono &&
    form.dni &&
    form.numeroCuenta &&
    form.cbu &&
    form.alias &&
    form.idBanco;

  return (
    <div>
      <PageHeader
        title="Propietarios"
        description="Usuarios propietarios, con su cuenta bancaria (user-svc). No se pueden borrar."
        action={
          <button className={btnPrimary} onClick={openNew}>
            + Nuevo propietario
          </button>
        }
      />

      <div className="mb-4 flex flex-wrap items-end gap-2">
        <FormField label="Buscar por">
          <select
            className={inputClass}
            value={buscarPor}
            onChange={(e) => {
              setBuscarPor(e.target.value as "nombre" | "dni");
              setPage(0);
            }}
          >
            <option value="nombre">Nombre</option>
            <option value="dni">DNI (contiene)</option>
          </select>
        </FormField>
        <FormField label={buscarPor === "nombre" ? "Nombre" : "DNI"}>
          <input
            className={inputClass}
            value={texto}
            onChange={(e) => {
              setTexto(e.target.value);
              setPage(0);
            }}
          />
        </FormField>
      </div>

      {query.isLoading && <Spinner />}
      <ErrorMessage error={query.error} />
      {query.data && (
        <>
          <DataTable
            columns={[
              { header: "Nombre", render: (p) => p.nombre },
              { header: "DNI", render: (p) => p.dni },
              { header: "Email", render: (p) => p.email },
              { header: "Teléfono", render: (p) => p.telefono },
              {
                header: "Alias cuenta",
                render: (p) => p.cuentaBancaria?.alias ?? "—",
              },
              {
                header: "Hotel",
                render: (p) => p.idHotel ?? "— (sin asignar)",
              },
            ]}
            rows={propietarios}
            keyFn={(p) => p.id}
            onRowClick={(p) =>
              navigate(`/usuarios/propietarios/${p.id}`, {
                state: { propietario: p },
              })
            }
            emptyMessage="Sin propietarios para este criterio de búsqueda"
          />
          <div className="mt-3 flex items-center justify-between text-sm text-gray-500">
            <span>
              Página {query.data.number + 1} de{" "}
              {Math.max(query.data.totalPages, 1)} · {query.data.totalElements}{" "}
              usuarios en total
            </span>
            <div className="flex gap-2">
              <button
                className={btnSecondary}
                disabled={query.data.first}
                onClick={() => setPage((p) => p - 1)}
              >
                ← Anterior
              </button>
              <button
                className={btnSecondary}
                disabled={query.data.last}
                onClick={() => setPage((p) => p + 1)}
              >
                Siguiente →
              </button>
            </div>
          </div>
        </>
      )}

      <Modal
        open={creating}
        onClose={() => setCreating(false)}
        title="Nuevo propietario"
      >
        <ErrorMessage error={error} />
        <div className="grid grid-cols-2 gap-x-3">
          <FormField label="Nombre">
            <input
              className={inputClass}
              value={form.nombre}
              onChange={(e) => setForm({ ...form, nombre: e.target.value })}
            />
          </FormField>
          <FormField label="DNI">
            <input
              className={inputClass}
              value={form.dni}
              onChange={(e) => setForm({ ...form, dni: e.target.value })}
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
        </div>
        <h4 className="mb-2 mt-2 text-sm font-semibold text-gray-700 dark:text-gray-300">
          Cuenta bancaria
        </h4>
        <div className="grid grid-cols-2 gap-x-3">
          <FormField label="Número de cuenta">
            <input
              className={inputClass}
              value={form.numeroCuenta}
              onChange={(e) =>
                setForm({ ...form, numeroCuenta: e.target.value })
              }
            />
          </FormField>
          <FormField label="CBU">
            <input
              className={inputClass}
              value={form.cbu}
              onChange={(e) => setForm({ ...form, cbu: e.target.value })}
            />
          </FormField>
          <FormField label="Alias">
            <input
              className={inputClass}
              value={form.alias}
              onChange={(e) => setForm({ ...form, alias: e.target.value })}
            />
          </FormField>
          <FormField label="Banco">
            <select
              className={inputClass}
              value={form.idBanco}
              onChange={(e) => setForm({ ...form, idBanco: e.target.value })}
            >
              <option value="">Elegir…</option>
              {bancosQuery.data?.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.nombre}
                </option>
              ))}
            </select>
          </FormField>
        </div>
        <div className="mt-4 flex justify-end gap-2">
          <button className={btnSecondary} onClick={() => setCreating(false)}>
            Cancelar
          </button>
          <button
            className={btnPrimary}
            disabled={!formValid || crear.isPending}
            onClick={() => crear.mutate()}
          >
            Crear propietario
          </button>
        </div>
      </Modal>
    </div>
  );
}
