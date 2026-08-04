package edu.utn.frsf.isi.dan.gestion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import edu.utn.frsf.isi.dan.gestion.dao.AmenityHotelRepository;
import edu.utn.frsf.isi.dan.gestion.dao.HabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.dao.HotelRepository;
import edu.utn.frsf.isi.dan.gestion.model.Amenity;
import edu.utn.frsf.isi.dan.gestion.model.AmenityHotel;
import edu.utn.frsf.isi.dan.gestion.model.Habitacion;
import edu.utn.frsf.isi.dan.gestion.model.Hotel;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class HotelServiceTest {

  @InjectMocks private HotelService hotelService;

  @Mock private HotelRepository hotelRepository;

  @Mock private HabitacionRepository habitacionRepository;

  @Mock private HabitacionService habitacionService;

  @Mock private AmenityHotelRepository amenityHotelRepository;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testSaveFallaSiYaExisteHotelConMismoCuitYEsAlta() {
    // Arrange
    Hotel hotel = Hotel.builder().cuit("30-12345678-9").build();
    when(hotelRepository.existsByCuit("30-12345678-9")).thenReturn(true);

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> hotelService.save(hotel));
    verify(hotelRepository, never()).save(any(Hotel.class));
  }

  @Test
  public void testSaveNoValidaCuitDuplicadoSiEsActualizacion() {
    // Arrange
    Hotel hotel = Hotel.builder().id(1).cuit("30-12345678-9").build();
    when(hotelRepository.save(any(Hotel.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Hotel result = hotelService.save(hotel);

    // Assert
    assertEquals(hotel, result);
    verify(hotelRepository, never()).existsByCuit(anyString());
    verify(hotelRepository).save(hotel);
  }

  @Test
  public void testCerrarHotelConVariasHabitacionesLasMarcaNoDisponibles() {
    // Arrange
    Integer hotelId = 1;
    Hotel hotel = Hotel.builder().id(hotelId).cerrado(false).build();

    Habitacion h1 = Habitacion.builder().id(10).disponible(true).build();
    Habitacion h2 = Habitacion.builder().id(11).disponible(true).build();
    Habitacion h3 = Habitacion.builder().id(12).disponible(true).build();
    List<Habitacion> habitaciones = new ArrayList<>(List.of(h1, h2, h3));

    when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
    when(hotelRepository.save(any(Hotel.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(habitacionRepository.findByHotelId(hotelId)).thenReturn(habitaciones);

    // Act
    Optional<Hotel> result = hotelService.cerrar(hotelId);

    // Assert
    assertTrue(result.isPresent());
    assertTrue(result.get().getCerrado());
    verify(hotelRepository).save(hotel);

    ArgumentCaptor<Habitacion> captor = ArgumentCaptor.forClass(Habitacion.class);
    verify(habitacionService, times(3)).save(captor.capture());
    for (Habitacion capturada : captor.getAllValues()) {
      assertFalse(capturada.getDisponible());
    }
  }

  @Test
  public void testCerrarHotelInexistenteDevuelveOptionalEmptyYNoTocaHabitaciones() {
    // Arrange
    Integer hotelId = 999;
    when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

    // Act
    Optional<Hotel> result = hotelService.cerrar(hotelId);

    // Assert
    assertTrue(result.isEmpty());
    verifyNoInteractions(habitacionRepository);
    verifyNoInteractions(habitacionService);
  }

  @Test
  public void testCerrarHotelSinHabitacionesQuedaCerradoSinInvocarHabitacionService() {
    // Arrange
    Integer hotelId = 2;
    Hotel hotel = Hotel.builder().id(hotelId).cerrado(false).build();

    when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
    when(hotelRepository.save(any(Hotel.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(habitacionRepository.findByHotelId(hotelId)).thenReturn(List.of());

    // Act
    Optional<Hotel> result = hotelService.cerrar(hotelId);

    // Assert
    assertTrue(result.isPresent());
    assertTrue(result.get().getCerrado());
    verify(habitacionService, never()).save(any(Habitacion.class));
  }

  @Test
  public void testAgregarAmenitiesNoDuplicaLosYaExistentes() {
    // Arrange
    Integer hotelId = 1;
    Hotel hotel = Hotel.builder().id(hotelId).build();
    AmenityHotel amenityHotelExistente =
        AmenityHotel.builder().hotel(hotel).amenity(Amenity.WIFI).build();

    when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
    when(amenityHotelRepository.findByHotelIdAndAmenity(hotelId, Amenity.WIFI))
        .thenReturn(Optional.of(amenityHotelExistente));
    when(amenityHotelRepository.findByHotelIdAndAmenity(hotelId, Amenity.PILETA))
        .thenReturn(Optional.empty());

    // Act
    hotelService.agregarAmenities(hotelId, List.of(Amenity.WIFI, Amenity.PILETA));

    // Assert
    ArgumentCaptor<AmenityHotel> captor = ArgumentCaptor.forClass(AmenityHotel.class);
    verify(amenityHotelRepository, times(1)).save(captor.capture());
    assertEquals(Amenity.PILETA, captor.getValue().getAmenity());
  }

  @Test
  public void testAgregarAmenitiesConHotelInexistenteLanzaExcepcion() {
    // Arrange
    Integer hotelId = 404;
    when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        EntityNotFoundException.class,
        () -> hotelService.agregarAmenities(hotelId, List.of(Amenity.WIFI)));
    verifyNoInteractions(amenityHotelRepository);
  }

  @Test
  public void testQuitarAmenityCuandoHotelNoLoTieneLanzaExcepcion() {
    // Arrange
    Integer hotelId = 1;
    when(amenityHotelRepository.findByHotelIdAndAmenity(hotelId, Amenity.SPA))
        .thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        EntityNotFoundException.class, () -> hotelService.quitarAmenity(hotelId, Amenity.SPA));
    verify(amenityHotelRepository, never()).delete(any(AmenityHotel.class));
  }

  @Test
  public void testActualizarDatosPermitidosSoloActualizaCategoriaTelefonoYCorreo() {
    // Arrange
    Integer hotelId = 1;
    Hotel hotelExistente =
        Hotel.builder()
            .id(hotelId)
            .nombre("Hotel Original")
            .cuit("30-11111111-1")
            .domicilio("Calle Falsa 123")
            .categoria(3)
            .telefono("111-1111")
            .correoContacto("original@hotel.com")
            .build();

    Hotel datosActualizados =
        Hotel.builder()
            .nombre("Hotel Hackeado")
            .cuit("30-99999999-9")
            .domicilio("Otra Direccion 999")
            .categoria(5)
            .telefono("222-2222")
            .correoContacto("nuevo@hotel.com")
            .build();

    when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotelExistente));
    when(hotelRepository.save(any(Hotel.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Optional<Hotel> result = hotelService.actualizarDatosPermitidos(hotelId, datosActualizados);

    // Assert
    assertTrue(result.isPresent());
    Hotel actualizado = result.get();
    assertEquals(5, actualizado.getCategoria());
    assertEquals("222-2222", actualizado.getTelefono());
    assertEquals("nuevo@hotel.com", actualizado.getCorreoContacto());
    assertEquals("Hotel Original", actualizado.getNombre());
    assertEquals("30-11111111-1", actualizado.getCuit());
    assertEquals("Calle Falsa 123", actualizado.getDomicilio());
  }
}
