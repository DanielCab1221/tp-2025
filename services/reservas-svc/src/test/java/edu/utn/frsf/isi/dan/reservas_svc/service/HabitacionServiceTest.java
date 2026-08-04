package edu.utn.frsf.isi.dan.reservas_svc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Habitacion;
import edu.utn.frsf.isi.dan.reservas_svc.model.Hotel;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import edu.utn.frsf.isi.dan.reservas_svc.repository.HabitacionRepository;
import edu.utn.frsf.isi.dan.shared.HabitacionDTO;
import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import edu.utn.frsf.isi.dan.shared.HotelDTO;
import edu.utn.frsf.isi.dan.shared.TarifaDTO;
import edu.utn.frsf.isi.dan.shared.TipoEvento;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class HabitacionServiceTest {

  @InjectMocks private HabitacionService habitacionService;

  @Mock private HabitacionRepository habitacionRepository;

  @Mock private ReservaService reservaService;

  @Mock private DisponibilidadService disponibilidadService;

  @Mock private MongoTemplate mongoTemplate;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testHandleEventCrearGuardaHabitacionMapeadaDesdeElDto() {
    // Arrange
    HotelDTO hotelDto = HotelDTO.builder().id(5).nombre("Hotel Test").cerrado(false).build();
    HabitacionDTO dto =
        HabitacionDTO.builder()
            .habitacionId(100L)
            .precioNoche(1500.0)
            .capacidad(2)
            .disponible(true)
            .tipoHabitacionId(1)
            .tipoHabitacion("Doble")
            .amenities(List.of("wifi"))
            .hotel(hotelDto)
            .build();
    HabitacionEvent event =
        HabitacionEvent.builder().tipoEvento(TipoEvento.CREAR).habitacion(dto).build();

    // Act
    habitacionService.handleEvent(event);

    // Assert
    ArgumentCaptor<Habitacion> captor = ArgumentCaptor.forClass(Habitacion.class);
    verify(habitacionRepository, times(1)).save(captor.capture());
    Habitacion guardada = captor.getValue();
    assertEquals(100L, guardada.getHabitacionId());
    assertEquals(1500.0, guardada.getPrecioNoche());
    assertEquals(2, guardada.getCapacidad());
    assertEquals(true, guardada.getDisponible());
    assertEquals(1, guardada.getIdTipoHabitacion());
    assertEquals("Doble", guardada.getTipoHabitacion());
    assertEquals(List.of("wifi"), guardada.getAmenities());
    assertEquals(5, guardada.getHotel().getId());
  }

  @Test
  public void testHandleEventEliminarRemueveHabitacionPorHabitacionId() {
    // Arrange
    HabitacionDTO dto = HabitacionDTO.builder().habitacionId(100L).build();
    HabitacionEvent event =
        HabitacionEvent.builder().tipoEvento(TipoEvento.ELIMINAR).habitacion(dto).build();

    // Act
    habitacionService.handleEvent(event);

    // Assert
    verify(mongoTemplate, times(1)).remove(any(Query.class), eq(Habitacion.class));
  }

  @Test
  public void testHandleEventActualizarPrecioActualizaPrecioDeTodasLasHabitacionesDelTipo() {
    // Arrange
    TarifaDTO tarifa = TarifaDTO.builder().tipoHabitacionId(1).nuevoPrecio(2000.0).build();
    HabitacionEvent event =
        HabitacionEvent.builder().tipoEvento(TipoEvento.ACTUALIZAR_PRECIO).tarifa(tarifa).build();

    // Act
    habitacionService.handleEvent(event);

    // Assert
    verify(mongoTemplate, times(1))
        .updateMulti(any(Query.class), any(Update.class), eq(Habitacion.class));
  }

  @Test
  public void testActualizarDatosCreaReservaCerradaCuandoElHotelSeCierraPorPrimeraVez() {
    // Arrange: la habitacion guardada tenia el hotel abierto (cerrado = false)
    Habitacion existente =
        Habitacion.builder()
            .id("mongoId1")
            .habitacionId(100L)
            .hotel(Hotel.builder().id(5).cerrado(false).build())
            .build();
    Habitacion actualizada =
        Habitacion.builder()
            .id("mongoId1")
            .habitacionId(100L)
            .hotel(Hotel.builder().id(5).cerrado(true).build())
            .build();
    when(mongoTemplate.findOne(any(Query.class), eq(Habitacion.class))).thenReturn(existente);
    when(mongoTemplate.findAndModify(
            any(Query.class),
            any(Update.class),
            any(FindAndModifyOptions.class),
            eq(Habitacion.class)))
        .thenReturn(actualizada);

    HotelDTO hotelDto = HotelDTO.builder().id(5).cerrado(true).build();
    HabitacionDTO dto = HabitacionDTO.builder().habitacionId(100L).hotel(hotelDto).build();
    HabitacionEvent event =
        HabitacionEvent.builder().tipoEvento(TipoEvento.ACTUALIZAR_DATOS).habitacion(dto).build();

    // Act
    habitacionService.handleEvent(event);

    // Assert
    ArgumentCaptor<Reserva> captor = ArgumentCaptor.forClass(Reserva.class);
    verify(reservaService, times(1)).save(captor.capture());
    Reserva reserva = captor.getValue();
    assertEquals(EstadoReserva.CERRADA, reserva.getEstadoReserva());
    assertNull(reserva.getCheckOut());
    assertEquals("mongoId1", reserva.getIdHabitacion());
    assertEquals(5L, reserva.getHotelId());
  }

  @Test
  public void testActualizarDatosCreaReservaCerradaCuandoElEstadoPrevioDeCerradoEraNulo() {
    // Arrange: la habitacion guardada nunca tuvo seteado hotel.cerrado (null)
    Habitacion existente =
        Habitacion.builder()
            .id("mongoId1")
            .habitacionId(100L)
            .hotel(Hotel.builder().id(5).cerrado(null).build())
            .build();
    Habitacion actualizada =
        Habitacion.builder()
            .id("mongoId1")
            .habitacionId(100L)
            .hotel(Hotel.builder().id(5).cerrado(true).build())
            .build();
    when(mongoTemplate.findOne(any(Query.class), eq(Habitacion.class))).thenReturn(existente);
    when(mongoTemplate.findAndModify(
            any(Query.class),
            any(Update.class),
            any(FindAndModifyOptions.class),
            eq(Habitacion.class)))
        .thenReturn(actualizada);

    HotelDTO hotelDto = HotelDTO.builder().id(5).cerrado(true).build();
    HabitacionDTO dto = HabitacionDTO.builder().habitacionId(100L).hotel(hotelDto).build();
    HabitacionEvent event =
        HabitacionEvent.builder().tipoEvento(TipoEvento.ACTUALIZAR_DATOS).habitacion(dto).build();

    // Act
    habitacionService.handleEvent(event);

    // Assert
    verify(reservaService, times(1)).save(any(Reserva.class));
  }

  @Test
  public void testActualizarDatosNoDuplicaReservaCerradaEnReentregaDeMensaje() {
    // Arrange: la habitacion ya tenia guardado hotel.cerrado = true de un procesamiento anterior
    Habitacion existente =
        Habitacion.builder()
            .id("mongoId1")
            .habitacionId(100L)
            .hotel(Hotel.builder().id(5).cerrado(true).build())
            .build();
    Habitacion actualizada =
        Habitacion.builder()
            .id("mongoId1")
            .habitacionId(100L)
            .hotel(Hotel.builder().id(5).cerrado(true).build())
            .build();
    when(mongoTemplate.findOne(any(Query.class), eq(Habitacion.class))).thenReturn(existente);
    when(mongoTemplate.findAndModify(
            any(Query.class),
            any(Update.class),
            any(FindAndModifyOptions.class),
            eq(Habitacion.class)))
        .thenReturn(actualizada);

    HotelDTO hotelDto = HotelDTO.builder().id(5).cerrado(true).build();
    HabitacionDTO dto = HabitacionDTO.builder().habitacionId(100L).hotel(hotelDto).build();
    HabitacionEvent event =
        HabitacionEvent.builder().tipoEvento(TipoEvento.ACTUALIZAR_DATOS).habitacion(dto).build();

    // Act
    habitacionService.handleEvent(event);

    // Assert
    verify(reservaService, never()).save(any());
  }

  @Test
  public void testActualizarDatosNoCreaReservaEnActualizacionNormalSinCierre() {
    // Arrange: actualizacion de datos comun, el hotel sigue abierto antes y despues
    Habitacion existente =
        Habitacion.builder()
            .id("mongoId1")
            .habitacionId(100L)
            .hotel(Hotel.builder().id(5).cerrado(false).build())
            .build();
    Habitacion actualizada =
        Habitacion.builder()
            .id("mongoId1")
            .habitacionId(100L)
            .capacidad(4)
            .hotel(Hotel.builder().id(5).cerrado(false).build())
            .build();
    when(mongoTemplate.findOne(any(Query.class), eq(Habitacion.class))).thenReturn(existente);
    when(mongoTemplate.findAndModify(
            any(Query.class),
            any(Update.class),
            any(FindAndModifyOptions.class),
            eq(Habitacion.class)))
        .thenReturn(actualizada);

    HotelDTO hotelDto = HotelDTO.builder().id(5).cerrado(false).build();
    HabitacionDTO dto =
        HabitacionDTO.builder().habitacionId(100L).capacidad(4).hotel(hotelDto).build();
    HabitacionEvent event =
        HabitacionEvent.builder().tipoEvento(TipoEvento.ACTUALIZAR_DATOS).habitacion(dto).build();

    // Act
    habitacionService.handleEvent(event);

    // Assert
    verify(reservaService, never()).save(any());
  }

  @Test
  public void testActualizarDatosNoCreaReservaCuandoLaHabitacionNoExistiaAun() {
    // Arrange: findByHabitacionId no encuentra nada porque reservas-svc nunca supo de esta
    // habitacion (primer evento que le llega es un ACTUALIZAR_DATOS, no un CREAR previo); al no
    // existir el documento, updateByHabitacionId tampoco puede aplicar el update y falla.
    when(mongoTemplate.findOne(any(Query.class), eq(Habitacion.class))).thenReturn(null);
    when(mongoTemplate.findAndModify(
            any(Query.class),
            any(Update.class),
            any(FindAndModifyOptions.class),
            eq(Habitacion.class)))
        .thenReturn(null);

    HotelDTO hotelDto = HotelDTO.builder().id(5).cerrado(true).build();
    HabitacionDTO dto = HabitacionDTO.builder().habitacionId(100L).hotel(hotelDto).build();
    HabitacionEvent event =
        HabitacionEvent.builder().tipoEvento(TipoEvento.ACTUALIZAR_DATOS).habitacion(dto).build();

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> habitacionService.handleEvent(event));
    verify(reservaService, never()).save(any());
  }
}
