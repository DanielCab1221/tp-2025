package edu.utn.frsf.isi.dan.reservas_svc.controller;

import edu.utn.frsf.isi.dan.reservas_svc.model.Habitacion;
import edu.utn.frsf.isi.dan.reservas_svc.service.HabitacionService;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la consulta de habitaciones dentro del servicio de reservas. Expone
 * endpoints de sólo lectura, ya que las habitaciones son administradas por el servicio de gestión y
 * replicadas hacia este servicio.
 *
 * <p>Las rutas expuestas por este controlador están bajo el prefijo <code>/habitaciones</code>.
 */
@RestController
@RequestMapping("/habitaciones")
public class HabitacionController {
  @Autowired private HabitacionService habitacionService;

  /**
   * Lista todas las habitaciones disponibles en el servicio de reservas.
   *
   * @return la lista completa de habitaciones
   */
  @GetMapping
  public List<Habitacion> getAll() {
    return habitacionService.findAll();
  }

  /**
   * Obtiene una habitación a partir de su identificador.
   *
   * @param id identificador de la habitación
   * @return la habitación encontrada, o 404 Not Found si no existe
   */
  @GetMapping("/{id}")
  public ResponseEntity<Habitacion> getById(@PathVariable String id) {
    return habitacionService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Busca habitaciones disponibles para un rango de fechas, filtrando opcionalmente por cantidad de
   * huéspedes, rango de precio, categoría mínima, amenities requeridas y proximidad a una ubicación
   * geográfica.
   *
   * @param checkIn fecha de entrada
   * @param checkOut fecha de salida
   * @param huespedes cantidad de huéspedes a alojar (opcional)
   * @param precioMin precio mínimo (opcional)
   * @param precioMax precio máximo (opcional)
   * @param categoriaMinima categoría mínima del hotel (opcional)
   * @param amenities amenities que debe poseer el hotel (opcional)
   * @param latitud latitud del punto de referencia para el filtro de distancia (opcional)
   * @param longitud longitud del punto de referencia para el filtro de distancia (opcional)
   * @param distanciaMaximaKm distancia máxima en kilómetros respecto al punto de referencia
   *     (opcional)
   * @return la lista de habitaciones disponibles que cumplen los criterios indicados
   */
  @GetMapping("/disponibles")
  public List<Habitacion> buscarDisponibles(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
      @RequestParam(required = false) Integer huespedes,
      @RequestParam(required = false) Double precioMin,
      @RequestParam(required = false) Double precioMax,
      @RequestParam(required = false) Integer categoriaMinima,
      @RequestParam(required = false) List<String> amenities,
      @RequestParam(required = false) Double latitud,
      @RequestParam(required = false) Double longitud,
      @RequestParam(required = false) Double distanciaMaximaKm) {
    return habitacionService.buscarDisponibles(
        checkIn.atStartOfDay(ZoneOffset.UTC).toInstant(),
        checkOut.atStartOfDay(ZoneOffset.UTC).toInstant(),
        huespedes,
        precioMin,
        precioMax,
        categoriaMinima,
        amenities,
        latitud,
        longitud,
        distanciaMaximaKm);
  }
}
