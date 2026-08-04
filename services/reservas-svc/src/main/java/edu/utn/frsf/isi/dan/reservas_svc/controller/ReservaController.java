package edu.utn.frsf.isi.dan.reservas_svc.controller;

import edu.utn.frsf.isi.dan.reservas_svc.model.Pago;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Review;
import edu.utn.frsf.isi.dan.reservas_svc.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión del ciclo de vida de las reservas: creación, bloqueos
 * temporales, cancelación, check-in/check-out, pagos y reviews.
 *
 * <p>Las rutas expuestas por este controlador están bajo el prefijo <code>/reservas</code>.
 */
@Tag(name = "Reserva Controller", description = "Ciclo de vida de las reservas")
@RestController
@RequestMapping("/reservas")
public class ReservaController {
  @Autowired private ReservaService reservaService;

  /**
   * Lista todas las reservas, o las reservas de un usuario específico si se indica.
   *
   * @param idUsuario identificador del usuario a filtrar (opcional)
   * @return la lista de reservas encontradas
   */
  @Operation(summary = "Listar reservas")
  @GetMapping
  public List<Reserva> getAll(@RequestParam(required = false) String idUsuario) {
    if (idUsuario == null) {
      return reservaService.findAll();
    }
    return reservaService.findByIdUsuario(idUsuario);
  }

  /**
   * Obtiene una reserva a partir de su identificador.
   *
   * @param id identificador de la reserva
   * @return la reserva encontrada, o 404 Not Found si no existe
   */
  @Operation(summary = "Obtener reserva por ID")
  @GetMapping("/{id}")
  public ResponseEntity<Reserva> getById(@PathVariable String id) {
    return reservaService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Crea una nueva reserva.
   *
   * @param reserva datos de la reserva a crear
   * @return la reserva creada
   */
  @Operation(summary = "Crear reserva")
  @PostMapping
  public Reserva create(@Valid @RequestBody Reserva reserva) {
    return reservaService.crear(reserva);
  }

  /**
   * Crea un bloqueo temporal sobre una habitación para un rango de fechas, impidiendo que sea
   * reservada por otro huésped mientras dura el bloqueo.
   *
   * @param idHabitacion identificador de la habitación a bloquear
   * @param checkIn fecha de inicio del bloqueo
   * @param checkOut fecha de fin del bloqueo
   * @return la reserva de bloqueo creada
   */
  @Operation(
      summary = "Bloquear habitación",
      description =
          "Crea un bloqueo temporal sobre una habitación para un rango de fechas, impidiendo que sea reservada mientras dura el bloqueo.")
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

  /**
   * Cancela una reserva existente.
   *
   * @param id identificador de la reserva a cancelar
   * @return la reserva actualizada con su nuevo estado
   */
  @Operation(summary = "Cancelar reserva")
  @PatchMapping("/{id}/cancelar")
  public Reserva cancelar(@PathVariable String id) {
    return reservaService.cancelar(id);
  }

  /**
   * Registra el check-in de una reserva.
   *
   * @param id identificador de la reserva
   * @return la reserva actualizada con su nuevo estado
   */
  @Operation(summary = "Registrar check-in")
  @PatchMapping("/{id}/checkin")
  public Reserva checkIn(@PathVariable String id) {
    return reservaService.registrarCheckIn(id);
  }

  /**
   * Registra el check-out de una reserva.
   *
   * @param id identificador de la reserva
   * @return la reserva actualizada con su nuevo estado
   */
  @Operation(summary = "Registrar check-out")
  @PatchMapping("/{id}/checkout")
  public Reserva checkOut(@PathVariable String id) {
    return reservaService.registrarCheckOut(id);
  }

  /**
   * Registra un pago asociado a una reserva.
   *
   * @param id identificador de la reserva
   * @param pago datos del pago a registrar
   * @return la reserva actualizada con el pago registrado
   */
  @Operation(summary = "Registrar pago")
  @PostMapping("/{id}/pagos")
  public Reserva registrarPago(@PathVariable String id, @Valid @RequestBody Pago pago) {
    return reservaService.registrarPago(id, pago);
  }

  /**
   * Obtiene los pagos registrados de una reserva.
   *
   * @param id identificador de la reserva
   * @return la lista de pagos de la reserva
   */
  @Operation(summary = "Listar pagos de una reserva")
  @GetMapping("/{id}/pagos")
  public List<Pago> getPagos(@PathVariable String id) {
    return reservaService.obtenerPagos(id);
  }

  /**
   * Registra la review que el cliente hace del hotel para una reserva.
   *
   * @param id identificador de la reserva
   * @param review datos de la review a registrar
   * @return la reserva actualizada con la review registrada
   */
  @Operation(summary = "Registrar review del cliente al hotel")
  @PostMapping("/{id}/review-cliente")
  public Reserva registrarReviewCliente(@PathVariable String id, @RequestBody Review review) {
    return reservaService.registrarReviewCliente(id, review);
  }

  /**
   * Registra la review que el hotel hace del cliente para una reserva.
   *
   * @param id identificador de la reserva
   * @param review datos de la review a registrar
   * @return la reserva actualizada con la review registrada
   */
  @Operation(summary = "Registrar review del hotel al cliente")
  @PostMapping("/{id}/review-hotel")
  public Reserva registrarReviewHotel(@PathVariable String id, @RequestBody Review review) {
    return reservaService.registrarReviewHotel(id, review);
  }
}
