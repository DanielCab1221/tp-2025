package edu.utn.frsf.isi.dan.user.controller;

import edu.utn.frsf.isi.dan.user.dto.HuespedRecord;
import edu.utn.frsf.isi.dan.user.dto.HuespedUpdateDTO;
import edu.utn.frsf.isi.dan.user.dto.PropietarioRecord;
import edu.utn.frsf.isi.dan.user.dto.PropietarioUpdateDTO;
import edu.utn.frsf.isi.dan.user.exception.ExceptionInfo;
import edu.utn.frsf.isi.dan.user.model.Usuario;
import edu.utn.frsf.isi.dan.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controlador para la gestión de usuarios (huéspedes y propietarios). */
@Tag(name = "User Controller", description = "Operaciones para la gestión de usuarios")
@RestController
@RequestMapping("/users")
public class UserController {

  @Autowired private UserService userService;

  /**
   * Crea un nuevo usuario de tipo huésped.
   *
   * @param huespedRecord datos del huésped a crear
   * @return 201 Created una vez creado el usuario
   */
  @Operation(
      summary = "Crear usuario huesped",
      description = "Crea un nuevo usuario de tipo huesped",
      responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Usuario huesped creado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Error en la solicitud"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor")
      })
  @PostMapping("/huesped")
  public ResponseEntity<Void> crearUsuarioHuesped(@RequestBody HuespedRecord huespedRecord) {
    userService.crearUsuarioHuesped(huespedRecord);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  /**
   * Crea un nuevo usuario de tipo propietario.
   *
   * @param propietarioRecord datos del propietario a crear
   * @return 201 Created una vez creado el usuario
   */
  @Operation(
      summary = "Crear usuario propietario",
      description = "Crea un nuevo usuario de tipo propietario")
  @PostMapping("/propietario")
  public ResponseEntity<Void> crearUsuarioPropietario(
      @RequestBody @Valid PropietarioRecord propietarioRecord) {
    userService.crearUsuarioPropietario(propietarioRecord);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  /**
   * Busca usuarios cuyo nombre contenga el texto indicado, de forma paginada. Si no se especifica
   * un nombre, devuelve todos los usuarios paginados.
   *
   * @param nombre texto a buscar en el nombre del usuario (opcional)
   * @param pageable información de paginación
   * @return página de usuarios que coinciden con la búsqueda
   */
  @GetMapping
  public Page<Usuario> buscarUsuariosPorNombre(
      @RequestParam(required = false) String nombre, Pageable pageable) {
    if (nombre == null || nombre.isEmpty()) {
      return userService.buscarPorNombre("", pageable);
    }
    return userService.buscarPorNombre(nombre, pageable);
  }

  /**
   * Busca un usuario por coincidencia exacta de DNI.
   *
   * @param dni DNI exacto del usuario a buscar
   * @return el usuario encontrado, o 404 Not Found si no existe
   */
  @GetMapping("/dni/{dni}")
  public ResponseEntity<Usuario> buscarUsuarioPorDni(@PathVariable String dni) {
    Usuario usuario = userService.buscarPorDniExacto(dni);
    if (usuario == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(usuario);
  }

  /**
   * Busca usuarios cuyo DNI contenga el texto indicado, de forma paginada.
   *
   * @param dni texto a buscar dentro del DNI del usuario
   * @param pageable información de paginación
   * @return página de usuarios que coinciden con la búsqueda
   */
  @GetMapping("/buscar-dni")
  public Page<Usuario> buscarUsuariosPorDni(@RequestParam String dni, Pageable pageable) {
    return userService.buscarPorDni(dni, pageable);
  }

  /**
   * Actualiza los datos de un huésped existente.
   *
   * @param id identificador del huésped
   * @param updateDTO nuevos datos del huésped
   * @return el usuario actualizado, o 404 Not Found si no existe
   */
  @Operation(
      summary = "Actualizar huesped",
      description = "Actualiza datos de un huesped existente")
  @PatchMapping("/huesped/{id}")
  public ResponseEntity<?> actualizarHuesped(
      @PathVariable Integer id, @RequestBody @Valid HuespedUpdateDTO updateDTO) {
    try {
      return ResponseEntity.ok(userService.actualizarHuesped(id, updateDTO));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ExceptionInfo(
                  e.getMessage(),
                  null,
                  String.valueOf(System.currentTimeMillis()),
                  HttpStatus.NOT_FOUND.value()));
    }
  }

  /**
   * Elimina un huésped a partir de su identificador.
   *
   * @param id identificador del huésped a eliminar
   * @return 204 No Content si se eliminó correctamente, o 404 Not Found si no existe
   */
  @Operation(summary = "Eliminar huesped", description = "Elimina un huesped por su ID")
  @DeleteMapping("/huesped/{id}")
  public ResponseEntity<?> eliminarHuesped(@PathVariable Integer id) {
    try {
      userService.eliminarHuesped(id);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ExceptionInfo(
                  e.getMessage(),
                  null,
                  String.valueOf(System.currentTimeMillis()),
                  HttpStatus.NOT_FOUND.value()));
    }
  }

  /**
   * Actualiza los datos de un propietario existente.
   *
   * @param id identificador del propietario
   * @param updateDTO nuevos datos del propietario
   * @return el usuario actualizado, o 404 Not Found si no existe
   */
  @Operation(
      summary = "Actualizar propietario",
      description = "Actualiza datos de un propietario existente")
  @PatchMapping("/propietario/{id}")
  public ResponseEntity<?> actualizarPropietario(
      @PathVariable Integer id, @RequestBody @Valid PropietarioUpdateDTO updateDTO) {
    try {
      return ResponseEntity.ok(userService.actualizarPropietario(id, updateDTO));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ExceptionInfo(
                  e.getMessage(),
                  null,
                  String.valueOf(System.currentTimeMillis()),
                  HttpStatus.NOT_FOUND.value()));
    }
  }
}
