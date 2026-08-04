package edu.utn.frsf.isi.dan.reservas_svc.controller;

import edu.utn.frsf.isi.dan.reservas_svc.messaging.EventStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Expone en vivo, vía Server-Sent Events, los mensajes que {@code reservas-svc} recibe de {@code
 * gestion-svc} a través de RabbitMQ. Pensado para que el frontend pueda visualizar la mensajería
 * asíncrona entre microservicios a medida que ocurre.
 */
@Tag(name = "Event Stream Controller", description = "Bus de eventos en vivo entre microservicios")
@RestController
@RequestMapping("/eventos")
public class EventStreamController {

  @Autowired private EventStreamService eventStreamService;

  /**
   * Abre una conexión SSE que emite cada evento de mensajería recibido desde RabbitMQ a medida que
   * ocurre.
   *
   * @return el emitter SSE suscripto al bus de eventos
   */
  @Operation(
      summary = "Suscribirse al bus de eventos",
      description =
          "Devuelve un stream SSE con cada evento que reservas-svc recibe de gestion-svc vía RabbitMQ.")
  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter stream() {
    return eventStreamService.subscribe();
  }
}
