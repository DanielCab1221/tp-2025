package edu.utn.frsf.isi.dan.gestion.controller;

import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import edu.utn.frsf.isi.dan.gestion.service.TarifaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de entidades {@link Tarifa}. Proporciona endpoints para crear,
 * obtener, listar y eliminar tarifas.
 *
 * <p>Las rutas expuestas por este controlador están bajo el prefijo <code>/tarifas</code>.
 */
@Tag(name = "Tarifa Controller", description = "CRUD de tarifas de habitaciones")
@RestController
@RequestMapping("/tarifas")
public class TarifaController {
  @Autowired private TarifaService tarifaService;

  /**
   * Crea una nueva tarifa.
   *
   * @param tarifa datos de la tarifa a crear
   * @return la tarifa creada
   */
  @Operation(summary = "Crear tarifa")
  @PostMapping
  public ResponseEntity<Tarifa> create(@RequestBody Tarifa tarifa) {
    return ResponseEntity.ok(tarifaService.crear(tarifa));
  }

  /**
   * Obtiene una tarifa a partir de su identificador.
   *
   * @param id identificador de la tarifa
   * @return la tarifa encontrada, o 404 Not Found si no existe
   */
  @Operation(summary = "Obtener tarifa por ID")
  @GetMapping("/{id}")
  public ResponseEntity<Tarifa> getById(@PathVariable Integer id) {
    return tarifaService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Lista todas las tarifas registradas.
   *
   * @return la lista completa de tarifas
   */
  @Operation(summary = "Listar tarifas")
  @GetMapping
  public List<Tarifa> getAll() {
    return tarifaService.findAll();
  }

  /**
   * Elimina una tarifa a partir de su identificador.
   *
   * @param id identificador de la tarifa a eliminar
   * @return 204 No Content una vez eliminada la tarifa
   */
  @Operation(summary = "Eliminar tarifa")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Integer id) {
    tarifaService.eliminar(id);
    return ResponseEntity.noContent().build();
  }
}
