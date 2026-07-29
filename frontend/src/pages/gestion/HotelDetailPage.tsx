import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useParams } from "react-router-dom";
import { gestionSvc } from "../../api/gestionSvc";
import { AMENITIES, type Amenity } from "../../types/gestionSvc";
import {
  btnDanger,
  btnPrimary,
  btnSecondary,
  ErrorMessage,
  FormField,
  inputClass,
  PageHeader,
  Spinner,
} from "../../components/ui";

export function HotelDetailPage() {
  const { id } = useParams<{ id: string }>();
  const hotelId = Number(id);
  const queryClient = useQueryClient();
  const query = useQuery({
    queryKey: ["hotel", hotelId],
    queryFn: () => gestionSvc.obtenerHotel(hotelId),
  });
  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["hotel", hotelId] });
    queryClient.invalidateQueries({ queryKey: ["hoteles"] });
  };

  const [form, setForm] = useState({
    categoria: "",
    telefono: "",
    correoContacto: "",
  });
  const [error, setError] = useState<unknown>(null);
  const [synced, setSynced] = useState(false);
  if (query.data && !synced) {
    setForm({
      categoria: query.data.categoria?.toString() ?? "",
      telefono: query.data.telefono ?? "",
      correoContacto: query.data.correoContacto ?? "",
    });
    setSynced(true);
  }

  const actualizar = useMutation({
    mutationFn: () =>
      gestionSvc.actualizarHotel(hotelId, {
        categoria: form.categoria ? Number(form.categoria) : null,
        telefono: form.telefono || null,
        correoContacto: form.correoContacto || null,
      }),
    onSuccess: invalidate,
    onError: setError,
  });

  const cerrar = useMutation({
    mutationFn: () => gestionSvc.cerrarHotel(hotelId),
    onSuccess: invalidate,
  });

  const [selectedAmenity, setSelectedAmenity] = useState<Amenity | "">("");
  const agregarAmenity = useMutation({
    mutationFn: (amenity: Amenity) =>
      gestionSvc.agregarAmenities(hotelId, [amenity]),
    onSuccess: () => {
      invalidate();
      setSelectedAmenity("");
    },
  });
  const quitarAmenity = useMutation({
    mutationFn: (amenity: Amenity) =>
      gestionSvc.quitarAmenity(hotelId, amenity),
    onSuccess: invalidate,
  });

  if (query.isLoading) return <Spinner />;
  if (!query.data) return <ErrorMessage error={query.error} />;
  const hotel = query.data;
  const amenitiesActuales = new Set(
    hotel.amenities?.map((a) => a.amenity) ?? [],
  );
  const amenitiesDisponibles = AMENITIES.filter(
    (a) => !amenitiesActuales.has(a),
  );

  return (
    <div>
      <PageHeader
        title={hotel.nombre}
        description={`Hotel #${hotel.id} · CUIT ${hotel.cuit} · ${hotel.cerrado ? "Cerrado" : "Abierto"}`}
        action={
          !hotel.cerrado && (
            <button
              className={btnDanger}
              disabled={cerrar.isPending}
              onClick={() => {
                if (
                  confirm(
                    "Cerrar el hotel es irreversible: todas sus habitaciones pasan a no disponibles. ¿Continuar?",
                  )
                )
                  cerrar.mutate();
              }}
            >
              Cerrar hotel
            </button>
          )
        }
      />

      <div className="mb-6 rounded-lg border border-gray-200 p-4 dark:border-gray-800">
        <h3 className="mb-1 text-sm font-semibold text-gray-700 dark:text-gray-300">
          Datos editables
        </h3>
        <p className="mb-3 text-xs text-gray-400">
          Solo categoría, teléfono y correo se pueden modificar (regla de
          negocio).
        </p>
        <ErrorMessage error={error} />
        <div className="grid grid-cols-3 gap-x-3">
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
        </div>
        <div className="mb-3 text-sm text-gray-500">
          Domicilio: {hotel.domicilio}{" "}
          {hotel.latitud && hotel.longitud
            ? `· (${hotel.latitud}, ${hotel.longitud})`
            : ""}
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
        <h3 className="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-300">
          Amenities
        </h3>
        <div className="mb-3 flex flex-wrap gap-2">
          {[...amenitiesActuales].length === 0 && (
            <span className="text-sm text-gray-400">
              Sin amenities cargados.
            </span>
          )}
          {[...amenitiesActuales].map((a) => (
            <span
              key={a}
              className="inline-flex items-center gap-1.5 rounded-full bg-indigo-50 px-2.5 py-1 text-xs font-medium text-indigo-700 dark:bg-indigo-900/40 dark:text-indigo-300"
            >
              {a}
              <button
                className="text-indigo-400 hover:text-indigo-700 dark:hover:text-indigo-100"
                disabled={quitarAmenity.isPending}
                onClick={() => quitarAmenity.mutate(a)}
                aria-label={`Quitar ${a}`}
              >
                ✕
              </button>
            </span>
          ))}
        </div>
        <div className="flex items-end gap-2">
          <FormField label="Agregar amenity">
            <select
              className={inputClass}
              value={selectedAmenity}
              onChange={(e) => setSelectedAmenity(e.target.value as Amenity)}
            >
              <option value="">Elegir…</option>
              {amenitiesDisponibles.map((a) => (
                <option key={a} value={a}>
                  {a}
                </option>
              ))}
            </select>
          </FormField>
          <button
            className={btnSecondary}
            disabled={!selectedAmenity || agregarAmenity.isPending}
            onClick={() =>
              selectedAmenity && agregarAmenity.mutate(selectedAmenity)
            }
          >
            Agregar
          </button>
        </div>
      </div>
    </div>
  );
}
