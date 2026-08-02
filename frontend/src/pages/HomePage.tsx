import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { userSvc } from "../api/userSvc";
import { gestionSvc } from "../api/gestionSvc";
import { reservasSvc } from "../api/reservasSvc";
import { PageHeader } from "../components/ui";

const cards = [
  {
    title: "Usuarios",
    subtitle: "user-svc · MySQL",
    links: [
      { to: "/usuarios/bancos", label: "Bancos" },
      { to: "/usuarios/huespedes", label: "Huéspedes" },
      { to: "/usuarios/propietarios", label: "Propietarios" },
    ],
  },
  {
    title: "Gestión Hotelera",
    subtitle: "gestion-svc · PostgreSQL",
    links: [
      { to: "/gestion/hoteles", label: "Hoteles" },
      { to: "/gestion/tipos-habitacion", label: "Tipos de Habitación" },
      { to: "/gestion/habitaciones", label: "Habitaciones" },
      { to: "/gestion/tarifas", label: "Tarifas" },
    ],
  },
  {
    title: "Reservas",
    subtitle: "reservas-svc · MongoDB",
    links: [
      { to: "/reservas/disponibilidad", label: "Buscar disponibilidad" },
      { to: "/reservas", label: "Reservas" },
      { to: "/reservas/bloqueos", label: "Bloqueos" },
    ],
  },
];

const swaggerLinks = [
  {
    label: "user-svc",
    url: `${import.meta.env.VITE_USER_SVC_URL}/swagger-ui.html`,
  },
  {
    label: "gestion-svc",
    url: `${import.meta.env.VITE_GESTION_SVC_URL}/swagger-ui.html`,
  },
  {
    label: "reservas-svc",
    url: `${import.meta.env.VITE_RESERVAS_SVC_URL}/swagger-ui.html`,
  },
];

const adminLinks = [
  { label: "phpMyAdmin · MySQL", url: "http://localhost:6080" },
  { label: "pgAdmin · PostgreSQL", url: "http://localhost:6081" },
  { label: "mongo-express · MongoDB", url: "http://localhost:6091" },
  { label: "RabbitMQ Management", url: "http://localhost:15672" },
];

function ExternalLinkList({
  links,
}: {
  links: { label: string; url: string }[];
}) {
  return (
    <ul className="space-y-1">
      {links.map((link) => (
        <li key={link.url}>
          <a
            href={link.url}
            target="_blank"
            rel="noopener noreferrer"
            className="text-sm text-indigo-600 hover:underline dark:text-indigo-400"
          >
            {link.label} ↗
          </a>
        </li>
      ))}
    </ul>
  );
}

function useHealth(name: string, check: () => Promise<unknown>) {
  return useQuery({
    queryKey: ["health", name],
    queryFn: async () => {
      await check();
      return true;
    },
    retry: 0,
    staleTime: 30_000,
  });
}

function HealthDot({ status }: { status: "loading" | "ok" | "down" }) {
  const color =
    status === "ok"
      ? "bg-green-500"
      : status === "down"
        ? "bg-red-500"
        : "bg-gray-300 animate-pulse";
  const label =
    status === "ok"
      ? "en línea"
      : status === "down"
        ? "no responde"
        : "chequeando…";
  return (
    <span className="inline-flex items-center gap-1.5 text-xs text-gray-500 dark:text-gray-400">
      <span className={`h-2 w-2 rounded-full ${color}`} />
      {label}
    </span>
  );
}

export function HomePage() {
  const userHealth = useHealth("user-svc", userSvc.listarBancos);
  const gestionHealth = useHealth(
    "gestion-svc",
    gestionSvc.listarTiposHabitacion,
  );
  const reservasHealth = useHealth(
    "reservas-svc",
    reservasSvc.listarHabitaciones,
  );

  const healthByTitle: Record<string, ReturnType<typeof useHealth>> = {
    Usuarios: userHealth,
    "Gestión Hotelera": gestionHealth,
    Reservas: reservasHealth,
  };

  return (
    <div>
      <PageHeader
        title="Panel de testing — TP DAN"
        description="Un tablero para ejercitar user-svc, gestion-svc y reservas-svc a mano, sin curl ni Postman."
      />
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        {cards.map((card) => {
          const health = healthByTitle[card.title];
          const status = health.isLoading
            ? "loading"
            : health.isError
              ? "down"
              : "ok";
          return (
            <div
              key={card.title}
              className="rounded-lg border border-gray-200 bg-white p-4 dark:border-gray-800 dark:bg-gray-900"
            >
              <div className="mb-1 flex items-center justify-between">
                <h2 className="font-semibold text-gray-900 dark:text-gray-100">
                  {card.title}
                </h2>
                <HealthDot status={status} />
              </div>
              <p className="mb-3 text-xs text-gray-400">{card.subtitle}</p>
              <ul className="space-y-1">
                {card.links.map((link) => (
                  <li key={link.to}>
                    <Link
                      to={link.to}
                      className="text-sm text-indigo-600 hover:underline dark:text-indigo-400"
                    >
                      {link.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          );
        })}
      </div>

      <div className="mt-4 rounded-lg border border-gray-200 bg-white p-4 dark:border-gray-800 dark:bg-gray-900">
        <h2 className="mb-3 font-semibold text-gray-900 dark:text-gray-100">
          Enlaces útiles
        </h2>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <p className="mb-2 text-xs font-medium uppercase tracking-wide text-gray-400">
              Documentación de API (Swagger)
            </p>
            <ExternalLinkList links={swaggerLinks} />
          </div>
          <div>
            <p className="mb-2 text-xs font-medium uppercase tracking-wide text-gray-400">
              Consolas de administración
            </p>
            <ExternalLinkList links={adminLinks} />
          </div>
        </div>
      </div>
    </div>
  );
}
