import { useEffect, useState } from "react";
import type { EventoBus } from "../types/eventBus";

const MAX_EVENTOS = 30;

export type EventBusStatus = "conectando" | "conectado" | "desconectado";

export function useEventBus() {
  const [eventos, setEventos] = useState<EventoBus[]>([]);
  const [status, setStatus] = useState<EventBusStatus>("conectando");

  useEffect(() => {
    const url = `${import.meta.env.VITE_RESERVAS_SVC_URL}/eventos/stream`;
    const source = new EventSource(url);

    source.onopen = () => setStatus("conectado");
    source.onerror = () => setStatus("desconectado");
    source.addEventListener("evento", (e) => {
      const evento = JSON.parse((e as MessageEvent<string>).data) as EventoBus;
      setEventos((prev) => [evento, ...prev].slice(0, MAX_EVENTOS));
    });

    return () => source.close();
  }, []);

  return { eventos, status };
}
