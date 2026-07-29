export class ApiError extends Error {
  status: number;
  path?: string;

  constructor(status: number, message: string, path?: string) {
    super(message);
    this.status = status;
    this.path = path;
  }
}

function defaultMessageForStatus(status: number): string {
  switch (status) {
    case 400:
      return "Solicitud inválida";
    case 404:
      return "No encontrado";
    case 405:
      return "Método no permitido";
    case 409:
      return "Conflicto: la operación no está permitida en el estado actual";
    default:
      return `Error inesperado (HTTP ${status})`;
  }
}

// Los 3 backends devuelven, para errores "de negocio" atrapados en el propio
// controller, una respuesta sin body (solo el status importa). Solo algunos
// 400/500 que escapan al ControllerAdvisor traen este JSON.
interface ExceptionInfo {
  message: string;
  path?: string;
  status?: number;
  timestamp?: string;
}

export async function request<T>(
  url: string,
  options?: RequestInit,
): Promise<T> {
  const res = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options?.headers,
    },
  });

  if (!res.ok) {
    const text = await res.text();
    let message = defaultMessageForStatus(res.status);
    let path: string | undefined;
    if (text) {
      try {
        const body = JSON.parse(text) as ExceptionInfo;
        if (body.message) message = body.message;
        path = body.path;
      } catch {
        // body no-JSON (vacío o texto plano): nos quedamos con el mensaje genérico
      }
    }
    throw new ApiError(res.status, message, path);
  }

  if (res.status === 204) return undefined as T;
  const text = await res.text();
  if (!text) return undefined as T;
  return JSON.parse(text) as T;
}

export function buildQuery<T extends object>(params: T): string {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === null || value === "") continue;
    if (Array.isArray(value)) {
      value.forEach((v) => search.append(key, String(v)));
    } else {
      search.append(key, String(value));
    }
  }
  const qs = search.toString();
  return qs ? `?${qs}` : "";
}
