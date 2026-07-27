package edu.utn.frsf.isi.dan.reservas_svc.controller;

import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import edu.utn.frsf.isi.dan.reservas_svc.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/reservas")
public class ReservaController {
    @Autowired
    private ReservaService reservaService;

    @GetMapping
    public List<Reserva> getAll(@RequestParam(required = false) String idUsuario) {
        if (idUsuario == null) {
            return reservaService.findAll();
        }
        return reservaService.findByIdUsuario(idUsuario);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reserva> getById(@PathVariable String id) {
        return reservaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Reserva create(@RequestBody Reserva reserva) {
        return reservaService.crear(reserva);
    }

    @PostMapping("/bloqueos")
    public Reserva bloquear(
            @RequestParam String idHabitacion,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return reservaService.bloquear(
                idHabitacion,
                checkIn.atStartOfDay(ZoneOffset.UTC).toInstant(),
                checkOut.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    @PatchMapping("/{id}/cancelar")
    public Reserva cancelar(@PathVariable String id) {
        return reservaService.cancelar(id);
    }

    @PatchMapping("/{id}/checkin")
    public Reserva checkIn(@PathVariable String id) {
        return reservaService.registrarCheckIn(id);
    }

    @PatchMapping("/{id}/checkout")
    public Reserva checkOut(@PathVariable String id) {
        return reservaService.registrarCheckOut(id);
    }
}
