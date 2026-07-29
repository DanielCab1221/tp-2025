import { Navigate, Route, Routes } from "react-router-dom";
import { Layout } from "./components/Layout";
import { HomePage } from "./pages/HomePage";
import { BancosPage } from "./pages/usuarios/BancosPage";
import { HuespedesPage } from "./pages/usuarios/HuespedesPage";
import { HuespedDetailPage } from "./pages/usuarios/HuespedDetailPage";
import { PropietariosPage } from "./pages/usuarios/PropietariosPage";
import { PropietarioDetailPage } from "./pages/usuarios/PropietarioDetailPage";
import { HotelesPage } from "./pages/gestion/HotelesPage";
import { HotelDetailPage } from "./pages/gestion/HotelDetailPage";
import { TiposHabitacionPage } from "./pages/gestion/TiposHabitacionPage";
import { HabitacionesPage } from "./pages/gestion/HabitacionesPage";
import { HabitacionDetailPage } from "./pages/gestion/HabitacionDetailPage";
import { TarifasPage } from "./pages/gestion/TarifasPage";
import { DisponibilidadPage } from "./pages/reservas/DisponibilidadPage";
import { ReservasPage } from "./pages/reservas/ReservasPage";
import { ReservaDetailPage } from "./pages/reservas/ReservaDetailPage";
import { BloqueosPage } from "./pages/reservas/BloqueosPage";

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<HomePage />} />

        <Route path="usuarios/bancos" element={<BancosPage />} />
        <Route path="usuarios/huespedes" element={<HuespedesPage />} />
        <Route path="usuarios/huespedes/:id" element={<HuespedDetailPage />} />
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
  );
}
