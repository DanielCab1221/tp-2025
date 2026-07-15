package edu.utn.frsf.isi.dan.user.controller;

import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTO;
import edu.utn.frsf.isi.dan.user.model.TarjetaCredito;
import edu.utn.frsf.isi.dan.user.service.TarjetaCreditoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tarjeta Credito Controller", description = "Gestión de tarjetas de crédito")
@RestController
@RequestMapping("/tarjetas")
public class TarjetaCreditoController {

    @Autowired
    private TarjetaCreditoService tarjetaCreditoService;

    @Operation(summary = "Agregar tarjeta a un huésped", description = "Agrega una tarjeta de crédito a un huésped. Si es principal, desmarca la anterior.")
    @PostMapping("/huesped/{huespedId}")
    public ResponseEntity<TarjetaCredito> agregarTarjeta(
            @PathVariable Integer huespedId,
            @RequestBody @Valid TarjetaCreditoDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(tarjetaCreditoService.agregarTarjeta(huespedId, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Listar tarjetas de un huésped")
    @GetMapping("/huesped/{huespedId}")
    public ResponseEntity<List<TarjetaCredito>> listarTarjetas(@PathVariable Integer huespedId) {
        return ResponseEntity.ok(tarjetaCreditoService.listarTarjetasDeHuesped(huespedId));
    }

    @Operation(summary = "Eliminar tarjeta", description = "Elimina una tarjeta si NO es la principal")
    @DeleteMapping("/{tarjetaId}")
    public ResponseEntity<Void> eliminarTarjeta(@PathVariable Integer tarjetaId) {
        try {
            tarjetaCreditoService.eliminarTarjeta(tarjetaId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Operation(summary = "Cambiar tarjeta principal", description = "Marca una tarjeta como principal y desmarca la anterior")
    @PatchMapping("/huesped/{huespedId}/principal/{tarjetaId}")
    public ResponseEntity<TarjetaCredito> cambiarTarjetaPrincipal(
            @PathVariable Integer huespedId,
            @PathVariable Integer tarjetaId) {
        try {
            return ResponseEntity.ok(tarjetaCreditoService.cambiarTarjetaPrincipal(huespedId, tarjetaId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
