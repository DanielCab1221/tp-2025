import { test, expect, type APIRequestContext } from "@playwright/test";

// Estos tests prueban que las reglas de negocio se respetan, no solo el
// camino feliz (ver golden-path.spec.ts). Arman los datos previos pegándole
// directo a las APIs (más rápido y estable que clickear todo el alta), y
// usan el browser solo para la acción que realmente se quiere probar.

const USER_SVC = "http://localhost:8081";
const GESTION_SVC = "http://localhost:8083";
const RESERVAS_SVC = "http://localhost:8082";

async function esperarHabitacionEnReservas(
  request: APIRequestContext,
  hotelNombre: string,
  intentos = 20,
) {
  for (let i = 0; i < intentos; i++) {
    const res = await request.get(`${RESERVAS_SVC}/habitaciones`);
    const data = await res.json();
    const match = data.find(
      (h: { hotel?: { nombre?: string } }) => h.hotel?.nombre === hotelNombre,
    );
    if (match) return match as { id: string; precioNoche: number };
    await new Promise((r) => setTimeout(r, 500));
  }
  throw new Error(
    `La habitación del hotel "${hotelNombre}" no llegó a reservas-svc a tiempo (RabbitMQ)`,
  );
}

async function crearReservaEfectuable(
  request: APIRequestContext,
  suffix: number,
) {
  const hotelNombre = `Hotel Reglas ${suffix}`;
  const hotelRes = await request.post(`${GESTION_SVC}/hoteles`, {
    data: {
      nombre: hotelNombre,
      cuit: String(30100000000 + (suffix % 900000000)),
      domicilio: "Calle Reglas 1",
      latitud: -31.6,
      longitud: -60.7,
      categoria: 3,
      cerrado: false,
    },
  });
  const hotel = await hotelRes.json();

  await request.post(`${GESTION_SVC}/habitaciones`, {
    data: {
      numero: 1,
      piso: 1,
      tipoHabitacion: { id: 1 }, // SINGLE
      hotel: { id: hotel.id },
      disponible: true,
    },
  });
  await request.post(`${GESTION_SVC}/tarifas`, {
    data: { tipoHabitacion: { id: 1 }, precioNoche: 40000 },
  });

  const habitacion = await esperarHabitacionEnReservas(request, hotelNombre);

  const reservaRes = await request.post(`${RESERVAS_SVC}/reservas`, {
    data: {
      idHabitacion: habitacion.id,
      checkIn: "2027-01-10T00:00:00Z",
      checkOut: "2027-01-14T00:00:00Z",
      huesped: {
        idUsuario: "999",
        nombreApellido: `Huesped Reglas ${suffix}`,
        email: `reglas${suffix}@mail.com`,
      },
    },
  });
  const reserva = await reservaRes.json();
  return {
    reservaId: reserva._id as string,
    precioTotal: reserva.precioTotal as number,
  };
}

test("no se puede borrar un banco referenciado por una tarjeta (409)", async ({
  page,
  request,
}) => {
  const suffix = Date.now();
  const bancoNombre = `Banco E2E ${suffix}`;

  const bancoRes = await request.post(`${USER_SVC}/bancos`, {
    data: { nombre: bancoNombre },
  });
  const banco = await bancoRes.json();

  // Un huésped con tarjeta en ese banco es lo que lo deja "referenciado".
  await request.post(`${USER_SVC}/users/huesped`, {
    data: {
      nombre: `Huesped Banco ${suffix}`,
      email: `bancoref${suffix}@mail.com`,
      telefono: "3410000000",
      dni: String(21000000 + (suffix % 8000000)),
      fechaNacimiento: "1990-01-01",
      numeroCC: "4111111111111111",
      nombreTitular: "TEST",
      fechaVencimientoCC: "12/29",
      cvcCC: "123",
      esPrincipalCC: true,
      idBanco: banco.id,
    },
  });

  await page.goto("/usuarios/bancos");
  const fila = page.getByRole("row", { name: new RegExp(bancoNombre) });
  await expect(fila).toBeVisible();
  await fila.getByRole("button", { name: "Borrar" }).click();

  // El botón de la fila abre el modal de confirmación propio del dashboard
  // (ya no es un confirm() nativo del navegador); hay que confirmarlo ahí.
  const confirmDialog = page.getByRole("dialog", { name: "Confirmar acción" });
  await confirmDialog.getByRole("button", { name: "Borrar" }).click();

  await expect(
    page.getByText(
      "No se puede eliminar el banco porque esta referenciado por una tarjeta o cuenta bancaria",
    ),
  ).toBeVisible();
  // Sigue en la lista: el borrado no se aplicó.
  await expect(fila).toBeVisible();
});

test("la tarjeta principal no se puede borrar desde la UI", async ({
  page,
  request,
}) => {
  const suffix = Date.now();
  const huespedNombre = `Huesped Tarjeta ${suffix}`;
  const dni = String(22000000 + (suffix % 7000000));

  await request.post(`${USER_SVC}/users/huesped`, {
    data: {
      nombre: huespedNombre,
      email: `tarjeta${suffix}@mail.com`,
      telefono: "3410000000",
      dni,
      fechaNacimiento: "1990-01-01",
      numeroCC: "4111111111111111",
      nombreTitular: "TEST",
      fechaVencimientoCC: "12/29",
      cvcCC: "123",
      esPrincipalCC: true,
      idBanco: 1,
    },
  });

  await page.goto("/usuarios/huespedes");
  await page.getByLabel("Buscar por").selectOption({ label: "DNI (contiene)" });
  // getByLabel("DNI") es ambiguo: el <select> de arriba incluye el texto de
  // sus <option> ("DNI (contiene)") en su nombre accesible.
  await page.getByRole("textbox", { name: "DNI" }).fill(dni);
  await page.getByRole("row", { name: new RegExp(huespedNombre) }).click();
  await page.waitForURL(/\/usuarios\/huespedes\/\d+/);

  const borrarTarjeta = page.getByRole("button", {
    name: "Borrar",
    exact: true,
  });
  await expect(borrarTarjeta).toBeVisible();
  await expect(borrarTarjeta).toBeDisabled();
});

test("checkout sin review del hotel y con pago completo pasa a ADEUDADA", async ({
  page,
  request,
}) => {
  const suffix = Date.now();
  const { reservaId, precioTotal } = await crearReservaEfectuable(
    request,
    suffix,
  );

  // Completar el 100% del pago (confirma y luego alcanza el pago completo) y
  // hacer checkin, todo por API.
  await request.post(`${RESERVAS_SVC}/reservas/${reservaId}/pagos`, {
    data: {
      method: "TARJETA",
      transactionId: `setup-${suffix}`,
      amount: { precio: precioTotal, moneda: "ARS" },
      status: "APROBADO",
    },
  });
  await request.patch(`${RESERVAS_SVC}/reservas/${reservaId}/checkin`);

  await page.goto(`/reservas/${reservaId}`);
  await expect(page.getByText("EFECTUADA")).toBeVisible();

  // El pago está completo, pero el dueño todavía no dejó su review: aunque el
  // check-out se puede efectuar, la reserva queda ADEUDADA hasta que lo haga.
  await page.getByRole("button", { name: "Check-out" }).click();
  await expect(page.getByText("ADEUDADA")).toBeVisible();
});

test("no se puede cancelar una reserva que ya tiene pagos registrados", async ({
  page,
  request,
}) => {
  const suffix = Date.now();
  const { reservaId, precioTotal } = await crearReservaEfectuable(
    request,
    suffix,
  );

  await request.post(`${RESERVAS_SVC}/reservas/${reservaId}/pagos`, {
    data: {
      method: "TARJETA",
      transactionId: `setup-${suffix}`,
      amount: { precio: precioTotal / 2, moneda: "ARS" },
      status: "APROBADO",
    },
  });

  await page.goto(`/reservas/${reservaId}`);
  await expect(page.getByText("CONFIRMADA")).toBeVisible();
  await expect(page.getByRole("button", { name: "Cancelar" })).toBeDisabled();
});
