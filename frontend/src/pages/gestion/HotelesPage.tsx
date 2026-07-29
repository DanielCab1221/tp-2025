import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { gestionSvc } from "../../api/gestionSvc";
import {
  AMENITIES,
  type Amenity,
  type BuscarHotelesParams,
} from "../../types/gestionSvc";
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

export function HotelesPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [filtros, setFiltros] = useState<BuscarHotelesParams>({});
  const query = useQuery({
    queryKey: ["hoteles", filtros],
    queryFn: () => gestionSvc.listarHoteles(filtros),
  });

  const [creating, setCreating] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const [form, setForm] = useState({
    nombre: "",
    cuit: "",
    domicilio: "",
    latitud: "",
    longitud: "",
    telefono: "",
    correoContacto: "",
    categoria: "3",
  });

  const crear = useMutation({
    mutationFn: () =>
      gestionSvc.crearHotel({
        nombre: form.nombre,
        cuit: form.cuit,
        domicilio: form.domicilio,
        latitud: form.latitud ? Number(form.latitud) : null,
        longitud: form.longitud ? Number(form.longitud) : null,
        telefono: form.telefono || null,
        correoContacto: form.correoContacto || null,
        categoria: form.categoria ? Number(form.categoria) : null,
        cerrado: false,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["hoteles"] });
      setCreating(false);
    },
    onError: setError,
  });

  function openNew() {
    setForm({
      nombre: "",
      cuit: "",
      domicilio: "",
      latitud: "",
      longitud: "",
      telefono: "",
      correoContacto: "",
      categoria: "3",
    });
    setError(null);
    setCreating(true);
  }

  return (
    <div>
      <PageHeader
        title="Hoteles"
        description="Gestión de hoteles (gestion-svc). No se pueden borrar, solo cerrar."
        action={
          <button className={btnPrimary} onClick={openNew}>
            + Nuevo hotel
          </button>
        }
      />

      <div className="mb-4 flex flex-wrap items-end gap-2">
        <FormField label="Nombre">
          <input
            className={inputClass}
            value={filtros.nombre ?? ""}
            onChange={(e) =>
              setFiltros({ ...filtros, nombre: e.target.value || undefined })
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
        <FormField label="Amenity">
          <select
            className={inputClass}
            value={filtros.amenity ?? ""}
            onChange={(e) =>
              setFiltros({
                ...filtros,
                amenity: (e.target.value || undefined) as Amenity | undefined,
              })
            }
          >
            <option value="">Cualquiera</option>
            {AMENITIES.map((a) => (
              <option key={a} value={a}>
                {a}
              </option>
            ))}
          </select>
        </FormField>
        <FormField label="Cerrado">
          <select
            className={inputClass}
            value={filtros.cerrado === undefined ? "" : String(filtros.cerrado)}
            onChange={(e) =>
              setFiltros({
                ...filtros,
                cerrado:
                  e.target.value === "" ? undefined : e.target.value === "true",
              })
            }
          >
            <option value="">Cualquiera</option>
            <option value="false">Abiertos</option>
            <option value="true">Cerrados</option>
          </select>
        </FormField>
      </div>

      {query.isLoading && <Spinner />}
      <ErrorMessage error={query.error} />
      {query.data && (
        <DataTable
          columns={[
            { header: "Nombre", render: (h) => h.nombre },
            { header: "CUIT", render: (h) => h.cuit },
            {
              header: "Categoría",
              render: (h) => (h.categoria ? "★".repeat(h.categoria) : "—"),
            },
            { header: "Domicilio", render: (h) => h.domicilio },
            {
              header: "Estado",
              render: (h) => (h.cerrado ? "Cerrado" : "Abierto"),
            },
            { header: "Amenities", render: (h) => h.amenities?.length ?? 0 },
          ]}
          rows={query.data}
          keyFn={(h) => h.id}
          onRowClick={(h) => navigate(`/gestion/hoteles/${h.id}`)}
        />
      )}

      <Modal
        open={creating}
        onClose={() => setCreating(false)}
        title="Nuevo hotel"
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
          <FormField label="CUIT">
            <input
              className={inputClass}
              value={form.cuit}
              onChange={(e) => setForm({ ...form, cuit: e.target.value })}
            />
          </FormField>
          <FormField label="Domicilio">
            <input
              className={inputClass}
              value={form.domicilio}
              onChange={(e) => setForm({ ...form, domicilio: e.target.value })}
            />
          </FormField>
          <FormField label="Categoría (1-5)">
            <input
              type="number"
              min={1}
              max={5}
              className={inputClass}
              value={form.categoria}
              onChange={(e) => setForm({ ...form, categoria: e.target.value })}
            />
          </FormField>
          <FormField label="Teléfono">
            <input
              className={inputClass}
              value={form.telefono}
              onChange={(e) => setForm({ ...form, telefono: e.target.value })}
            />
          </FormField>
          <FormField label="Correo de contacto">
            <input
              className={inputClass}
              value={form.correoContacto}
              onChange={(e) =>
                setForm({ ...form, correoContacto: e.target.value })
              }
            />
          </FormField>
          <FormField label="Latitud">
            <input
              className={inputClass}
              value={form.latitud}
              onChange={(e) => setForm({ ...form, latitud: e.target.value })}
            />
          </FormField>
          <FormField label="Longitud">
            <input
              className={inputClass}
              value={form.longitud}
              onChange={(e) => setForm({ ...form, longitud: e.target.value })}
            />
          </FormField>
        </div>
        <div className="mt-4 flex justify-end gap-2">
          <button className={btnSecondary} onClick={() => setCreating(false)}>
            Cancelar
          </button>
          <button
            className={btnPrimary}
            disabled={
              !form.nombre || !form.cuit || !form.domicilio || crear.isPending
            }
            onClick={() => crear.mutate()}
          >
            Crear hotel
          </button>
        </div>
      </Modal>
    </div>
  );
}
