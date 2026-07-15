package edu.utn.frsf.isi.dan.user.controller;

import edu.utn.frsf.isi.dan.user.dto.HuespedRecord;
import edu.utn.frsf.isi.dan.user.dto.HuespedUpdateDTO;
import edu.utn.frsf.isi.dan.user.dto.PropietarioRecord;
import edu.utn.frsf.isi.dan.user.dto.PropietarioUpdateDTO;
import edu.utn.frsf.isi.dan.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.utn.frsf.isi.dan.user.model.Usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "User Controller", description = "Operaciones para la gestión de usuarios")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Crear usuario huesped", 
                description = "Crea un nuevo usuario de tipo huesped",
                responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario huesped creado exitosamente"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Error en la solicitud"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor")}
    )
    @PostMapping("/huesped")
    public ResponseEntity<Void> crearUsuarioHuesped(@RequestBody HuespedRecord huespedRecord) {
        userService.crearUsuarioHuesped(huespedRecord);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Crear usuario propietario", description = "Crea un nuevo usuario de tipo propietario")
    @PostMapping("/propietario")
    public ResponseEntity<Void> crearUsuarioPropietario(@RequestBody @Valid PropietarioRecord propietarioRecord) {
        userService.crearUsuarioPropietario(propietarioRecord);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public Page<Usuario> buscarUsuariosPorNombre(@RequestParam(required = false) String nombre, Pageable pageable) {
        if (nombre == null || nombre.isEmpty()) {
            return userService.buscarPorNombre("", pageable);
        }
        return userService.buscarPorNombre(nombre, pageable);
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<Usuario> buscarUsuarioPorDni(@PathVariable String dni) {
        Usuario usuario = userService.buscarPorDniExacto(dni);
        if (usuario == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/buscar-dni")
    public Page<Usuario> buscarUsuariosPorDni(@RequestParam String dni, Pageable pageable) {
        return userService.buscarPorDni(dni, pageable);
    }

    @Operation(summary = "Actualizar huesped", description = "Actualiza datos de un huesped existente")
    @PatchMapping("/huesped/{id}")
    public ResponseEntity<Usuario> actualizarHuesped(@PathVariable Integer id, @RequestBody @Valid HuespedUpdateDTO updateDTO) {
        try {
            return ResponseEntity.ok(userService.actualizarHuesped(id, updateDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Eliminar huesped", description = "Elimina un huesped por su ID")
    @DeleteMapping("/huesped/{id}")
    public ResponseEntity<Void> eliminarHuesped(@PathVariable Integer id) {
        try {
            userService.eliminarHuesped(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Actualizar propietario", description = "Actualiza datos de un propietario existente")
    @PatchMapping("/propietario/{id}")
    public ResponseEntity<Usuario> actualizarPropietario(@PathVariable Integer id, @RequestBody @Valid PropietarioUpdateDTO updateDTO) {
        try {
            return ResponseEntity.ok(userService.actualizarPropietario(id, updateDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}