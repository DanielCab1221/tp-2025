package edu.utn.frsf.isi.dan.gestion.controller;

import edu.utn.frsf.isi.dan.gestion.model.Amenity;
import edu.utn.frsf.isi.dan.gestion.model.Hotel;
import edu.utn.frsf.isi.dan.gestion.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de entidades {@link Hotel}. Proporciona endpoints para crear,
 * buscar, actualizar, cerrar hoteles y administrar sus amenities.
 *
 * <p>Las rutas expuestas por este controlador están bajo el prefijo <code>/hoteles</code>.
 */
@Tag(name = "Hotel Controller", description = "CRUD, búsqueda y administración de hoteles")
@RestController
@RequestMapping("/hoteles")
public class HotelController {
  @Autowired private HotelService hotelService;

  /**
   * Crea un nuevo hotel.
   *
   * @param hotel datos del hotel a crear
   * @return el hotel creado
   */
  @Operation(summary = "Crear hotel")
  @PostMapping
  public ResponseEntity<Hotel> create(@Valid @RequestBody Hotel hotel) {
    return ResponseEntity.ok(hotelService.save(hotel));
  }

  /**
   * Obtiene un hotel a partir de su identificador.
   *
   * @param id identificador del hotel
   * @return el hotel encontrado, o 404 Not Found si no existe
   */
  @Operation(summary = "Obtener hotel por ID")
  @GetMapping("/{id}")
  public ResponseEntity<Hotel> getById(@PathVariable Integer id) {
    return hotelService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Obtiene todos los hoteles, o filtra según los criterios de búsqueda indicados (nombre,
   * categoría, domicilio, estado de cierre y amenity).
   *
   * @param nombre texto a buscar en el nombre del hotel (opcional)
   * @param categoriaMinima categoría mínima requerida (opcional)
   * @param domicilio texto a buscar en el domicilio del hotel (opcional)
   * @param cerrado si el hotel debe estar cerrado (opcional)
   * @param amenity amenity que debe poseer el hotel (opcional)
   * @return la lista de hoteles que coinciden con los filtros, o todos si no se indica ninguno
   */
  @Operation(
      summary = "Listar/buscar hoteles",
      description =
          "Lista todos los hoteles o filtra por nombre, categoría, domicilio, estado de cierre y amenity.")
  @GetMapping
  public List<Hotel> getAll(
      @RequestParam(required = false) String nombre,
      @RequestParam(required = false) Integer categoriaMinima,
      @RequestParam(required = false) String domicilio,
      @RequestParam(required = false) Boolean cerrado,
      @RequestParam(required = false) Amenity amenity) {
    if (nombre == null
        && categoriaMinima == null
        && domicilio == null
        && cerrado == null
        && amenity == null) {
      return hotelService.findAll();
    }
    return hotelService.buscar(nombre, categoriaMinima, domicilio, cerrado, amenity);
  }

  /**
   * Actualiza los datos permitidos de un hotel existente.
   *
   * @param id identificador del hotel a actualizar
   * @param hotel nuevos datos del hotel
   * @return el hotel actualizado, o 404 Not Found si no existe
   */
  @Operation(summary = "Actualizar hotel")
  @PutMapping("/{id}")
  public ResponseEntity<Hotel> update(@PathVariable Integer id, @RequestBody Hotel hotel) {
    return hotelService
        .actualizarDatosPermitidos(id, hotel)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Marca un hotel como cerrado.
   *
   * @param id identificador del hotel a cerrar
   * @return el hotel actualizado, o 404 Not Found si no existe
   */
  @Operation(summary = "Cerrar hotel")
  @PatchMapping("/{id}/cerrar")
  public ResponseEntity<Hotel> cerrar(@PathVariable Integer id) {
    return hotelService
        .cerrar(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Agrega una o más amenities a un hotel.
   *
   * @param id identificador del hotel
   * @param amenities lista de amenities a agregar
   * @return el hotel actualizado con las amenities agregadas
   */
  @Operation(summary = "Agregar amenities a un hotel")
  @PutMapping("/{id}/amenities")
  public ResponseEntity<Hotel> agregarAmenities(
      @PathVariable Integer id, @RequestBody List<Amenity> amenities) {
    return ResponseEntity.ok(hotelService.agregarAmenities(id, amenities));
  }

  /**
   * Quita una amenity de un hotel.
   *
   * @param id identificador del hotel
   * @param amenity amenity a quitar
   * @return 204 No Content una vez quitada la amenity
   */
  @Operation(summary = "Quitar amenity de un hotel")
  @DeleteMapping("/{id}/amenities/{amenity}")
  public ResponseEntity<Void> quitarAmenity(
      @PathVariable Integer id, @PathVariable Amenity amenity) {
    hotelService.quitarAmenity(id, amenity);
    return ResponseEntity.noContent().build();
  }
}
