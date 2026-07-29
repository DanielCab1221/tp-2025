import { afterEach, describe, expect, it, vi } from "vitest";
import { ApiError, buildQuery, request } from "./http";

function jsonResponse(status: number, body: unknown, ok = status < 400) {
  return {
    ok,
    status,
    text: () => Promise.resolve(JSON.stringify(body)),
  } as Response;
}

function emptyResponse(status: number, ok = status < 400) {
  return {
    ok,
    status,
    text: () => Promise.resolve(""),
  } as Response;
}

describe("buildQuery", () => {
  it("devuelve string vacío sin parámetros", () => {
    expect(buildQuery({})).toBe("");
  });

  it("omite undefined, null y strings vacíos", () => {
    expect(buildQuery({ a: undefined, b: null, c: "", d: "ok" })).toBe("?d=ok");
  });

  it("serializa números y booleanos como string", () => {
    const qs = buildQuery({ page: 0, size: 10, disponible: true });
    const params = new URLSearchParams(qs.slice(1));
    expect(params.get("page")).toBe("0");
    expect(params.get("size")).toBe("10");
    expect(params.get("disponible")).toBe("true");
  });

  it("repite la clave para cada elemento de un array", () => {
    const qs = buildQuery({ amenities: ["WIFI", "PILETA"] });
    const params = new URLSearchParams(qs.slice(1));
    expect(params.getAll("amenities")).toEqual(["WIFI", "PILETA"]);
  });
});

describe("request", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("parsea el body JSON en una respuesta exitosa", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(jsonResponse(200, { id: 1, nombre: "ACME" })),
    );
    const result = await request<{ id: number; nombre: string }>(
      "http://x/bancos/1",
    );
    expect(result).toEqual({ id: 1, nombre: "ACME" });
  });

  it("devuelve undefined en un 204 sin body", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(emptyResponse(204)));
    const result = await request("http://x/tarjetas/1");
    expect(result).toBeUndefined();
  });

  it("devuelve undefined cuando el body viene vacío pero ok", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(emptyResponse(201)));
    const result = await request("http://x/users/huesped");
    expect(result).toBeUndefined();
  });

  it("usa message/path del ExceptionInfo cuando el error trae JSON", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        jsonResponse(400, {
          message: "El tipo de habitacion es requerido",
          path: "uri=/tarifas",
          status: 400,
          timestamp: "123",
        }),
      ),
    );
    await expect(request("http://x/tarifas")).rejects.toMatchObject({
      status: 400,
      message: "El tipo de habitacion es requerido",
      path: "uri=/tarifas",
    });
  });

  it("usa un mensaje genérico por status cuando el error viene sin body", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(emptyResponse(409)));
    await expect(request("http://x/bancos/1")).rejects.toMatchObject({
      status: 409,
      message: "Conflicto: la operación no está permitida en el estado actual",
    });
  });

  it("no revienta si el error trae un body no-JSON", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: false,
        status: 500,
        text: () => Promise.resolve("<html>boom</html>"),
      } as Response),
    );
    await expect(request("http://x/algo")).rejects.toBeInstanceOf(ApiError);
  });

  it("manda Content-Type application/json por default", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValue(jsonResponse(200, { ok: true }));
    vi.stubGlobal("fetch", fetchMock);
    await request("http://x/bancos", { method: "POST", body: "{}" });
    const [, init] = fetchMock.mock.calls[0];
    expect(init.headers).toMatchObject({ "Content-Type": "application/json" });
  });
});
