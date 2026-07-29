import { describe, expect, it } from "vitest";
import {
  isHuesped,
  isPropietario,
  type Huesped,
  type Propietario,
} from "./userSvc";

const huesped: Huesped = {
  id: 1,
  nombre: "Juan Perez",
  email: "juan@mail.com",
  telefono: "3411234567",
  dni: "30123456",
  fechaNacimiento: "1990-05-15",
  tarjetaCredito: [],
};

const propietario: Propietario = {
  id: 2,
  nombre: "Carlos Gomez",
  email: "carlos@mail.com",
  telefono: "3419998888",
  dni: "27123456",
  cuentaBancaria: null,
  idHotel: null,
};

describe("isHuesped / isPropietario", () => {
  it("distingue un huésped por la presencia de tarjetaCredito", () => {
    expect(isHuesped(huesped)).toBe(true);
    expect(isHuesped(propietario)).toBe(false);
  });

  it("distingue un propietario por la presencia de cuentaBancaria", () => {
    expect(isPropietario(propietario)).toBe(true);
    expect(isPropietario(huesped)).toBe(false);
  });

  it("cuentaBancaria en null todavía cuenta como propietario (el campo existe)", () => {
    // La API devuelve cuentaBancaria: null cuando el propietario no tiene
    // cuenta cargada, pero la clave sigue presente en el objeto.
    expect(isPropietario({ ...propietario, cuentaBancaria: null })).toBe(true);
  });
});
