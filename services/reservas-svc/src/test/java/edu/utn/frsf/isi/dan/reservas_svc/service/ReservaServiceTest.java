package edu.utn.frsf.isi.dan.reservas_svc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.utn.frsf.isi.dan.reservas_svc.exception.RecursoNoEncontradoException;
import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Habitacion;
import edu.utn.frsf.isi.dan.reservas_svc.model.Hotel;
import edu.utn.frsf.isi.dan.reservas_svc.model.Huesped;
import edu.utn.frsf.isi.dan.reservas_svc.model.Pago;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Review;
import edu.utn.frsf.isi.dan.reservas_svc.model.Tarifa;
import edu.utn.frsf.isi.dan.reservas_svc.repository.HabitacionRepository;
import edu.utn.frsf.isi.dan.reservas_svc.repository.ReservaRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ReservaServiceTest {

  @InjectMocks private ReservaService reservaService;

  @Mock private ReservaRepository reservaRepository;

  @Mock private HabitacionRepository habitacionRepository;

  @Mock private DisponibilidadService disponibilidadService;

  private static final String HABITACION_ID = "hab-1";
  private static final String RESERVA_ID = "res-1";

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    when(reservaRepository.save(any(Reserva.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  private Habitacion habitacionDisponible(Double precioNoche) {
    return Habitacion.builder()
        .id(HABITACION_ID)
        .precioNoche(precioNoche)
        .disponible(true)
        .hotel(Hotel.builder().id(5).build())
        .build();
  }

  private Reserva reservaConEstado(EstadoReserva estado) {
    return reservaConEstado(estado, null, null);
  }

  private Reserva reservaConEstado(EstadoReserva estado, Double precioTotal, List<Pago> pagos) {
    return Reserva.builder()
        ._id(RESERVA_ID)
        .idHabitacion(HABITACION_ID)
        .estadoReserva(estado)
        .precioTotal(precioTotal)
        .pago(pagos)
        .build();
  }

  private Pago pagoDe(double monto) {
    return Pago.builder().method("tarjeta").amount(Tarifa.builder().precio(monto).build()).build();
  }

  // ---------------------------------------------------------------------
  // crear()
  // ---------------------------------------------------------------------

  @Test
  public void testCrear_CheckInNull_LanzaIllegalArgumentException() {
    Reserva request =
        Reserva.builder()
            .idHabitacion(HABITACION_ID)
            .checkIn(null)
            .checkOut(Instant.parse("2026-03-06T12:00:00Z"))
            .build();

    assertThrows(IllegalArgumentException.class, () -> reservaService.crear(request));
  }

  @Test
  public void testCrear_CheckOutNull_LanzaIllegalArgumentException() {
    Reserva request =
        Reserva.builder()
            .idHabitacion(HABITACION_ID)
            .checkIn(Instant.parse("2026-03-01T12:00:00Z"))
            .checkOut(null)
            .build();

    assertThrows(IllegalArgumentException.class, () -> reservaService.crear(request));
  }

  @Test
  public void testCrear_CheckInIgualCheckOut_LanzaIllegalArgumentException() {
    Instant fecha = Instant.parse("2026-03-01T12:00:00Z");
    Reserva request =
        Reserva.builder().idHabitacion(HABITACION_ID).checkIn(fecha).checkOut(fecha).build();

    assertThrows(IllegalArgumentException.class, () -> reservaService.crear(request));
  }

  @Test
  public void testCrear_CheckInPosteriorACheckOut_LanzaIllegalArgumentException() {
    Reserva request =
        Reserva.builder()
            .idHabitacion(HABITACION_ID)
            .checkIn(Instant.parse("2026-03-10T12:00:00Z"))
            .checkOut(Instant.parse("2026-03-05T12:00:00Z"))
            .build();

    assertThrows(IllegalArgumentException.class, () -> reservaService.crear(request));
  }

  @Test
  public void testCrear_HabitacionNoEncontrada_LanzaRecursoNoEncontradoException() {
    Reserva request =
        Reserva.builder()
            .idHabitacion(HABITACION_ID)
            .checkIn(Instant.parse("2026-03-01T12:00:00Z"))
            .checkOut(Instant.parse("2026-03-06T12:00:00Z"))
            .build();
    when(habitacionRepository.findById(HABITACION_ID)).thenReturn(Optional.empty());

    assertThrows(RecursoNoEncontradoException.class, () -> reservaService.crear(request));
  }

  @Test
  public void testCrear_HabitacionNoDisponible_LanzaIllegalArgumentException() {
    Reserva request =
        Reserva.builder()
            .idHabitacion(HABITACION_ID)
            .checkIn(Instant.parse("2026-03-01T12:00:00Z"))
            .checkOut(Instant.parse("2026-03-06T12:00:00Z"))
            .build();
    Habitacion habitacion = habitacionDisponible(100.0);
    habitacion.setDisponible(false);
    when(habitacionRepository.findById(HABITACION_ID)).thenReturn(Optional.of(habitacion));

    assertThrows(IllegalArgumentException.class, () -> reservaService.crear(request));
  }

  @Test
  public void testCrear_SolapaConOtraReserva_LanzaIllegalArgumentException() {
    Instant checkIn = Instant.parse("2026-03-01T12:00:00Z");
    Instant checkOut = Instant.parse("2026-03-06T12:00:00Z");
    Reserva request =
        Reserva.builder().idHabitacion(HABITACION_ID).checkIn(checkIn).checkOut(checkOut).build();
    Habitacion habitacion = habitacionDisponible(100.0);
    when(habitacionRepository.findById(HABITACION_ID)).thenReturn(Optional.of(habitacion));
    when(disponibilidadService.tieneReservaQueSolapa(HABITACION_ID, checkIn, checkOut))
        .thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> reservaService.crear(request));
  }

  @Test
  public void testCrear_CasoFeliz_CalculaPrecioTotalYQuedaReservada() {
    Instant checkIn = Instant.parse("2026-03-01T12:00:00Z");
    Instant checkOut = Instant.parse("2026-03-06T12:00:00Z"); // 5 noches
    Huesped huesped = Huesped.builder().idUsuario("u1").email("a@a.com").build();
    Reserva request =
        Reserva.builder()
            .idHabitacion(HABITACION_ID)
            .checkIn(checkIn)
            .checkOut(checkOut)
            .huesped(huesped)
            .build();
    Habitacion habitacion = habitacionDisponible(150.5);
    when(habitacionRepository.findById(HABITACION_ID)).thenReturn(Optional.of(habitacion));
    when(disponibilidadService.tieneReservaQueSolapa(HABITACION_ID, checkIn, checkOut))
        .thenReturn(false);

    Reserva resultado = reservaService.crear(request);

    assertNotNull(resultado);
    assertEquals(752.5, resultado.getPrecioTotal());
    assertEquals(150.5, resultado.getPrecioNoche());
    assertEquals(EstadoReserva.RESERVADA, resultado.getEstadoReserva());
    assertEquals(HABITACION_ID, resultado.getIdHabitacion());
    assertEquals(5L, resultado.getHotelId());
    assertEquals(huesped, resultado.getHuesped());
    verify(reservaRepository).save(any(Reserva.class));
  }

  // ---------------------------------------------------------------------
  // registrarPago()
  // ---------------------------------------------------------------------

  @Test
  public void testRegistrarPago_PagoNulo_LanzaIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class, () -> reservaService.registrarPago(RESERVA_ID, null));
  }

  @Test
  public void testRegistrarPago_AmountNulo_LanzaIllegalArgumentException() {
    Pago pago = Pago.builder().method("tarjeta").amount(null).build();
    assertThrows(
        IllegalArgumentException.class, () -> reservaService.registrarPago(RESERVA_ID, pago));
  }

  @Test
  public void testRegistrarPago_PrecioNulo_LanzaIllegalArgumentException() {
    Pago pago = Pago.builder().amount(Tarifa.builder().precio(null).build()).build();
    assertThrows(
        IllegalArgumentException.class, () -> reservaService.registrarPago(RESERVA_ID, pago));
  }

  @Test
  public void testRegistrarPago_PrecioCero_LanzaIllegalArgumentException() {
    Pago pago = pagoDe(0.0);
    assertThrows(
        IllegalArgumentException.class, () -> reservaService.registrarPago(RESERVA_ID, pago));
  }

  @Test
  public void testRegistrarPago_PrecioNegativo_LanzaIllegalArgumentException() {
    Pago pago = pagoDe(-10.0);
    assertThrows(
        IllegalArgumentException.class, () -> reservaService.registrarPago(RESERVA_ID, pago));
  }

  @Test
  public void testRegistrarPago_ReservaCancelada_LanzaIllegalArgumentException() {
    Reserva reserva = reservaConEstado(EstadoReserva.CANCELADA, 1000.0, new ArrayList<>());
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));
    Pago pago = pagoDe(500.0);

    assertThrows(
        IllegalArgumentException.class, () -> reservaService.registrarPago(RESERVA_ID, pago));
  }

  @Test
  public void testRegistrarPago_ReservaFinalizada_LanzaIllegalArgumentException() {
    Reserva reserva = reservaConEstado(EstadoReserva.FINALIZADA, 1000.0, new ArrayList<>());
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));
    Pago pago = pagoDe(500.0);

    assertThrows(
        IllegalArgumentException.class, () -> reservaService.registrarPago(RESERVA_ID, pago));
  }

  @Test
  public void testRegistrarPago_ReservadaAlcanzaExactamente50Porciento_PasaAConfirmada() {
    Reserva reserva = reservaConEstado(EstadoReserva.RESERVADA, 1000.0, new ArrayList<>());
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));
    Pago pago = pagoDe(500.0);

    Reserva resultado = reservaService.registrarPago(RESERVA_ID, pago);

    assertEquals(EstadoReserva.CONFIRMADA, resultado.getEstadoReserva());
    assertEquals(1, resultado.getPago().size());
  }

  @Test
  public void testRegistrarPago_ReservadaNoAlcanza50Porciento_QuedaReservada() {
    Reserva reserva = reservaConEstado(EstadoReserva.RESERVADA, 1000.0, new ArrayList<>());
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));
    Pago pago = pagoDe(499.99);

    Reserva resultado = reservaService.registrarPago(RESERVA_ID, pago);

    assertEquals(EstadoReserva.RESERVADA, resultado.getEstadoReserva());
  }

  @Test
  public void testRegistrarPago_AdeudadaCompleta100Porciento_PasaAFinalizada() {
    List<Pago> pagosPrevios = new ArrayList<>(List.of(pagoDe(800.0)));
    Reserva reserva = reservaConEstado(EstadoReserva.ADEUDADA, 1000.0, pagosPrevios);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));
    Pago pago = pagoDe(200.0);

    Reserva resultado = reservaService.registrarPago(RESERVA_ID, pago);

    assertEquals(EstadoReserva.FINALIZADA, resultado.getEstadoReserva());
    assertEquals(2, resultado.getPago().size());
  }

  @Test
  public void testRegistrarPago_AdeudadaNoCompleta100Porciento_QuedaAdeudada() {
    List<Pago> pagosPrevios = new ArrayList<>(List.of(pagoDe(800.0)));
    Reserva reserva = reservaConEstado(EstadoReserva.ADEUDADA, 1000.0, pagosPrevios);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));
    Pago pago = pagoDe(100.0);

    Reserva resultado = reservaService.registrarPago(RESERVA_ID, pago);

    assertEquals(EstadoReserva.ADEUDADA, resultado.getEstadoReserva());
  }

  @Test
  public void testRegistrarPago_ReservaInexistente_LanzaRecursoNoEncontradoException() {
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.empty());
    Pago pago = pagoDe(100.0);

    assertThrows(
        RecursoNoEncontradoException.class, () -> reservaService.registrarPago(RESERVA_ID, pago));
  }

  // ---------------------------------------------------------------------
  // cancelar()
  // ---------------------------------------------------------------------

  @Test
  public void testCancelar_Reservada_SinPagos_Cancela() {
    Reserva reserva = reservaConEstado(EstadoReserva.RESERVADA);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));

    Reserva resultado = reservaService.cancelar(RESERVA_ID);

    assertEquals(EstadoReserva.CANCELADA, resultado.getEstadoReserva());
  }

  @Test
  public void testCancelar_Confirmada_SinPagos_Cancela() {
    Reserva reserva = reservaConEstado(EstadoReserva.CONFIRMADA);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));

    Reserva resultado = reservaService.cancelar(RESERVA_ID);

    assertEquals(EstadoReserva.CANCELADA, resultado.getEstadoReserva());
  }

  @Test
  public void testCancelar_Bloqueada_SinPagos_Cancela() {
    Reserva reserva = reservaConEstado(EstadoReserva.BLOQUEADA);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));

    Reserva resultado = reservaService.cancelar(RESERVA_ID);

    assertEquals(EstadoReserva.CANCELADA, resultado.getEstadoReserva());
  }

  @Test
  public void testCancelar_ConPagosRegistrados_LanzaIllegalArgumentException() {
    Reserva reserva =
        reservaConEstado(EstadoReserva.RESERVADA, 1000.0, new ArrayList<>(List.of(pagoDe(100.0))));
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));

    assertThrows(IllegalArgumentException.class, () -> reservaService.cancelar(RESERVA_ID));
  }

  @Test
  public void testCancelar_EstadoNoCancelable_LanzaIllegalArgumentException() {
    Reserva reserva = reservaConEstado(EstadoReserva.EFECTUADA);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));

    assertThrows(IllegalArgumentException.class, () -> reservaService.cancelar(RESERVA_ID));
  }

  @Test
  public void testCancelar_ReservaInexistente_LanzaRecursoNoEncontradoException() {
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.empty());

    assertThrows(RecursoNoEncontradoException.class, () -> reservaService.cancelar(RESERVA_ID));
  }

  // ---------------------------------------------------------------------
  // registrarCheckIn()
  // ---------------------------------------------------------------------

  @Test
  public void testRegistrarCheckIn_Confirmada_PasaAEfectuada() {
    Reserva reserva = reservaConEstado(EstadoReserva.CONFIRMADA);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));

    Reserva resultado = reservaService.registrarCheckIn(RESERVA_ID);

    assertEquals(EstadoReserva.EFECTUADA, resultado.getEstadoReserva());
  }

  @Test
  public void testRegistrarCheckIn_EstadoDistinto_LanzaIllegalArgumentException() {
    Reserva reserva = reservaConEstado(EstadoReserva.RESERVADA);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));

    assertThrows(IllegalArgumentException.class, () -> reservaService.registrarCheckIn(RESERVA_ID));
  }

  @Test
  public void testRegistrarCheckIn_ReservaInexistente_LanzaRecursoNoEncontradoException() {
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.empty());

    assertThrows(
        RecursoNoEncontradoException.class, () -> reservaService.registrarCheckIn(RESERVA_ID));
  }

  // ---------------------------------------------------------------------
  // registrarCheckOut()
  // ---------------------------------------------------------------------

  @Test
  public void testRegistrarCheckOut_EstadoDistintoDeEfectuada_LanzaIllegalArgumentException() {
    Reserva reserva = reservaConEstado(EstadoReserva.CONFIRMADA);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));

    assertThrows(
        IllegalArgumentException.class, () -> reservaService.registrarCheckOut(RESERVA_ID));
  }

  @Test
  public void testRegistrarCheckOut_SinClientReview_LanzaIllegalArgumentException() {
    Reserva reserva = reservaConEstado(EstadoReserva.EFECTUADA, 1000.0, new ArrayList<>());
    reserva.setClientReview(null);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));

    assertThrows(
        IllegalArgumentException.class, () -> reservaService.registrarCheckOut(RESERVA_ID));
  }

  @Test
  public void testRegistrarCheckOut_ConReviewYPagoCompleto_PasaAFinalizada() {
    List<Pago> pagos = new ArrayList<>(List.of(pagoDe(1000.0)));
    Reserva reserva = reservaConEstado(EstadoReserva.EFECTUADA, 1000.0, pagos);
    reserva.setClientReview(Review.builder().rating(5).comment("Muy bueno").build());
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));

    Reserva resultado = reservaService.registrarCheckOut(RESERVA_ID);

    assertEquals(EstadoReserva.FINALIZADA, resultado.getEstadoReserva());
  }

  @Test
  public void testRegistrarCheckOut_ConReviewYPagoIncompleto_PasaAAdeudada() {
    List<Pago> pagos = new ArrayList<>(List.of(pagoDe(500.0)));
    Reserva reserva = reservaConEstado(EstadoReserva.EFECTUADA, 1000.0, pagos);
    reserva.setClientReview(Review.builder().rating(4).comment("Bueno").build());
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));

    Reserva resultado = reservaService.registrarCheckOut(RESERVA_ID);

    assertEquals(EstadoReserva.ADEUDADA, resultado.getEstadoReserva());
  }

  @Test
  public void testRegistrarCheckOut_ReservaInexistente_LanzaRecursoNoEncontradoException() {
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.empty());

    assertThrows(
        RecursoNoEncontradoException.class, () -> reservaService.registrarCheckOut(RESERVA_ID));
  }

  // ---------------------------------------------------------------------
  // registrarReviewCliente()
  // ---------------------------------------------------------------------

  @Test
  public void testRegistrarReviewCliente_Efectuada_RegistraReview() {
    Reserva reserva = reservaConEstado(EstadoReserva.EFECTUADA);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));
    Review review = Review.builder().rating(5).comment("Excelente").build();

    Reserva resultado = reservaService.registrarReviewCliente(RESERVA_ID, review);

    assertEquals(review, resultado.getClientReview());
    assertNotNull(resultado.getClientReview().getCreatedAt());
  }

  @Test
  public void testRegistrarReviewCliente_EstadoDistinto_LanzaIllegalArgumentException() {
    Reserva reserva = reservaConEstado(EstadoReserva.RESERVADA);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));
    Review review = Review.builder().rating(5).comment("Excelente").build();

    assertThrows(
        IllegalArgumentException.class,
        () -> reservaService.registrarReviewCliente(RESERVA_ID, review));
  }

  @Test
  public void testRegistrarReviewCliente_ReservaInexistente_LanzaRecursoNoEncontradoException() {
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.empty());
    Review review = Review.builder().rating(5).comment("Excelente").build();

    assertThrows(
        RecursoNoEncontradoException.class,
        () -> reservaService.registrarReviewCliente(RESERVA_ID, review));
  }

  // ---------------------------------------------------------------------
  // registrarReviewHotel()
  // ---------------------------------------------------------------------

  @Test
  public void testRegistrarReviewHotel_Efectuada_RegistraReview() {
    Reserva reserva = reservaConEstado(EstadoReserva.EFECTUADA);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));
    Review review = Review.builder().rating(4).comment("Buen huesped").build();

    Reserva resultado = reservaService.registrarReviewHotel(RESERVA_ID, review);

    assertEquals(review, resultado.getHostReview());
    assertNotNull(resultado.getHostReview().getCreatedAt());
  }

  @Test
  public void testRegistrarReviewHotel_Finalizada_RegistraReview() {
    Reserva reserva = reservaConEstado(EstadoReserva.FINALIZADA);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));
    Review review = Review.builder().rating(3).comment("Ok").build();

    Reserva resultado = reservaService.registrarReviewHotel(RESERVA_ID, review);

    assertEquals(review, resultado.getHostReview());
  }

  @Test
  public void testRegistrarReviewHotel_Adeudada_RegistraReview() {
    Reserva reserva = reservaConEstado(EstadoReserva.ADEUDADA);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));
    Review review = Review.builder().rating(2).comment("Regular").build();

    Reserva resultado = reservaService.registrarReviewHotel(RESERVA_ID, review);

    assertEquals(review, resultado.getHostReview());
  }

  @Test
  public void testRegistrarReviewHotel_EstadoNoPermitido_LanzaIllegalArgumentException() {
    Reserva reserva = reservaConEstado(EstadoReserva.RESERVADA);
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.of(reserva));
    Review review = Review.builder().rating(1).comment("Malo").build();

    assertThrows(
        IllegalArgumentException.class,
        () -> reservaService.registrarReviewHotel(RESERVA_ID, review));
  }

  @Test
  public void testRegistrarReviewHotel_ReservaInexistente_LanzaRecursoNoEncontradoException() {
    when(reservaRepository.findById(RESERVA_ID)).thenReturn(Optional.empty());
    Review review = Review.builder().rating(1).comment("Malo").build();

    assertThrows(
        RecursoNoEncontradoException.class,
        () -> reservaService.registrarReviewHotel(RESERVA_ID, review));
  }
}
