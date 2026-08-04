package edu.utn.frsf.isi.dan.reservas_svc.service;

import edu.utn.frsf.isi.dan.reservas_svc.exception.RecursoNoEncontradoException;
import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Habitacion;
import edu.utn.frsf.isi.dan.reservas_svc.model.Pago;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Review;
import edu.utn.frsf.isi.dan.reservas_svc.repository.HabitacionRepository;
import edu.utn.frsf.isi.dan.reservas_svc.repository.ReservaRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReservaService {
  private static final Set<EstadoReserva> CANCELABLES =
      Set.of(EstadoReserva.RESERVADA, EstadoReserva.CONFIRMADA, EstadoReserva.BLOQUEADA);
  private static final Set<EstadoReserva> ACEPTAN_PAGO =
      Set.of(
          EstadoReserva.RESERVADA,
          EstadoReserva.CONFIRMADA,
          EstadoReserva.EFECTUADA,
          EstadoReserva.ADEUDADA);
  private static final Set<EstadoReserva> ACEPTAN_REVIEW_HOTEL =
      Set.of(EstadoReserva.EFECTUADA, EstadoReserva.FINALIZADA, EstadoReserva.ADEUDADA);

  @Autowired private ReservaRepository reservaRepository;

  @Autowired private HabitacionRepository habitacionRepository;

  @Autowired private DisponibilidadService disponibilidadService;

  public List<Reserva> findAll() {
    return reservaRepository.findAll();
  }

  public Optional<Reserva> findById(String id) {
    return reservaRepository.findById(id);
  }

  public List<Reserva> findByIdUsuario(String idUsuario) {
    return reservaRepository.findByHuespedIdUsuario(idUsuario);
  }

  public Reserva save(Reserva reserva) {
    return reservaRepository.save(reserva);
  }

  public void deleteById(String id) {
    reservaRepository.deleteById(id);
  }

  public Reserva crear(Reserva reservaRequest) {
    if (reservaRequest.getCheckIn() == null
        || reservaRequest.getCheckOut() == null
        || !reservaRequest.getCheckIn().isBefore(reservaRequest.getCheckOut())) {
      throw new IllegalArgumentException(
          "El rango checkIn/checkOut es invalido: checkIn debe ser anterior a checkOut");
    }
    Habitacion habitacion =
        habitacionRepository
            .findById(reservaRequest.getIdHabitacion())
            .orElseThrow(
                () ->
                    new RecursoNoEncontradoException(
                        "No se encontro la habitacion con id: "
                            + reservaRequest.getIdHabitacion()));
    if (!Boolean.TRUE.equals(habitacion.getDisponible())) {
      throw new IllegalArgumentException("La habitacion no esta disponible");
    }
    if (disponibilidadService.tieneReservaQueSolapa(
        habitacion.getId(), reservaRequest.getCheckIn(), reservaRequest.getCheckOut())) {
      throw new IllegalArgumentException(
          "La habitacion ya tiene una reserva para ese rango de fechas");
    }

    long noches =
        Duration.between(reservaRequest.getCheckIn(), reservaRequest.getCheckOut()).toDays();
    Double precioNoche = habitacion.getPrecioNoche();

    Reserva reserva =
        Reserva.builder()
            .idHabitacion(habitacion.getId())
            .hotelId(
                habitacion.getHotel() != null ? habitacion.getHotel().getId().longValue() : null)
            .createdAt(Instant.now())
            .checkIn(reservaRequest.getCheckIn())
            .checkOut(reservaRequest.getCheckOut())
            .precioNoche(precioNoche)
            .precioTotal(precioNoche != null ? precioNoche * noches : null)
            .huesped(reservaRequest.getHuesped())
            .estadoReserva(EstadoReserva.RESERVADA)
            .build();
    return reservaRepository.save(reserva);
  }

  public Reserva bloquear(String idHabitacion, Instant checkIn, Instant checkOut) {
    if (checkIn == null || checkOut == null || !checkIn.isBefore(checkOut)) {
      throw new IllegalArgumentException(
          "El rango checkIn/checkOut es invalido: checkIn debe ser anterior a checkOut");
    }
    Habitacion habitacion =
        habitacionRepository
            .findById(idHabitacion)
            .orElseThrow(
                () ->
                    new RecursoNoEncontradoException(
                        "No se encontro la habitacion con id: " + idHabitacion));
    if (disponibilidadService.tieneReservaQueSolapa(habitacion.getId(), checkIn, checkOut)) {
      throw new IllegalArgumentException(
          "La habitacion ya tiene una reserva para ese rango de fechas");
    }

    Reserva bloqueo =
        Reserva.builder()
            .idHabitacion(habitacion.getId())
            .hotelId(
                habitacion.getHotel() != null ? habitacion.getHotel().getId().longValue() : null)
            .createdAt(Instant.now())
            .checkIn(checkIn)
            .checkOut(checkOut)
            .estadoReserva(EstadoReserva.BLOQUEADA)
            .build();
    return reservaRepository.save(bloqueo);
  }

  /**
   * Cancela las reservas RESERVADA (sin seña) creadas antes de {@code limite}, liberando la
   * habitacion para otros huespedes. No toca reservas con algun pago registrado: por debajo del
   * 50% no llegaron a CONFIRMADA, pero cancelarlas automaticamente implicaria un reembolso que
   * esta fuera del alcance de esta limpieza.
   */
  public List<Reserva> liberarReservasVencidas(Instant limite) {
    List<Reserva> vencidas =
        reservaRepository.findByEstadoReservaAndCreatedAtBefore(EstadoReserva.RESERVADA, limite);
    List<Reserva> liberadas = new ArrayList<>();
    for (Reserva reserva : vencidas) {
      if (reserva.getPago() == null || reserva.getPago().isEmpty()) {
        reserva.setEstadoReserva(EstadoReserva.CANCELADA);
        liberadas.add(reservaRepository.save(reserva));
      }
    }
    return liberadas;
  }

  public Reserva cancelar(String id) {
    Reserva reserva = obtenerOFallar(id);
    if (!CANCELABLES.contains(reserva.getEstadoReserva())) {
      throw new IllegalArgumentException(
          "No se puede cancelar una reserva en estado " + reserva.getEstadoReserva());
    }
    if (reserva.getPago() != null && !reserva.getPago().isEmpty()) {
      throw new IllegalArgumentException("No se puede cancelar una reserva con pagos registrados");
    }
    reserva.setEstadoReserva(EstadoReserva.CANCELADA);
    return reservaRepository.save(reserva);
  }

  public Reserva registrarCheckIn(String id) {
    Reserva reserva = obtenerOFallar(id);
    if (reserva.getEstadoReserva() != EstadoReserva.CONFIRMADA) {
      throw new IllegalArgumentException(
          "Solo se puede hacer check-in de una reserva CONFIRMADA (estado actual: "
              + reserva.getEstadoReserva()
              + ")");
    }
    reserva.setEstadoReserva(EstadoReserva.EFECTUADA);
    return reservaRepository.save(reserva);
  }

  public Reserva registrarCheckOut(String id) {
    Reserva reserva = obtenerOFallar(id);
    if (reserva.getEstadoReserva() != EstadoReserva.EFECTUADA) {
      throw new IllegalArgumentException(
          "Solo se puede hacer check-out de una reserva EFECTUADA (estado actual: "
              + reserva.getEstadoReserva()
              + ")");
    }
    reserva.setEstadoReserva(
        finalizacionCompleta(reserva) ? EstadoReserva.FINALIZADA : EstadoReserva.ADEUDADA);
    return reservaRepository.save(reserva);
  }

  public Reserva registrarPago(String id, Pago pago) {
    if (pago == null
        || pago.getAmount() == null
        || pago.getAmount().getPrecio() == null
        || pago.getAmount().getPrecio() <= 0) {
      throw new IllegalArgumentException(
          "El pago debe tener un monto (amount.precio) mayor a cero");
    }
    Reserva reserva = obtenerOFallar(id);
    if (!ACEPTAN_PAGO.contains(reserva.getEstadoReserva())) {
      throw new IllegalArgumentException(
          "No se pueden registrar pagos para una reserva en estado " + reserva.getEstadoReserva());
    }

    if (reserva.getPago() == null) {
      reserva.setPago(new ArrayList<>());
    }
    reserva.getPago().add(pago);

    if (reserva.getEstadoReserva() == EstadoReserva.RESERVADA
        && alcanzaMinimoParaConfirmar(reserva)) {
      reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
    } else if (reserva.getEstadoReserva() == EstadoReserva.ADEUDADA
        && finalizacionCompleta(reserva)) {
      reserva.setEstadoReserva(EstadoReserva.FINALIZADA);
    }
    return reservaRepository.save(reserva);
  }

  public List<Pago> obtenerPagos(String id) {
    Reserva reserva = obtenerOFallar(id);
    return reserva.getPago() != null ? reserva.getPago() : List.of();
  }

  private boolean alcanzaMinimoParaConfirmar(Reserva reserva) {
    return reserva.getPrecioTotal() != null
        && totalPagado(reserva) >= reserva.getPrecioTotal() * 0.5;
  }

  private boolean pagoCompleto(Reserva reserva) {
    return reserva.getPrecioTotal() != null && totalPagado(reserva) >= reserva.getPrecioTotal();
  }

  private boolean finalizacionCompleta(Reserva reserva) {
    return pagoCompleto(reserva) && reserva.getHostReview() != null;
  }

  public Reserva registrarReviewCliente(String id, Review review) {
    Reserva reserva = obtenerOFallar(id);
    if (reserva.getEstadoReserva() != EstadoReserva.EFECTUADA) {
      throw new IllegalArgumentException(
          "El huesped solo puede dejar un review con la reserva en estado EFECTUADA (estado actual: "
              + reserva.getEstadoReserva()
              + ")");
    }
    review.setCreatedAt(Instant.now().toString());
    reserva.setClientReview(review);
    return reservaRepository.save(reserva);
  }

  public Reserva registrarReviewHotel(String id, Review review) {
    Reserva reserva = obtenerOFallar(id);
    if (!ACEPTAN_REVIEW_HOTEL.contains(reserva.getEstadoReserva())) {
      throw new IllegalArgumentException(
          "El hotel solo puede dejar un review de una reserva efectuada (estado actual: "
              + reserva.getEstadoReserva()
              + ")");
    }
    review.setCreatedAt(Instant.now().toString());
    reserva.setHostReview(review);
    if (reserva.getEstadoReserva() == EstadoReserva.ADEUDADA && finalizacionCompleta(reserva)) {
      reserva.setEstadoReserva(EstadoReserva.FINALIZADA);
    }
    return reservaRepository.save(reserva);
  }

  private double totalPagado(Reserva reserva) {
    if (reserva.getPago() == null) {
      return 0;
    }
    return reserva.getPago().stream()
        .filter(pago -> pago.getAmount() != null && pago.getAmount().getPrecio() != null)
        .mapToDouble(pago -> pago.getAmount().getPrecio())
        .sum();
  }

  private Reserva obtenerOFallar(String id) {
    return reservaRepository
        .findById(id)
        .orElseThrow(
            () -> new RecursoNoEncontradoException("No se encontro la reserva con id: " + id));
  }
}
