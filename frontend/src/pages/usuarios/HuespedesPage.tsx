import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { userSvc } from "../../api/userSvc";
import { isHuesped } from "../../types/userSvc";
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

export function HuespedesPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [buscarPor, setBuscarPor] = useState<"nombre" | "dni">("nombre");
  const [texto, setTexto] = useState("");
  const [page, setPage] = useState(0);

  const query = useQuery({
    queryKey: ["huespedes", buscarPor, texto, page],
    queryFn: () =>
      buscarPor === "nombre"
        ? userSvc.buscarUsuarios(texto, page, PAGE_SIZE)
        : userSvc.buscarPorDni(texto, page, PAGE_SIZE),
  });

  const huespedes = query.data?.content.filter(isHuesped) ?? [];

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
    fechaNacimiento: "",
    numeroCC: "",
    nombreTitular: "",
    fechaVencimientoCC: "",
    cvcCC: "",
    esPrincipalCC: true,
    idBanco: "",
  });

  const crear = useMutation({
    mutationFn: () =>
      userSvc.crearHuesped({
        ...form,
        idBanco: Number(form.idBanco),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["huespedes"] });
      setCreating(false);
    },
    onError: setError,
  });

  function openNew() {
    setForm({
      nombre: "",
      email: "",
      telefono: "",
      dni: "",
      fechaNacimiento: "",
      numeroCC: "",
      nombreTitular: "",
      fechaVencimientoCC: "",
      cvcCC: "",
      esPrincipalCC: true,
      idBanco: "",
    });
    setError(null);
    setCreating(true);
  }

  const formValid =
    form.nombre &&
    form.email &&
    form.telefono &&
    form.dni &&
    form.fechaNacimiento &&
    form.numeroCC &&
    form.nombreTitular &&
    form.fechaVencimientoCC &&
    form.cvcCC &&
    form.idBanco;

  return (
    <div>
      <PageHeader
        title="Huéspedes"
        description="Usuarios huésped, con su tarjeta de crédito principal (user-svc)."
        action={
          <button className={btnPrimary} onClick={openNew}>
            + Nuevo huésped
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
            placeholder={buscarPor === "nombre" ? "ej: juan" : "ej: 30"}
          />
        </FormField>
      </div>

      {query.isLoading && <Spinner />}
      <ErrorMessage error={query.error} />
      {query.data && (
        <>
          <DataTable
            columns={[
              { header: "Nombre", render: (h) => h.nombre },
              { header: "DNI", render: (h) => h.dni },
              { header: "Email", render: (h) => h.email },
              { header: "Teléfono", render: (h) => h.telefono },
              { header: "Nacimiento", render: (h) => h.fechaNacimiento },
              { header: "Tarjetas", render: (h) => h.tarjetaCredito.length },
            ]}
            rows={huespedes}
            keyFn={(h) => h.id}
            onRowClick={(h) =>
              navigate(`/usuarios/huespedes/${h.id}`, { state: { huesped: h } })
            }
            emptyMessage="Sin huéspedes para este criterio de búsqueda"
          />
          <div className="mt-3 flex items-center justify-between text-sm text-gray-500">
            <span>
              Página {query.data.number + 1} de{" "}
              {Math.max(query.data.totalPages, 1)} · {query.data.totalElements}{" "}
              usuarios en total (huésped + propietario)
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
        title="Nuevo huésped"
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
          <FormField label="Fecha de nacimiento">
            <input
              type="date"
              className={inputClass}
              value={form.fechaNacimiento}
              onChange={(e) =>
                setForm({ ...form, fechaNacimiento: e.target.value })
              }
            />
          </FormField>
        </div>

        <h4 className="mb-2 mt-2 text-sm font-semibold text-gray-700 dark:text-gray-300">
          Tarjeta principal
        </h4>
        <div className="grid grid-cols-2 gap-x-3">
          <FormField label="Número (16 dígitos)">
            <input
              className={inputClass}
              value={form.numeroCC}
              onChange={(e) => setForm({ ...form, numeroCC: e.target.value })}
              maxLength={16}
            />
          </FormField>
          <FormField label="Nombre del titular">
            <input
              className={inputClass}
              value={form.nombreTitular}
              onChange={(e) =>
                setForm({ ...form, nombreTitular: e.target.value })
              }
            />
          </FormField>
          <FormField label="Vencimiento (MM/YY)">
            <input
              className={inputClass}
              placeholder="12/27"
              value={form.fechaVencimientoCC}
              onChange={(e) =>
                setForm({ ...form, fechaVencimientoCC: e.target.value })
              }
            />
          </FormField>
          <FormField label="CVC">
            <input
              className={inputClass}
              value={form.cvcCC}
              onChange={(e) => setForm({ ...form, cvcCC: e.target.value })}
              maxLength={4}
            />
          </FormField>
          <FormField label="Banco emisor">
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
            Crear huésped
          </button>
        </div>
      </Modal>
    </div>
  );
}
