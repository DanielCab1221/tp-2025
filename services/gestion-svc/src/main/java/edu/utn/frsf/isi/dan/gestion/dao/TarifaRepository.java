package edu.utn.frsf.isi.dan.gestion.dao;

import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Integer> {

  List<Tarifa> findByTipoHabitacionIdOrderByFechaInicioDesc(Integer tipoHabitacionId);

  /**
   * Vigente = ya empezo (fechaInicio <= fecha) y no termino (fechaFin es null -> continua, o
   * fechaFin >= fecha). Una tarifa continua tiene fechaFin null, por eso no se puede resolver esto
   * con nombres de metodo derivados de Spring Data (no manejan bien el OR con NULL).
   */
  @Query(
      "SELECT t FROM Tarifa t WHERE t.tipoHabitacion.id = :tipoHabitacionId "
          + "AND t.fechaInicio <= :fecha AND (t.fechaFin IS NULL OR t.fechaFin >= :fecha) "
          + "ORDER BY t.fechaInicio DESC")
  List<Tarifa> findVigentes(
      @Param("tipoHabitacionId") Integer tipoHabitacionId, @Param("fecha") LocalDate fecha);
}
