import { lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { Layout } from "./components/Layout";
import { Spinner } from "./components/ui";

const HomePage = lazy(() =>
  import("./pages/HomePage").then((m) => ({ default: m.HomePage })),
);
const BancosPage = lazy(() =>
  import("./pages/usuarios/BancosPage").then((m) => ({
    default: m.BancosPage,
  })),
);
const HuespedesPage = lazy(() =>
  import("./pages/usuarios/HuespedesPage").then((m) => ({
    default: m.HuespedesPage,
  })),
);
const HuespedDetailPage = lazy(() =>
  import("./pages/usuarios/HuespedDetailPage").then((m) => ({
    default: m.HuespedDetailPage,
  })),
);
const PropietariosPage = lazy(() =>
  import("./pages/usuarios/PropietariosPage").then((m) => ({
    default: m.PropietariosPage,
  })),
);
const PropietarioDetailPage = lazy(() =>
  import("./pages/usuarios/PropietarioDetailPage").then((m) => ({
    default: m.PropietarioDetailPage,
  })),
);
const HotelesPage = lazy(() =>
  import("./pages/gestion/HotelesPage").then((m) => ({
    default: m.HotelesPage,
  })),
);
const HotelDetailPage = lazy(() =>
  import("./pages/gestion/HotelDetailPage").then((m) => ({
    default: m.HotelDetailPage,
  })),
);
const TiposHabitacionPage = lazy(() =>
  import("./pages/gestion/TiposHabitacionPage").then((m) => ({
    default: m.TiposHabitacionPage,
  })),
);
const HabitacionesPage = lazy(() =>
  import("./pages/gestion/HabitacionesPage").then((m) => ({
    default: m.HabitacionesPage,
  })),
);
const HabitacionDetailPage = lazy(() =>
  import("./pages/gestion/HabitacionDetailPage").then((m) => ({
    default: m.HabitacionDetailPage,
  })),
);
const TarifasPage = lazy(() =>
  import("./pages/gestion/TarifasPage").then((m) => ({
    default: m.TarifasPage,
  })),
);
const DisponibilidadPage = lazy(() =>
  import("./pages/reservas/DisponibilidadPage").then((m) => ({
    default: m.DisponibilidadPage,
  })),
);
const ReservasPage = lazy(() =>
  import("./pages/reservas/ReservasPage").then((m) => ({
    default: m.ReservasPage,
  })),
);
const ReservaDetailPage = lazy(() =>
  import("./pages/reservas/ReservaDetailPage").then((m) => ({
    default: m.ReservaDetailPage,
  })),
);
const BloqueosPage = lazy(() =>
  import("./pages/reservas/BloqueosPage").then((m) => ({
    default: m.BloqueosPage,
  })),
);

export default function App() {
  return (
    <Suspense fallback={<Spinner />}>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<HomePage />} />

          <Route path="usuarios/bancos" element={<BancosPage />} />
          <Route path="usuarios/huespedes" element={<HuespedesPage />} />
          <Route
            path="usuarios/huespedes/:id"
            element={<HuespedDetailPage />}
          />
          <Route path="usuarios/propietarios" element={<PropietariosPage />} />
          <Route
            path="usuarios/propietarios/:id"
            element={<PropietarioDetailPage />}
          />

          <Route path="gestion/hoteles" element={<HotelesPage />} />
          <Route path="gestion/hoteles/:id" element={<HotelDetailPage />} />
          <Route
            path="gestion/tipos-habitacion"
            element={<TiposHabitacionPage />}
          />
          <Route path="gestion/habitaciones" element={<HabitacionesPage />} />
          <Route
            path="gestion/habitaciones/:id"
            element={<HabitacionDetailPage />}
          />
          <Route path="gestion/tarifas" element={<TarifasPage />} />

          <Route
            path="reservas/disponibilidad"
            element={<DisponibilidadPage />}
          />
          <Route path="reservas" element={<ReservasPage />} />
          <Route path="reservas/:id" element={<ReservaDetailPage />} />
          <Route path="reservas/bloqueos" element={<BloqueosPage />} />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </Suspense>
  );
}
