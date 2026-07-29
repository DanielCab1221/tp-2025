import { test, expect } from "@playwright/test";

// Recorre el ciclo de vida completo de una reserva contra el backend real
// (requiere el stack de infra/docker-compose.yml arriba): crea un hotel,
// una habitación y una tarifa en gestion-svc, un huésped en user-svc,
// busca disponibilidad y reserva en reservas-svc, paga el 50%, hace
// checkin, deja el review del huésped, hace checkout (queda ADEUDADA) y
// termina de pagar (FINALIZADA). Usa nombres únicos por corrida para no
// pisar datos de corridas anteriores.
test("camino feliz: hotel -> reserva -> pago -> checkin/checkout -> finalizada", async ({
  page,
}) => {
  const suffix = Date.now();
  const hotelNombre = `Hotel E2E ${suffix}`;
  const huespedNombre = `Huesped E2E ${suffix}`;
  const email = `e2e${suffix}@mail.com`;
  const dni = String(20000000 + (suffix % 9000000));
  const cuit = String(30000000000 + (suffix % 900000000));

  // 1. Crear hotel (gestion-svc)
  await page.goto("/gestion/hoteles");
  await page.getByRole("button", { name: "+ Nuevo hotel" }).click();
  let dialog = page.getByRole("dialog", { name: "Nuevo hotel" });
  await dialog.getByLabel("Nombre").fill(hotelNombre);
  await dialog.getByLabel("CUIT").fill(cuit);
  await dialog.getByLabel("Domicilio").fill("Calle Falsa 123");
  // reservas-svc explota si el hotel no tiene lat/long (ver bug reportado
  // aparte); las completamos para poder probar el resto del flujo.
  await dialog.getByLabel("Latitud").fill("-31.6333");
  await dialog.getByLabel("Longitud").fill("-60.7");
  await dialog.getByRole("button", { name: "Crear hotel" }).click();
  await expect(page.getByText(hotelNombre)).toBeVisible();

  // 2. Crear una habitación tipo SINGLE para ese hotel
  await page.goto("/gestion/habitaciones");
  await page.getByRole("button", { name: "+ Nueva habitación" }).click();
  dialog = page.getByRole("dialog", { name: "Nueva habitación" });
  await dialog.getByLabel("Número").fill("101");
  await dialog.getByLabel("Piso").fill("1");
  await dialog.getByLabel("Hotel").selectOption({ label: hotelNombre });
  await dialog
    .getByLabel("Tipo de habitación")
    .selectOption({ label: "SINGLE" });
  await dialog.getByRole("button", { name: "Crear habitación" }).click();
  await expect(
    page.getByRole("row", { name: new RegExp(hotelNombre) }),
  ).toBeVisible();

  // 3. Tarifa para SINGLE: $40.000/noche
  await page.goto("/gestion/tarifas");
  await page.getByRole("button", { name: "+ Nueva tarifa" }).click();
  dialog = page.getByRole("dialog", { name: "Nueva tarifa" });
  await dialog
    .getByLabel("Tipo de habitación")
    .selectOption({ label: "SINGLE" });
  await dialog.getByLabel("Precio por noche").fill("40000");
  await dialog.getByRole("button", { name: "Crear tarifa" }).click();
  await expect(page.getByRole("heading", { name: "Tarifas" })).toBeVisible();

  // 4. Crear huésped (user-svc)
  await page.goto("/usuarios/huespedes");
  await page.getByRole("button", { name: "+ Nuevo huésped" }).click();
  dialog = page.getByRole("dialog", { name: "Nuevo huésped" });
  await dialog.getByLabel("Nombre", { exact: true }).fill(huespedNombre);
  await dialog.getByLabel("Email").fill(email);
  await dialog.getByLabel("Teléfono").fill("3410000000");
  await dialog.getByLabel("DNI").fill(dni);
  await dialog.getByLabel("Fecha de nacimiento").fill("1990-01-01");
  await dialog.getByLabel("Número (16 dígitos)").fill("4111111111111111");
  await dialog
    .getByLabel("Nombre del titular")
    .fill(huespedNombre.toUpperCase());
  await dialog.getByLabel("Vencimiento (MM/YY)").fill("12/29");
  await dialog.getByLabel("CVC").fill("123");
  await dialog.getByLabel("Banco emisor").selectOption({ index: 1 });
  await dialog.getByRole("button", { name: "Crear huésped" }).click();
  await expect(dialog).not.toBeVisible(); // la mutación es async: esperamos a que cierre

  // Obtenemos el id numérico del huésped entrando a su detalle (no hay
  // columna de id en la tabla, y reservas-svc lo necesita como string).
  // Buscamos por nombre en vez de asumir que aparece en la primera página:
  // ya hay más de 10 usuarios acumulados entre corridas de este test.
  await page.getByLabel("Nombre", { exact: true }).fill(huespedNombre);
  await page.getByRole("row", { name: new RegExp(huespedNombre) }).click();
  await page.waitForURL(/\/usuarios\/huespedes\/\d+/);
  const huespedId = page.url().match(/\/usuarios\/huespedes\/(\d+)/)?.[1];
  expect(huespedId).toBeTruthy();

  // 5. Buscar disponibilidad y reservar (reservas-svc)
  await page.goto("/reservas/disponibilidad");
  await page.getByLabel("Check-in *").fill("2026-12-01");
  await page.getByLabel("Check-out *").fill("2026-12-05");
  await page.getByRole("button", { name: "Buscar" }).click();

  const fila = page.getByRole("row", { name: new RegExp(hotelNombre) });
  await expect(fila).toBeVisible();
  await fila.getByRole("button", { name: "Reservar" }).click();

  dialog = page.getByRole("dialog", {
    name: new RegExp(`Reservar.*${hotelNombre}`),
  });
  await dialog.getByLabel("ID de usuario (user-svc)").fill(huespedId!);
  await dialog.getByLabel("Nombre y apellido").fill(huespedNombre);
  await dialog.getByLabel("Email").fill(email);
  await dialog.getByRole("button", { name: "Confirmar reserva" }).click();

  await page.waitForURL(/\/reservas\/[a-f0-9]{24}/);
  await expect(page.getByText("RESERVADA")).toBeVisible();

  // 6. Pagar el 50% ($80.000 de $160.000 = 4 noches x $40.000) -> confirma sola
  await page.getByRole("button", { name: "+ Agregar pago" }).click();
  dialog = page.getByRole("dialog", { name: "Registrar pago" });
  await dialog.getByLabel("ID de transacción").fill(`e2e-tx-1-${suffix}`);
  await dialog.getByLabel("Monto").fill("80000");
  await dialog.getByRole("button", { name: "Registrar" }).click();
  await expect(page.getByText("CONFIRMADA")).toBeVisible();

  // 7. Check-in
  await page.getByRole("button", { name: "Check-in" }).click();
  await expect(page.getByText("EFECTUADA")).toBeVisible();

  // 8. Review del huésped (obligatorio antes del checkout)
  await page.getByLabel("Comentario").first().fill("Todo excelente (E2E)");
  await page.getByRole("button", { name: "Publicar review" }).first().click();
  await expect(page.getByText("Todo excelente (E2E)")).toBeVisible();

  // 9. Check-out con 50% pagado -> ADEUDADA
  await page.getByRole("button", { name: "Check-out" }).click();
  await expect(page.getByText("ADEUDADA")).toBeVisible();

  // 10. Pagar el resto -> FINALIZADA
  await page.getByRole("button", { name: "+ Agregar pago" }).click();
  dialog = page.getByRole("dialog", { name: "Registrar pago" });
  await dialog.getByLabel("ID de transacción").fill(`e2e-tx-2-${suffix}`);
  await dialog.getByLabel("Monto").fill("80000");
  await dialog.getByRole("button", { name: "Registrar" }).click();
  await expect(page.getByText("FINALIZADA")).toBeVisible();
});
