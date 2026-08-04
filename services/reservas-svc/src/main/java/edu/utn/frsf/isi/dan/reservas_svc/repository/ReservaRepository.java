package edu.utn.frsf.isi.dan.reservas_svc.repository;

import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import java.time.Instant;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReservaRepository extends MongoRepository<Reserva, String> {
  List<Reserva> findByHuespedIdUsuario(String idUsuario);

  List<Reserva> findByEstadoReservaAndCreatedAtBefore(EstadoReserva estadoReserva, Instant limite);
}
