import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { reservasSvc } from "../../api/reservasSvc";
import { DataTable } from "../../components/DataTable";
import {
  EstadoBadge,
  ErrorMessage,
  FormField,
  inputClass,
  PageHeader,
  Spinner,
} from "../../components/ui";

export function ReservasPage() {
  const navigate = useNavigate();
  const [idUsuario, setIdUsuario] = useState("");
  const query = useQuery({
    queryKey: ["reservas", idUsuario],
    queryFn: () => reservasSvc.listarReservas(idUsuario || undefined),
  });

  return (
    <div>
      <PageHeader
        title="Reservas"
        description="Ciclo de vida completo de una reserva (reservas-svc)."
      />

      <div className="mb-4">
        <FormField
          label="Filtrar por ID de usuario"
          hint="Vacío = todas las reservas"
        >
          <input
            className={inputClass}
            value={idUsuario}
            onChange={(e) => setIdUsuario(e.target.value)}
          />
        </FormField>
      </div>

      {query.isLoading && <Spinner />}
      <ErrorMessage error={query.error} />
      {query.data && (
        <DataTable
          columns={[
            {
              header: "Huésped",
              render: (r) => r.huesped?.nombreApellido ?? "(bloqueo/cierre)",
            },
            { header: "Check-in", render: (r) => r.checkIn?.slice(0, 10) },
            {
              header: "Check-out",
              render: (r) => r.checkOut?.slice(0, 10) ?? "—",
            },
            {
              header: "Total",
              render: (r) =>
                r.precioTotal != null
                  ? `$${r.precioTotal.toLocaleString("es-AR")}`
                  : "—",
            },
            {
              header: "Estado",
              render: (r) => <EstadoBadge estado={r.estadoReserva ?? "—"} />,
            },
          ]}
          rows={query.data}
          keyFn={(r) => r._id}
          onRowClick={(r) => navigate(`/reservas/${r._id}`)}
        />
      )}
    </div>
  );
}
