package edu.utn.frsf.isi.dan.reservas_svc.messaging;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Mantiene las conexiones SSE abiertas con el frontend y retransmite por ahí los eventos que {@code
 * reservas-svc} recibe desde RabbitMQ, para poder visualizar en vivo la mensajería asíncrona entre
 * microservicios.
 */
@Service
@Log4j2
public class EventStreamService {

  private static final long SIN_TIMEOUT = 0L;

  private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  public SseEmitter subscribe() {
    SseEmitter emitter = new SseEmitter(SIN_TIMEOUT);
    emitters.add(emitter);
    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    emitter.onError((ex) -> emitters.remove(emitter));
    return emitter;
  }

  public void broadcast(EventoBus evento) {
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event().name("evento").data(evento));
      } catch (IOException | IllegalStateException e) {
        log.debug("Removiendo emitter SSE muerto: {}", e.getMessage());
        emitters.remove(emitter);
      }
    }
  }
}
