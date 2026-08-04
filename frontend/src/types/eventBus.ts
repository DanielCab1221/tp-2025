export type TipoEventoBus =
  "CREAR" | "ACTUALIZAR_DATOS" | "ACTUALIZAR_PRECIO" | "ELIMINAR";

export interface EventoBus {
  origen: string;
  destino: string;
  tipoEvento: TipoEventoBus;
  resumen: string;
  payload: unknown;
  timestamp: string;
}
