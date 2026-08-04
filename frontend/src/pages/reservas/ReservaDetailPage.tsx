import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useParams } from "react-router-dom";
import { reservasSvc } from "../../api/reservasSvc";
import { useToast } from "../../lib/toast";
import {
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

export function ReservaDetailPage() {
  const { id } = useParams<{ id: string }>();
  const reservaId = id as string;
  const queryClient = useQueryClient();
  const toast = useToast();
  const query = useQuery({
    queryKey: ["reserva", reservaId],
    queryFn: () => reservasSvc.obtenerReserva(reservaId),
  });
  const invalidate = () =>
    queryClient.invalidateQueries({ queryKey: ["reserva", reservaId] });

  const [actionError, setActionError] = useState<unknown>(null);

  const cancelar = useMutation({
    mutationFn: () => reservasSvc.cancelar(reservaId),
    onSuccess: () => {
      invalidate();
      toast.success("Reserva cancelada");
    },
    onError: setActionError,
  });
  const checkin = useMutation({
    mutationFn: () => reservasSvc.checkin(reservaId),
    onSuccess: () => {
      invalidate();
      toast.success("Check-in registrado");
    },
    onError: setActionError,
  });
  const checkout = useMutation({
    mutationFn: () => reservasSvc.checkout(reservaId),
    onSuccess: () => {
      invalidate();
      toast.success("Check-out registrado");
    },
    onError: setActionError,
  });

  const [payOpen, setPayOpen] = useState(false);
  const [payError, setPayError] = useState<unknown>(null);
  const [payForm, setPayForm] = useState({
    method: "TARJETA",
    transactionId: "",
    precio: "",
    moneda: "ARS",
    status: "APROBADO",
  });
  const registrarPago = useMutation({
    mutationFn: () =>
      reservasSvc.registrarPago(reservaId, {
        method: payForm.method,
        transactionId: payForm.transactionId,
        amount: { precio: Number(payForm.precio), moneda: payForm.moneda },
        status: payForm.status,
      }),
    onSuccess: () => {
      invalidate();
      setPayOpen(false);
      setPayForm({
        method: "TARJETA",
        transactionId: "",
        precio: "",
        moneda: "ARS",
        status: "APROBADO",
      });
      toast.success("Pago registrado");
    },
    onError: setPayError,
  });

  const [clientReviewForm, setClientReviewForm] = useState({
    rating: "5",
    comment: "",
  });
  const [hostReviewForm, setHostReviewForm] = useState({
    rating: "5",
    comment: "",
  });
  const reviewCliente = useMutation({
    mutationFn: () =>
      reservasSvc.reviewCliente(reservaId, {
        rating: Number(clientReviewForm.rating),
        comment: clientReviewForm.comment,
      }),
    onSuccess: () => {
      invalidate();
      toast.success("Review del huésped publicada");
    },
    onError: setActionError,
  });
  const reviewHotel = useMutation({
    mutationFn: () =>
      reservasSvc.reviewHotel(reservaId, {
        rating: Number(hostReviewForm.rating),
        comment: hostReviewForm.comment,
      }),
    onSuccess: () => {
      invalidate();
      toast.success("Review del hotel publicada");
    },
    onError: setActionError,
  });

  if (query.isLoading) return <Spinner />;
  if (!query.data) return <ErrorMessage error={query.error} />;
  const r = query.data;
  const estado = r.estadoReserva;

  const totalPagado = (r.pago ?? []).reduce(
    (acc, p) => acc + (p.amount?.precio ?? 0),
    0,
  );
  const porcentaje = r.precioTotal
    ? Math.min(100, Math.round((totalPagado / r.precioTotal) * 100))
    : 0;

  const puedeCancelar =
    ["RESERVADA", "CONFIRMADA", "BLOQUEADA"].includes(estado ?? "") &&
    (r.pago?.length ?? 0) === 0;
  const puedeCheckin = estado === "CONFIRMADA";
  const puedeCheckout = estado === "EFECTUADA";
  const puedePagar = [
    "RESERVADA",
    "CONFIRMADA",
    "EFECTUADA",
    "ADEUDADA",
  ].includes(estado ?? "");
  const puedeReviewCliente = estado === "EFECTUADA" && !r.clientReview;
  const puedeReviewHotel =
    ["EFECTUADA", "FINALIZADA", "ADEUDADA"].includes(estado ?? "") &&
    !r.hostReview;

  return (
    <div>
      <PageHeader
        title={
          r.huesped
            ? (r.huesped.nombreApellido ?? "Huésped")
            : estado === "BLOQUEADA"
              ? "Bloqueo administrativo"
              : "Reserva"
        }
        description={`Reserva ${r._id} · ${r.checkIn?.slice(0, 10)} → ${r.checkOut?.slice(0, 10) ?? "—"}`}
        action={<EstadoBadge estado={estado ?? "—"} />}
      />
      <ErrorMessage error={actionError} />

      <div className="mb-6 flex flex-wrap gap-2">
        <button
          className={btnSecondary}
          disabled={!puedeCancelar || cancelar.isPending}
          onClick={() => cancelar.mutate()}
        >
          Cancelar
        </button>
        <button
          className={btnPrimary}
          disabled={!puedeCheckin || checkin.isPending}
          onClick={() => checkin.mutate()}
        >
          Check-in
        </button>
        <button
          className={btnPrimary}
          disabled={!puedeCheckout || checkout.isPending}
          onClick={() => checkout.mutate()}
        >
          Check-out
        </button>
      </div>

      <div className="mb-6 rounded-lg border border-gray-200 p-4 dark:border-gray-800">
        <div className="mb-2 flex items-center justify-between">
          <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300">
            Pagos
          </h3>
          <button
            className={btnPrimary}
            disabled={!puedePagar}
            onClick={() => setPayOpen(true)}
          >
            + Agregar pago
          </button>
        </div>
        {r.precioTotal != null && (
          <div className="mb-3">
            <div className="mb-1 h-2 w-full overflow-hidden rounded-full bg-gray-100 dark:bg-gray-800">
              <div
                className={`h-full rounded-full ${porcentaje >= 100 ? "bg-green-500" : porcentaje >= 50 ? "bg-indigo-500" : "bg-amber-500"}`}
                style={{ width: `${porcentaje}%` }}
              />
            </div>
            <p className="text-xs text-gray-500">
              ${totalPagado.toLocaleString("es-AR")} / $
              {r.precioTotal.toLocaleString("es-AR")} ({porcentaje}%) — confirma
              automático al 50%, finaliza deuda al 100%
            </p>
          </div>
        )}
        {(r.pago?.length ?? 0) === 0 ? (
          <p className="text-sm text-gray-400">Sin pagos registrados.</p>
        ) : (
          <ul className="space-y-1 text-sm">
            {r.pago!.map((p, i) => (
              <li
                key={i}
                className="flex justify-between border-b border-gray-100 py-1 last:border-0 dark:border-gray-800"
              >
                <span>
                  {p.method} · {p.transactionId} · {p.status}
                </span>
                <span className="font-medium">
                  ${p.amount?.precio?.toLocaleString("es-AR")}{" "}
                  {p.amount?.moneda}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div className="rounded-lg border border-gray-200 p-4 dark:border-gray-800">
          <h3 className="mb-2 text-sm font-semibold text-gray-700 dark:text-gray-300">
            Review del huésped
          </h3>
          {r.clientReview ? (
            <p className="text-sm">
              ★ {r.clientReview.rating} — {r.clientReview.comment}
            </p>
          ) : puedeReviewCliente ? (
            <div>
              <FormField label="Puntaje (0-5)">
                <input
                  type="number"
                  min={0}
                  max={5}
                  step={0.5}
                  className={inputClass}
                  value={clientReviewForm.rating}
                  onChange={(e) =>
                    setClientReviewForm({
                      ...clientReviewForm,
                      rating: e.target.value,
                    })
                  }
                />
              </FormField>
              <FormField label="Comentario">
                <input
                  className={inputClass}
                  value={clientReviewForm.comment}
                  onChange={(e) =>
                    setClientReviewForm({
                      ...clientReviewForm,
                      comment: e.target.value,
                    })
                  }
                />
              </FormField>
              <button
                className={btnPrimary}
                disabled={reviewCliente.isPending}
                onClick={() => reviewCliente.mutate()}
              >
                Publicar review
              </button>
            </div>
          ) : (
            <p className="text-sm text-gray-400">
              Disponible después del check-in (estado EFECTUADA).
            </p>
          )}
        </div>

        <div className="rounded-lg border border-gray-200 p-4 dark:border-gray-800">
          <h3 className="mb-2 text-sm font-semibold text-gray-700 dark:text-gray-300">
            Review del hotel
          </h3>
          {r.hostReview ? (
            <p className="text-sm">
              ★ {r.hostReview.rating} — {r.hostReview.comment}
            </p>
          ) : puedeReviewHotel ? (
            <div>
              <FormField label="Puntaje (0-5)">
                <input
                  type="number"
                  min={0}
                  max={5}
                  step={0.5}
                  className={inputClass}
                  value={hostReviewForm.rating}
                  onChange={(e) =>
                    setHostReviewForm({
                      ...hostReviewForm,
                      rating: e.target.value,
                    })
                  }
                />
              </FormField>
              <FormField label="Comentario">
                <input
                  className={inputClass}
                  value={hostReviewForm.comment}
                  onChange={(e) =>
                    setHostReviewForm({
                      ...hostReviewForm,
                      comment: e.target.value,
                    })
                  }
                />
              </FormField>
              <button
                className={btnPrimary}
                disabled={reviewHotel.isPending}
                onClick={() => reviewHotel.mutate()}
              >
                Publicar review
              </button>
            </div>
          ) : (
            <p className="text-sm text-gray-400">
              Disponible desde el check-in en adelante.
            </p>
          )}
        </div>
      </div>

      <Modal
        open={payOpen}
        onClose={() => setPayOpen(false)}
        title="Registrar pago"
      >
        <ErrorMessage error={payError} />
        <div className="grid grid-cols-2 gap-x-3">
          <FormField label="Método">
            <input
              className={inputClass}
              value={payForm.method}
              onChange={(e) =>
                setPayForm({ ...payForm, method: e.target.value })
              }
            />
          </FormField>
          <FormField label="ID de transacción">
            <input
              className={inputClass}
              value={payForm.transactionId}
              onChange={(e) =>
                setPayForm({ ...payForm, transactionId: e.target.value })
              }
            />
          </FormField>
          <FormField label="Monto">
            <input
              type="number"
              className={inputClass}
              value={payForm.precio}
              onChange={(e) =>
                setPayForm({ ...payForm, precio: e.target.value })
              }
            />
          </FormField>
          <FormField label="Moneda">
            <input
              className={inputClass}
              value={payForm.moneda}
              onChange={(e) =>
                setPayForm({ ...payForm, moneda: e.target.value })
              }
            />
          </FormField>
          <FormField label="Estado">
            <input
              className={inputClass}
              value={payForm.status}
              onChange={(e) =>
                setPayForm({ ...payForm, status: e.target.value })
              }
            />
          </FormField>
        </div>
        <div className="mt-4 flex justify-end gap-2">
          <button className={btnSecondary} onClick={() => setPayOpen(false)}>
            Cancelar
          </button>
          <button
            className={btnPrimary}
            disabled={
              !payForm.transactionId ||
              !payForm.precio ||
              registrarPago.isPending
            }
            onClick={() => registrarPago.mutate()}
          >
            Registrar
          </button>
        </div>
      </Modal>
    </div>
  );
}
