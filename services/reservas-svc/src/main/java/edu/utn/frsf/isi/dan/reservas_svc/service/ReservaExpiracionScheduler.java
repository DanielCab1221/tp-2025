package edu.utn.frsf.isi.dan.reservas_svc.service;

import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Libera periodicamente las habitaciones de reservas RESERVADA que nunca llegaron a pagar la
 * sena minima: sin esto, una reserva abandonada bloquea la habitacion para otros huespedes de
 * forma indefinida (ver DisponibilidadService, que considera ocupada cualquier reserva que no
 * este CANCELADA).
 */
@Component
@Log4j2
public class ReservaExpiracionScheduler {

  @Autowired private ReservaService reservaService;

  @Value("${reserva.expiracion.horas:24}")
  private long horasParaExpirar;

  @Scheduled(fixedDelayString = "${reserva.expiracion.chequeo-ms:3600000}")
  public void liberarReservasVencidas() {
    Instant limite = Instant.now().minus(horasParaExpirar, ChronoUnit.HOURS);
    List<Reserva> liberadas = reservaService.liberarReservasVencidas(limite);
    if (!liberadas.isEmpty()) {
      log.info(
          "Liberadas {} reserva(s) RESERVADA sin sena, creadas antes de {}",
          liberadas.size(),
          limite);
    }
  }
}
