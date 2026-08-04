import { NavLink, Outlet, useLocation } from "react-router-dom";
import { ErrorBoundary } from "./ErrorBoundary";
import { EventBusPanel } from "./EventBusPanel";

interface NavLinkItem {
  to: string;
  label: string;
  end?: boolean;
}

interface NavSection {
  title: string;
  links: NavLinkItem[];
}

const sections: NavSection[] = [
  {
    title: "Usuarios · user-svc",
    links: [
      { to: "/usuarios/bancos", label: "Bancos" },
      { to: "/usuarios/huespedes", label: "Huéspedes" },
      { to: "/usuarios/propietarios", label: "Propietarios" },
    ],
  },
  {
    title: "Gestión Hotelera · gestion-svc",
    links: [
      { to: "/gestion/hoteles", label: "Hoteles" },
      { to: "/gestion/tipos-habitacion", label: "Tipos de Habitación" },
      { to: "/gestion/habitaciones", label: "Habitaciones" },
      { to: "/gestion/tarifas", label: "Tarifas" },
    ],
  },
  {
    title: "Reservas · reservas-svc",
    links: [
      { to: "/reservas/disponibilidad", label: "Buscar disponibilidad" },
      { to: "/reservas", label: "Reservas", end: true },
      { to: "/reservas/bloqueos", label: "Bloqueos" },
    ],
  },
];

export function Layout() {
  const location = useLocation();

  return (
    <div className="flex min-h-screen bg-gray-50 text-gray-900 dark:bg-gray-950 dark:text-gray-100">
      <aside className="w-60 shrink-0 border-r border-gray-200 bg-white p-4 dark:border-gray-800 dark:bg-gray-900">
        <NavLink to="/" className="mb-6 block">
          <span className="block text-lg font-semibold">TP DAN</span>
          <span className="block text-xs text-gray-500 dark:text-gray-400">
            Panel de testing
          </span>
        </NavLink>
        <nav className="space-y-6">
          {sections.map((section) => (
            <div key={section.title}>
              <h3 className="mb-2 px-2 text-xs font-semibold uppercase tracking-wide text-gray-400 dark:text-gray-500">
                {section.title}
              </h3>
              <ul className="space-y-0.5">
                {section.links.map((link) => (
                  <li key={link.to}>
                    <NavLink
                      to={link.to}
                      end={link.end ?? false}
                      className={({ isActive }) =>
                        `block rounded-md px-2 py-1.5 text-sm ${
                          isActive
                            ? "bg-indigo-50 font-medium text-indigo-700 dark:bg-indigo-900/40 dark:text-indigo-300"
                            : "text-gray-600 hover:bg-gray-100 dark:text-gray-300 dark:hover:bg-gray-800"
                        }`
                      }
                    >
                      {link.label}
                    </NavLink>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </nav>
      </aside>
      <main className="max-w-5xl flex-1 overflow-x-hidden p-6 pb-24">
        <ErrorBoundary key={location.pathname}>
          <Outlet />
        </ErrorBoundary>
      </main>
      <EventBusPanel />
    </div>
  );
}
