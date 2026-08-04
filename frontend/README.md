# Frontend — Panel de testing TP DAN

Dashboard en React + Vite + TypeScript para ejercitar `user-svc`, `gestion-svc` y `reservas-svc` a mano, sin curl ni Postman. **No es parte del enunciado del TP** — es una herramienta interna de testing/demo construida sobre la marcha.

Ver el [README del repo raíz](../README.md) para la arquitectura general y cómo levantar el backend.

## Qué incluye

- CRUD contra los 3 microservicios (usuarios/bancos/tarjetas, hoteles/habitaciones/tarifas, reservas/pagos/reviews).
- Un **Bus de eventos** (panel flotante) que muestra en vivo, vía Server-Sent Events, la mensajería asíncrona real entre `gestion-svc` y `reservas-svc` (RabbitMQ) — ver [`../MENSAJERIA.md`](../MENSAJERIA.md).
- Toasts de confirmación y diálogos de confirmación propios (sin `alert()`/`confirm()` nativos del navegador).

## Levantar en desarrollo

Con el backend ya corriendo (ver README raíz):

```bash
npm install
npm run dev
```

Por defecto apunta a `localhost:8081/8082/8083`; las URLs son configurables en `.env` (`VITE_USER_SVC_URL`, `VITE_GESTION_SVC_URL`, `VITE_RESERVAS_SVC_URL`).

## Scripts

| Script                | Qué hace                                              |
| ---------------------- | ------------------------------------------------------ |
| `npm run dev`          | Servidor de desarrollo con HMR                          |
| `npm run build`        | Typecheck + build de producción                         |
| `npm run preview`      | Sirve el build de producción localmente                 |
| `npm test`             | Tests unitarios (Vitest)                                |
| `npm run test:watch`   | Tests unitarios en modo watch                           |
| `npm run test:e2e`     | Tests end-to-end contra el backend real (Playwright)     |
| `npm run test:e2e:ui`  | Tests e2e con la UI interactiva de Playwright            |
| `npm run lint`         | Lint (oxlint)                                           |
| `npm run format`       | Formatea con Prettier                                   |
| `npm run format:check` | Verifica formato sin modificar archivos                 |

> Los tests e2e (`e2e/*.spec.ts`) pegan contra el backend real corriendo en `localhost:8081/8082/8083` — necesitás la infra de Docker levantada. Corren en serie (`workers: 1`) porque comparten estado mutable en las mismas bases; correrlos en paralelo genera fallos intermitentes por timing.

## Estructura

```
src/
├── api/         # Clientes HTTP por servicio (userSvc, gestionSvc, reservasSvc)
├── components/  # UI compartida (Modal, DataTable, Layout, EventBusPanel, ErrorBoundary)
├── hooks/       # Hooks compartidos (useEventBus)
├── lib/         # Utilidades transversales (http, toast, confirm)
├── pages/       # Una carpeta por servicio (usuarios/gestion/reservas) + HomePage
└── types/       # Tipos TS que reflejan los DTOs de cada backend
```

Las páginas se cargan con `React.lazy` por ruta (ver `App.tsx`), así que el bundle inicial no incluye el código de pantallas que todavía no se visitaron.

## Stack

React 19 · TypeScript · Vite · TailwindCSS · TanStack Query · React Router · Vitest · Playwright · oxlint · Prettier.
