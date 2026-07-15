package edu.utn.frsf.isi.dan.user.controller;

import edu.utn.frsf.isi.dan.user.dto.BancoDTO;
import edu.utn.frsf.isi.dan.user.model.Banco;
import edu.utn.frsf.isi.dan.user.service.BancoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Banco Controller", description = "CRUD de bancos")
@RestController
@RequestMapping("/bancos")
public class BancoController {

    @Autowired
    private BancoService bancoService;

    @Operation(summary = "Crear banco")
    @PostMapping
    public ResponseEntity<Banco> crearBanco(@RequestBody @Valid BancoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bancoService.crearBanco(dto));
    }

    @Operation(summary = "Listar todos los bancos")
    @GetMapping
    public ResponseEntity<List<Banco>> listarBancos() {
        return ResponseEntity.ok(bancoService.listarBancos());
    }

    @Operation(summary = "Obtener banco por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Banco> obtenerBanco(@PathVariable Integer id) {
        Optional<Banco> banco = bancoService.obtenerBancoPorId(id);
        return banco.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar banco")
    @PutMapping("/{id}")
    public ResponseEntity<Banco> actualizarBanco(@PathVariable Integer id, @RequestBody @Valid BancoDTO dto) {
        try {
            return ResponseEntity.ok(bancoService.actualizarBanco(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Eliminar banco")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarBanco(@PathVariable Integer id) {
        try {
            bancoService.eliminarBanco(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
