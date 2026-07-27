package edu.utn.frsf.isi.dan.reservas_svc.controller;

import edu.utn.frsf.isi.dan.reservas_svc.model.Pago;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Review;
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

    @PostMapping("/{id}/pagos")
    public Reserva registrarPago(@PathVariable String id, @RequestBody Pago pago) {
        return reservaService.registrarPago(id, pago);
    }

    @GetMapping("/{id}/pagos")
    public List<Pago> getPagos(@PathVariable String id) {
        return reservaService.obtenerPagos(id);
    }

    @PostMapping("/{id}/review-cliente")
    public Reserva registrarReviewCliente(@PathVariable String id, @RequestBody Review review) {
        return reservaService.registrarReviewCliente(id, review);
    }

    @PostMapping("/{id}/review-hotel")
    public Reserva registrarReviewHotel(@PathVariable String id, @RequestBody Review review) {
        return reservaService.registrarReviewHotel(id, review);
    }
}
