package edu.utn.frsf.isi.dan.gestion.dao;

import edu.utn.frsf.isi.dan.gestion.model.Habitacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Integer> {
  List<Habitacion> findByHotelId(Integer hotelId);

  @Query(
      "SELECT h FROM Habitacion h WHERE "
          + "(:tipoHabitacionId IS NULL OR h.tipoHabitacion.id = :tipoHabitacionId) "
          + "AND (:capacidadMinima IS NULL OR h.tipoHabitacion.capacidad >= :capacidadMinima) "
          + "AND (:disponible IS NULL OR h.disponible = :disponible) "
          + "AND (:hotelId IS NULL OR h.hotel.id = :hotelId)")
  List<Habitacion> buscar(
      @Param("tipoHabitacionId") Integer tipoHabitacionId,
      @Param("capacidadMinima") Integer capacidadMinima,
      @Param("disponible") Boolean disponible,
      @Param("hotelId") Integer hotelId);
}
