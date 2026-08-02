/**
 * Controlador REST para la gestión de entidades {@link Habitacion}. Proporciona endpoints para
 * crear, obtener, actualizar y eliminar habitaciones.
 *
 * <p>Las rutas expuestas por este controlador están bajo el prefijo <code>/habitaciones</code>.
 *
 * <ul>
 *   <li><b>POST /habitaciones</b>: Crea una nueva habitación.
 *   <li><b>GET /habitaciones/{id}</b>: Obtiene una habitación por su identificador.
 *   <li><b>GET /habitaciones</b>: Obtiene la lista de todas las habitaciones.
 *   <li><b>PUT /habitaciones/{id}</b>: Actualiza una habitación existente.
 *   <li><b>DELETE /habitaciones/{id}</b>: Elimina una habitación por su identificador.
 * </ul>
 *
 * @author martindominguez
 */
package edu.utn.frsf.isi.dan.gestion.controller;

import edu.utn.frsf.isi.dan.gestion.model.Habitacion;
import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import edu.utn.frsf.isi.dan.gestion.service.HabitacionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/habitaciones")
public class HabitacionController {
  @Autowired private HabitacionService habitacionService;

  /**
   * Crea una nueva habitación.
   *
   * @param habitacion datos de la habitación a crear
   * @return la habitación creada
   */
  @PostMapping
  public ResponseEntity<Habitacion> create(@Valid @RequestBody Habitacion habitacion) {
    return ResponseEntity.ok(habitacionService.save(habitacion));
  }

  /**
   * Obtiene una habitación a partir de su identificador.
   *
   * @param id identificador de la habitación
   * @return la habitación encontrada, o 404 Not Found si no existe
   */
  @GetMapping("/{id}")
  public ResponseEntity<Habitacion> getById(@PathVariable Integer id) {
    return habitacionService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Obtiene todas las habitaciones, o filtra según los criterios de búsqueda indicados (tipo,
   * capacidad, disponibilidad, hotel y rango de precio).
   *
   * @param tipoHabitacionId identificador del tipo de habitación (opcional)
   * @param capacidadMinima capacidad mínima requerida (opcional)
   * @param disponible si la habitación debe estar disponible (opcional)
   * @param hotelId identificador del hotel al que pertenece (opcional)
   * @param precioMin precio mínimo (opcional)
   * @param precioMax precio máximo (opcional)
   * @return la lista de habitaciones que coinciden con los filtros, o todas si no se indica ninguno
   */
  @GetMapping
  public List<Habitacion> getAll(
      @RequestParam(required = false) Integer tipoHabitacionId,
      @RequestParam(required = false) Integer capacidadMinima,
      @RequestParam(required = false) Boolean disponible,
      @RequestParam(required = false) Integer hotelId,
      @RequestParam(required = false) Double precioMin,
      @RequestParam(required = false) Double precioMax) {
    if (tipoHabitacionId == null
        && capacidadMinima == null
        && disponible == null
        && hotelId == null
        && precioMin == null
        && precioMax == null) {
      return habitacionService.findAll();
    }
    return habitacionService.buscar(
        tipoHabitacionId, capacidadMinima, disponible, hotelId, precioMin, precioMax);
  }

  /**
   * Obtiene la tarifa vigente de una habitación.
   *
   * @param id identificador de la habitación
   * @return la tarifa vigente, o 404 Not Found si la habitación no existe o no tiene una tarifa
   *     vigente
   */
  @GetMapping("/{id}/tarifa-vigente")
  public ResponseEntity<Tarifa> getTarifaVigente(@PathVariable Integer id) {
    return habitacionService
        .findById(id)
        .flatMap(habitacion -> habitacionService.obtenerTarifaVigente(habitacion))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Actualiza una habitación existente.
   *
   * @param id identificador de la habitación a actualizar
   * @param habitacion nuevos datos de la habitación
   * @return la habitación actualizada, o 404 Not Found si no existe
   */
  @PutMapping("/{id}")
  public ResponseEntity<Habitacion> update(
      @PathVariable Integer id, @Valid @RequestBody Habitacion habitacion) {
    if (!habitacionService.findById(id).isPresent()) return ResponseEntity.notFound().build();
    habitacion.setId(id);
    return ResponseEntity.ok(habitacionService.save(habitacion));
  }

  /**
   * Elimina una habitación a partir de su identificador.
   *
   * @param id identificador de la habitación a eliminar
   * @return 204 No Content si se eliminó correctamente, o 404 Not Found si no existe
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Integer id) {
    if (!habitacionService.findById(id).isPresent()) return ResponseEntity.notFound().build();
    habitacionService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
