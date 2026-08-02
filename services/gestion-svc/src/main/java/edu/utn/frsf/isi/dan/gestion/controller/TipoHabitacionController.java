package edu.utn.frsf.isi.dan.gestion.controller;

import edu.utn.frsf.isi.dan.gestion.model.TipoHabitacion;
import edu.utn.frsf.isi.dan.gestion.service.TipoHabitacionService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de entidades {@link TipoHabitacion}. Proporciona endpoints para
 * crear, obtener, listar, actualizar y eliminar tipos de habitación.
 *
 * <p>Las rutas expuestas por este controlador están bajo el prefijo <code>/tipos-habitacion</code>.
 */
@RestController
@RequestMapping("/tipos-habitacion")
public class TipoHabitacionController {
  @Autowired private TipoHabitacionService tipoHabitacionService;

  /**
   * Crea un nuevo tipo de habitación.
   *
   * @param tipoHabitacion datos del tipo de habitación a crear
   * @return el tipo de habitación creado
   */
  @PostMapping
  public ResponseEntity<TipoHabitacion> create(@RequestBody TipoHabitacion tipoHabitacion) {
    return ResponseEntity.ok(tipoHabitacionService.save(tipoHabitacion));
  }

  /**
   * Obtiene un tipo de habitación a partir de su identificador.
   *
   * @param id identificador del tipo de habitación
   * @return el tipo de habitación encontrado, o 404 Not Found si no existe
   */
  @GetMapping("/{id}")
  public ResponseEntity<TipoHabitacion> getById(@PathVariable Integer id) {
    return tipoHabitacionService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Lista todos los tipos de habitación registrados.
   *
   * @return la lista completa de tipos de habitación
   */
  @GetMapping
  public List<TipoHabitacion> getAll() {
    return tipoHabitacionService.findAll();
  }

  /**
   * Actualiza un tipo de habitación existente.
   *
   * @param id identificador del tipo de habitación a actualizar
   * @param tipoHabitacion nuevos datos del tipo de habitación
   * @return el tipo de habitación actualizado, o 404 Not Found si no existe
   */
  @PutMapping("/{id}")
  public ResponseEntity<TipoHabitacion> update(
      @PathVariable Integer id, @RequestBody TipoHabitacion tipoHabitacion) {
    if (!tipoHabitacionService.findById(id).isPresent()) return ResponseEntity.notFound().build();
    tipoHabitacion.setId(id);
    return ResponseEntity.ok(tipoHabitacionService.save(tipoHabitacion));
  }

  /**
   * Elimina un tipo de habitación a partir de su identificador.
   *
   * @param id identificador del tipo de habitación a eliminar
   * @return 204 No Content si se eliminó correctamente, o 404 Not Found si no existe
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Integer id) {
    if (!tipoHabitacionService.findById(id).isPresent()) return ResponseEntity.notFound().build();
    tipoHabitacionService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
