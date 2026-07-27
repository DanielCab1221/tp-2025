package edu.utn.frsf.isi.dan.reservas_svc.service;

import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Aislado de HabitacionService/ReservaService (que la usan ambos) para evitar
 * una dependencia circular entre esos dos beans.
 */
@Service
public class DisponibilidadService {
    @Autowired
    private MongoTemplate mongoTemplate;

    public boolean tieneReservaQueSolapa(String idHabitacion, Instant checkIn, Instant checkOut) {
        return !idsConReservaQueSolapa(List.of(idHabitacion), checkIn, checkOut).isEmpty();
    }

    /**
     * Dos rangos se solapan si el existente empieza antes de que termine el pedido, y
     * (el existente no tiene fin o termina despues de que empiece el pedido). checkOut = null
     * en la reserva existente representa un bloqueo permanente (ej. cierre de hotel).
     */
    public Set<String> idsConReservaQueSolapa(List<String> idsHabitacion, Instant checkIn, Instant checkOut) {
        Query query = new Query(new Criteria().andOperator(
                Criteria.where("idHabitacion").in(idsHabitacion),
                Criteria.where("estadoReserva").ne(EstadoReserva.CANCELADA),
                Criteria.where("checkIn").lt(checkOut),
                new Criteria().orOperator(
                        Criteria.where("checkOut").is(null),
                        Criteria.where("checkOut").gt(checkIn)
                )
        ));
        return mongoTemplate.find(query, Reserva.class).stream()
                .map(Reserva::getIdHabitacion)
                .collect(Collectors.toSet());
    }
}
