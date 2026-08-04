package edu.utn.frsf.isi.dan.reservas_svc.messaging;

import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import java.time.Instant;

/**
 * Representa, para fines de visualización, un mensaje asíncrono que viajó entre microservicios a
 * través de RabbitMQ. Se arma en el momento en que {@code reservas-svc} recibe un {@link
 * HabitacionEvent} publicado por {@code gestion-svc}.
 */
public record EventoBus(
    String origen,
    String destino,
    String tipoEvento,
    String resumen,
    Object payload,
    Instant timestamp) {

  public static EventoBus deHabitacionEvent(HabitacionEvent evento) {
    return new EventoBus(
        "gestion-svc",
        "reservas-svc",
        evento.getTipoEvento().name(),
        resumir(evento),
        evento,
        Instant.now());
  }

  private static String resumir(HabitacionEvent evento) {
    var habitacion = evento.getHabitacion();
    var tarifa = evento.getTarifa();
    return switch (evento.getTipoEvento()) {
      case CREAR ->
          "Nueva habitación %s creada en %s"
              .formatted(
                  habitacion.getNumero(),
                  habitacion.getHotel() != null ? habitacion.getHotel().getNombre() : "?");
      case ACTUALIZAR_DATOS ->
          "Habitación %s actualizada en %s"
              .formatted(
                  habitacion.getNumero(),
                  habitacion.getHotel() != null ? habitacion.getHotel().getNombre() : "?");
      case ELIMINAR -> "Habitación #%d eliminada".formatted(habitacion.getHabitacionId());
      case ACTUALIZAR_PRECIO ->
          "Nuevo precio $%.2f para el tipo de habitación #%d"
              .formatted(tarifa.getNuevoPrecio(), tarifa.getTipoHabitacionId());
    };
  }
}
