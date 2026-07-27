# Guía de pruebas end-to-end (ETAPA01 + ETAPA02)

Guía paso a paso para probar manualmente cada funcionalidad de los 3 microservicios. Todos los ejemplos usan `curl.exe` en sintaxis PowerShell (`^` para saltos de línea, comillas escapadas con `\"`). Si usás otra shell, adaptá la sintaxis de saltos de línea y de escape (por ejemplo `\` en bash y comillas simples para el JSON).

## Requisitos previos

Levantar toda la infraestructura y los 3 servicios:

```
docker compose -f infra/docker-compose.yml up -d --build
```

Servicios expuestos:

| Servicio      | Puerto | Base de datos              |
|---------------|--------|-----------------------------|
| `user-svc`    | 8081   | MySQL (`users`)             |
| `reservas-svc`| 8082   | MongoDB (`reservas`)        |
| `gestion-svc` | 8083   | PostgreSQL (`appdb`)        |

UIs de administración: phpMyAdmin `http://localhost:6080`, pgAdmin `http://localhost:6081`, mongo-express `http://localhost:6091`, RabbitMQ management `http://localhost:15672` (`guest`/`guest`).

Esperar unos segundos después del `up` a que los 3 servicios terminen de arrancar (Spring Boot tarda unos segundos en levantar). Se puede confirmar con:

```
curl.exe http://localhost:8081/bancos
curl.exe http://localhost:8083/tipos-habitacion
curl.exe http://localhost:8082/habitaciones
```

Si alguno todavía no responde, esperar unos segundos y reintentar.

---

## 1. `user-svc` (ETAPA01) — puerto 8081

### 1.1 Bancos (CRUD completo)

Crear banco:

```
curl.exe -X POST http://localhost:8081/bancos ^
  -H "Content-Type: application/json" ^
  -d "{\"nombre\":\"Banco Nacion\"}"
```

Listar bancos / obtener uno por id / actualizar:

```
curl.exe http://localhost:8081/bancos
curl.exe http://localhost:8081/bancos/1
curl.exe -X PUT http://localhost:8081/bancos/1 -H "Content-Type: application/json" -d "{\"nombre\":\"Banco Actualizado\"}"
```

Eliminar (si no está referenciado por ninguna tarjeta/cuenta bancaria, responde 204; si está referenciado, responde **409 Conflict**, no 500):

```
curl.exe -X DELETE http://localhost:8081/bancos/1
```

### 1.2 Huéspedes

Crear huésped (incluye los datos de la tarjeta principal en el mismo request, campos planos, no anidados):

```
curl.exe -X POST http://localhost:8081/users/huesped ^
  -H "Content-Type: application/json" ^
  -d "{\"nombre\":\"Juan Perez\",\"email\":\"juan@mail.com\",\"telefono\":\"3411234567\",\"dni\":\"30123456\",\"fechaNacimiento\":\"1990-05-15\",\"numeroCC\":\"4111111111111111\",\"nombreTitular\":\"JUAN PEREZ\",\"fechaVencimientoCC\":\"12/27\",\"cvcCC\":\"123\",\"esPrincipalCC\":true,\"idBanco\":1}"
```

Responde `201 Created` sin body. Buscar por nombre, por DNI exacto o por DNI parcial:

```
curl.exe "http://localhost:8081/users?nombre=juan&page=0&size=10"
curl.exe "http://localhost:8081/users/dni/30123456"
curl.exe "http://localhost:8081/users/buscar-dni?dni=301&page=0&size=10"
```

Actualizar (parcial, cualquier subconjunto de campos; se aceptan `nombre`, `email`, `telefono`, `fechaNacimiento`):

```
curl.exe -X PATCH http://localhost:8081/users/huesped/1 -H "Content-Type: application/json" -d "{\"telefono\":\"3411119999\"}"
```

Eliminar (borra en cascada sus tarjetas de crédito):

```
curl.exe -X DELETE http://localhost:8081/users/huesped/1
```

### 1.3 Propietarios

Crear (con cuenta bancaria; `idHotel` va `null` hasta que se cree el hotel en `gestion-svc`):

```
curl.exe -X POST http://localhost:8081/users/propietario ^
  -H "Content-Type: application/json" ^
  -d "{\"nombre\":\"Carlos Gomez\",\"email\":\"carlos@mail.com\",\"telefono\":\"3419998888\",\"dni\":\"27123456\",\"idHotel\":null,\"cuentaBancaria\":{\"numeroCuenta\":\"123456789\",\"cbu\":\"2850590940090418135201\",\"alias\":\"carlos.cbu\",\"idBanco\":1}}"
```

Actualizar (parcial; incluye poder setear `idHotel` una vez creado el hotel en ETAPA02):

```
curl.exe -X PATCH http://localhost:8081/users/propietario/2 -H "Content-Type: application/json" -d "{\"idHotel\":1}"
```

**No existe `DELETE /users/propietario/{id}`** (requisito del enunciado: "no se pueden borrar propietarios"). Probarlo debe devolver **405 Method Not Allowed**:

```
curl.exe -X DELETE http://localhost:8081/users/propietario/2
```

### 1.4 Tarjetas de crédito

Agregar una tarjeta a un huésped existente (si se marca `esPrincipal: true`, desmarca automáticamente la anterior):

```
curl.exe -X POST http://localhost:8081/tarjetas/huesped/1 ^
  -H "Content-Type: application/json" ^
  -d "{\"numero\":\"5500000000000004\",\"nombreTitular\":\"JUAN PEREZ\",\"fechaVencimiento\":\"11/26\",\"cvc\":\"456\",\"esPrincipal\":false,\"idBanco\":1}"
```

Listar tarjetas de un huésped:

```
curl.exe http://localhost:8081/tarjetas/huesped/1
```

Cambiar cuál es la principal:

```
curl.exe -X PATCH http://localhost:8081/tarjetas/huesped/1/principal/2
```

Eliminar una tarjeta que **no** es la principal (204). Si se intenta eliminar la principal, responde **409 Conflict**:

```
curl.exe -X DELETE http://localhost:8081/tarjetas/2
```

---

## 2. `gestion-svc` (ETAPA02) — puerto 8083

### 2.1 Tipos de habitación

El catálogo viene precargado con 9 tipos (ids 1 a 9: `SINGLE`, `DOBLE`, `TRIPLE`, etc. — ver `infra/postgres/initdb/01_schema.sql`). El `id` **no se autogenera**, hay que indicarlo explícitamente al crear uno nuevo (si se omite, responde 400 con un mensaje claro):

```
curl.exe http://localhost:8083/tipos-habitacion
curl.exe -X POST http://localhost:8083/tipos-habitacion -H "Content-Type: application/json" -d "{\"id\":10,\"nombre\":\"SUITE\",\"descripcion\":\"Suite presidencial\",\"capacidad\":2}"
```

### 2.2 Hoteles

Crear (CUIT único; si se repite, responde 400):

```
curl.exe -X POST http://localhost:8083/hoteles ^
  -H "Content-Type: application/json" ^
  -d "{\"nombre\":\"Hotel Central\",\"cuit\":\"30-12345678-9\",\"domicilio\":\"San Martin 1234, Rosario\",\"latitud\":-32.9468,\"longitud\":-60.6393,\"telefono\":\"3411112222\",\"correoContacto\":\"hotel@mail.com\",\"categoria\":4}"
```

Obtener y buscar (todos los filtros son opcionales y combinables):

```
curl.exe http://localhost:8083/hoteles/1
curl.exe "http://localhost:8083/hoteles?nombre=Central&categoriaMinima=3&amenity=WIFI"
```

Actualizar: **solo se permite modificar `categoria`, `telefono` y `correoContacto`** (aunque se manden `nombre` o `cuit` en el body, se ignoran):

```
curl.exe -X PUT http://localhost:8083/hoteles/1 -H "Content-Type: application/json" -d "{\"categoria\":5,\"telefono\":\"3410000000\",\"correoContacto\":\"nuevo@mail.com\"}"
```

Amenities del hotel (agregar/reemplazar y quitar uno):

```
curl.exe -X PUT http://localhost:8083/hoteles/1/amenities -H "Content-Type: application/json" -d "[\"WIFI\",\"PILETA\"]"
curl.exe -X DELETE http://localhost:8083/hoteles/1/amenities/PILETA
```

Cerrar el hotel (no existe reapertura ni baja definitiva, es intencional según el enunciado). Al cerrar, todas sus habitaciones quedan `disponible=false` y se dispara el evento asíncrono correspondiente:

```
curl.exe -X PATCH http://localhost:8083/hoteles/1/cerrar
```

**No existe `DELETE /hoteles/{id}`** (405 Method Not Allowed si se prueba).

### 2.3 Habitaciones

Crear (número y piso deben ser mayores a cero; si no, responde 400):

```
curl.exe -X POST http://localhost:8083/habitaciones ^
  -H "Content-Type: application/json" ^
  -d "{\"numero\":101,\"piso\":1,\"tipoHabitacion\":{\"id\":3},\"hotel\":{\"id\":1}}"
```

Obtener, buscar con filtros (todos combinables; el filtro por precio se resuelve contra la tarifa vigente) y consultar la tarifa vigente de una habitación puntual:

```
curl.exe http://localhost:8083/habitaciones/1
curl.exe "http://localhost:8083/habitaciones?tipoHabitacionId=3&capacidadMinima=2&disponible=true&hotelId=1&precioMin=1000&precioMax=10000"
curl.exe http://localhost:8083/habitaciones/1/tarifa-vigente
```

Actualizar / eliminar:

```
curl.exe -X PUT http://localhost:8083/habitaciones/1 -H "Content-Type: application/json" -d "{\"numero\":102,\"piso\":1,\"tipoHabitacion\":{\"id\":3},\"hotel\":{\"id\":1}}"
curl.exe -X DELETE http://localhost:8083/habitaciones/1
```

### 2.4 Tarifas

Crear una tarifa **continua** (sin `fechaFin`, queda vigente indefinidamente hasta que se cree otra del mismo tipo):

```
curl.exe -X POST http://localhost:8083/tarifas ^
  -H "Content-Type: application/json" ^
  -d "{\"fechaInicio\":\"2026-08-01\",\"tipoHabitacion\":{\"id\":3},\"precioNoche\":5000}"
```

Crear una tarifa **promocional** (con `fechaFin`): automáticamente recorta la tarifa continua anterior hasta el día previo, y al terminar la promoción se crea sola una tarifa de continuación con el precio que había antes:

```
curl.exe -X POST http://localhost:8083/tarifas ^
  -H "Content-Type: application/json" ^
  -d "{\"fechaInicio\":\"2026-08-10\",\"fechaFin\":\"2026-08-15\",\"tipoHabitacion\":{\"id\":3},\"precioNoche\":3500}"
```

Listar todas / obtener una / eliminar (al eliminar la vigente, se restaura automáticamente la anterior como vigente; **no se puede eliminar si es la única tarifa del tipo**):

```
curl.exe http://localhost:8083/tarifas
curl.exe http://localhost:8083/tarifas/1
curl.exe -X DELETE http://localhost:8083/tarifas/1
```

**No existe `PUT /tarifas/{id}`** (405 si se prueba: las tarifas no se "editan", se reemplazan creando una nueva).

---

## 3. `reservas-svc` (ETAPA02) — puerto 8082

Los datos de habitaciones y hoteles en `reservas-svc` **no se crean por API propia**: llegan automáticamente por mensajería asíncrona (RabbitMQ) cada vez que se crea/modifica algo en `gestion-svc`. Después de crear una habitación en `gestion-svc`, esperar 1-2 segundos y buscarla acá:

```
curl.exe http://localhost:8082/habitaciones
curl.exe http://localhost:8082/habitaciones/<id-de-mongo>
```

El `id` que usa `reservas-svc` para todo (búsquedas, reservas) es el `_id` de Mongo (un string), **no** el id numérico de `gestion-svc`. Para encontrar la habitación correspondiente a un `habitacionId` de `gestion-svc`, filtrar el listado por el campo `habitacionId`.

### 3.1 Búsqueda de disponibilidad (endpoint central del TP)

```
curl.exe "http://localhost:8082/habitaciones/disponibles?checkIn=2026-08-01&checkOut=2026-08-05"
curl.exe "http://localhost:8082/habitaciones/disponibles?checkIn=2026-08-01&checkOut=2026-08-05&huespedes=2&precioMin=1000&precioMax=8000&categoriaMinima=3&amenities=WIFI&amenities=PILETA"
curl.exe "http://localhost:8082/habitaciones/disponibles?checkIn=2026-08-01&checkOut=2026-08-05&latitud=-32.9468&longitud=-60.6393&distanciaMaximaKm=10"
```

Todos los filtros son opcionales y se combinan con AND. `checkIn`/`checkOut` son obligatorios y `checkIn` debe ser anterior a `checkOut` (400 si no). Una habitación deja de aparecer si: no está `disponible` (hotel cerrado), no tiene capacidad/precio/categoría/amenities que matcheen los filtros pedidos, está fuera del radio geográfico pedido, o tiene una reserva/bloqueo que se solapa con el rango de fechas pedido.

### 3.2 Ciclo de vida de una reserva

Crear (el precio total se calcula como `precioNoche * noches` al momento de crear la reserva; el `idHabitacion` es el `_id` de Mongo, y las fechas van en formato `Instant` ISO-8601):

```
curl.exe -X POST http://localhost:8082/reservas ^
  -H "Content-Type: application/json" ^
  -d "{\"idHabitacion\":\"<id-de-mongo>\",\"checkIn\":\"2026-08-01T00:00:00Z\",\"checkOut\":\"2026-08-05T00:00:00Z\",\"huesped\":{\"idUsuario\":\"huesped-1\",\"nombreApellido\":\"Juan Perez\",\"email\":\"juan@mail.com\"}}"
```

Queda en estado `RESERVADA`. Si se intenta crear otra reserva para la misma habitación con fechas que se solapan, responde 400.

Consultar por id o por usuario:

```
curl.exe http://localhost:8082/reservas/<id-reserva>
curl.exe "http://localhost:8082/reservas?idUsuario=huesped-1"
```

Registrar pagos (acumulativo; al llegar al 50% del total, la reserva pasa sola de `RESERVADA` a `CONFIRMADA`):

```
curl.exe -X POST http://localhost:8082/reservas/<id-reserva>/pagos ^
  -H "Content-Type: application/json" ^
  -d "{\"method\":\"TARJETA\",\"transactionId\":\"tx-001\",\"amount\":{\"precio\":5000,\"moneda\":\"ARS\"},\"status\":\"APROBADO\"}"
```

Check-in (solo si está `CONFIRMADA`; pasa a `EFECTUADA`):

```
curl.exe -X PATCH http://localhost:8082/reservas/<id-reserva>/checkin
```

Review del huésped (obligatorio antes de poder hacer check-out; solo mientras está `EFECTUADA`):

```
curl.exe -X POST http://localhost:8082/reservas/<id-reserva>/review-cliente -H "Content-Type: application/json" -d "{\"rating\":4.5,\"comment\":\"Muy buena atencion\"}"
```

Review del hotel (se puede hacer en `EFECTUADA`, `FINALIZADA` o `ADEUDADA`):

```
curl.exe -X POST http://localhost:8082/reservas/<id-reserva>/review-hotel -H "Content-Type: application/json" -d "{\"rating\":5,\"comment\":\"Huesped impecable\"}"
```

Check-out (solo si está `EFECTUADA` y ya tiene review del huésped; sin review responde 400). Si el pago acumulado cubre el 100% del total pasa a `FINALIZADA`, si no pasa a `ADEUDADA`:

```
curl.exe -X PATCH http://localhost:8082/reservas/<id-reserva>/checkout
```

Si quedó `ADEUDADA`, se le pueden seguir registrando pagos (mismo endpoint de la sección de pagos); al completar el 100% pasa sola a `FINALIZADA`.

Listar los pagos de una reserva:

```
curl.exe http://localhost:8082/reservas/<id-reserva>/pagos
```

Cancelar (solo si está `RESERVADA`, `CONFIRMADA` o `BLOQUEADA`, **y no tiene pagos registrados**; si tiene pagos o el estado no lo permite, responde 400):

```
curl.exe -X PATCH http://localhost:8082/reservas/<id-reserva>/cancelar
```

### 3.3 Bloqueos administrativos

Bloquear una habitación sin asociarla a un huésped (por ejemplo, para mantenimiento). Ocupa el rango de fechas igual que una reserva real a los fines de la disponibilidad:

```
curl.exe -X POST "http://localhost:8082/reservas/bloqueos?idHabitacion=<id-de-mongo>&checkIn=2026-09-01&checkOut=2026-09-03"
```

Se levanta con el mismo endpoint de cancelar (`PATCH /reservas/{id}/cancelar`, `BLOQUEADA` está entre los estados cancelables).

---

## 4. Verificación de la mensajería asíncrona (gestion-svc → reservas-svc)

Para confirmar que los eventos viajan correctamente por RabbitMQ:

1. Crear un hotel y una habitación en `gestion-svc` (sección 2.2/2.3) y, dentro de 1-2 segundos, buscarla en `GET http://localhost:8082/habitaciones` filtrando por `habitacionId`. Debe aparecer con los datos completos: `capacidad`, `precioNoche` (el vigente, no 0), `tipoHabitacion`, y el `hotel` embebido con su `ubicacion` geoespacial.
2. Crear una nueva tarifa en `gestion-svc` para el mismo `tipoHabitacion` (sección 2.4) y volver a consultar la habitación en `reservas-svc`: el `precioNoche` debe haberse actualizado solo, sin volver a crear la habitación. Esto debería reflejarse en **todas** las habitaciones de ese tipo, en cualquier hotel.
3. Agregar un amenity al hotel (sección 2.2) y crear/actualizar una habitación de ese hotel: el campo `amenities` de la habitación en `reservas-svc` debe reflejar los amenities del hotel (se puede confirmar filtrando `GET /habitaciones/disponibles?...&amenities=<ese-amenity>`).
4. Cerrar el hotel (`PATCH /hoteles/{id}/cerrar`) y confirmar en `reservas-svc`: la habitación pasa a `disponible=false`, su `hotel.cerrado` pasa a `true`, y se crea automáticamente una reserva en estado `CERRADA` para esa habitación (`GET /reservas`, filtrando por `idHabitacion`). Si se repite el cierre, no debe duplicarse esa reserva `CERRADA`.
5. Se puede ver el tráfico de mensajes directamente en la consola de RabbitMQ (`http://localhost:15672`, exchange `dan.exchange`, cola `habitacion.topic`).

---

## 5. Checklist de reglas de negocio a validar

- [ ] No existe forma de borrar un propietario ni un hotel (405 en ambos `DELETE`).
- [ ] No se puede eliminar la tarjeta de crédito principal de un huésped (409) ni un banco referenciado por una tarjeta/cuenta (409).
- [ ] Borrar un huésped borra en cascada sus tarjetas.
- [ ] El CUIT de un hotel es único.
- [ ] `PUT /hoteles/{id}` solo permite cambiar categoría, teléfono y correo de contacto.
- [ ] Cerrar un hotel deja todas sus habitaciones no disponibles y no tiene vuelta atrás (no hay endpoint de reapertura).
- [ ] Una tarifa promocional con `fechaFin` recorta la continua anterior y genera sola la continuación al terminar.
- [ ] No se puede eliminar la única tarifa vigente de un tipo de habitación.
- [ ] La búsqueda de disponibilidad excluye habitaciones no disponibles, fuera de rango de fechas (por reservas o bloqueos solapados), y respeta todos los filtros combinados (precio, capacidad, categoría, amenities, distancia).
- [ ] Una reserva pasa de `RESERVADA` a `CONFIRMADA` automáticamente al alcanzar el 50% del pago total.
- [ ] El check-in solo es posible en estado `CONFIRMADA`; el check-out solo en `EFECTUADA` y requiere que el huésped haya dejado un review.
- [ ] El check-out deja la reserva en `FINALIZADA` (pago completo) o `ADEUDADA` (pago parcial), y una reserva `ADEUDADA` puede seguir recibiendo pagos hasta completarse y pasar sola a `FINALIZADA`.
- [ ] No se puede cancelar una reserva con pagos registrados, ni una que ya está en un estado terminal (`CANCELADA`, `FINALIZADA`, `CERRADA`).
- [ ] Cerrar un hotel genera una reserva `CERRADA` por cada una de sus habitaciones, sin duplicarse si se repite el cierre.
