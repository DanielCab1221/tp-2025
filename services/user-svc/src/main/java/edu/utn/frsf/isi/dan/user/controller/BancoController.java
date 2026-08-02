package edu.utn.frsf.isi.dan.user.controller;

import edu.utn.frsf.isi.dan.user.dto.BancoDTO;
import edu.utn.frsf.isi.dan.user.model.Banco;
import edu.utn.frsf.isi.dan.user.service.BancoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controlador para la gestión de bancos */
@Tag(name = "Banco Controller", description = "CRUD de bancos")
@RestController
@RequestMapping("/bancos")
public class BancoController {

  @Autowired private BancoService bancoService;

  /**
   * Crea un nuevo banco a partir de los datos recibidos en el DTO.
   *
   * @param dto datos del banco a crear
   * @return el banco creado con estado 201 Created
   */
  @Operation(summary = "Crear banco")
  @PostMapping
  public ResponseEntity<Banco> crearBanco(@RequestBody @Valid BancoDTO dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(bancoService.crearBanco(dto));
  }

  /**
   * Lista todos los bancos registrados en el sistema.
   *
   * @return la lista completa de bancos
   */
  @Operation(summary = "Listar todos los bancos")
  @GetMapping
  public ResponseEntity<List<Banco>> listarBancos() {
    return ResponseEntity.ok(bancoService.listarBancos());
  }

  /**
   * Obtiene un banco a partir de su identificador.
   *
   * @param id identificador del banco
   * @return el banco encontrado, o 404 Not Found si no existe
   */
  @Operation(summary = "Obtener banco por ID")
  @GetMapping("/{id}")
  public ResponseEntity<Banco> obtenerBanco(@PathVariable Integer id) {
    Optional<Banco> banco = bancoService.obtenerBancoPorId(id);
    return banco.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  /**
   * Actualiza los datos de un banco existente.
   *
   * @param id identificador del banco a actualizar
   * @param dto nuevos datos del banco
   * @return el banco actualizado, o 404 Not Found si el banco no existe
   */
  @Operation(summary = "Actualizar banco")
  @PutMapping("/{id}")
  public ResponseEntity<Banco> actualizarBanco(
      @PathVariable Integer id, @RequestBody @Valid BancoDTO dto) {
    try {
      return ResponseEntity.ok(bancoService.actualizarBanco(id, dto));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Elimina un banco a partir de su identificador.
   *
   * @param id identificador del banco a eliminar
   * @return 204 No Content si se eliminó correctamente, 404 Not Found si no existe o 409 Conflict
   *     si el banco tiene relaciones que impiden su eliminación
   */
  @Operation(summary = "Eliminar banco")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> eliminarBanco(@PathVariable Integer id) {
    try {
      bancoService.eliminarBanco(id);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }
}
