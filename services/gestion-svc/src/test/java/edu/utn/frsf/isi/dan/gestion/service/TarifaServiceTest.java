package edu.utn.frsf.isi.dan.gestion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.utn.frsf.isi.dan.gestion.dao.TarifaRepository;
import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import edu.utn.frsf.isi.dan.gestion.model.TipoHabitacion;
import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TarifaServiceTest {

  @InjectMocks private TarifaService tarifaService;

  @Mock private TarifaRepository tarifaRepository;

  @Mock private EventPublisherService eventPublisherService;

  private TipoHabitacion tipoHabitacion;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    tipoHabitacion = TipoHabitacion.builder().id(1).nombre("Doble").capacidad(2).build();
    when(tarifaRepository.save(any(Tarifa.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  private Tarifa tarifa(Integer id, LocalDate fechaInicio, LocalDate fechaFin, Double precio) {
    return Tarifa.builder()
        .id(id)
        .fechaInicio(fechaInicio)
        .fechaFin(fechaFin)
        .tipoHabitacion(tipoHabitacion)
        .precioNoche(precio)
        .build();
  }

  // ---------------------------------------------------------------------
  // crear() - validaciones
  // ---------------------------------------------------------------------

  @Test
  public void testCrearFallaSinTipoHabitacion() {
    Tarifa request = Tarifa.builder().precioNoche(100.0).build();

    assertThrows(IllegalArgumentException.class, () -> tarifaService.crear(request));
    verify(tarifaRepository, never()).save(any());
  }

  @Test
  public void testCrearFallaConTipoHabitacionSinId() {
    Tarifa request =
        Tarifa.builder()
            .tipoHabitacion(TipoHabitacion.builder().build())
            .precioNoche(100.0)
            .build();

    assertThrows(IllegalArgumentException.class, () -> tarifaService.crear(request));
    verify(tarifaRepository, never()).save(any());
  }

  @Test
  public void testCrearFallaConPrecioNulo() {
    Tarifa request = Tarifa.builder().tipoHabitacion(tipoHabitacion).precioNoche(null).build();

    assertThrows(IllegalArgumentException.class, () -> tarifaService.crear(request));
    verify(tarifaRepository, never()).save(any());
  }

  @Test
  public void testCrearFallaConPrecioCero() {
    Tarifa request = Tarifa.builder().tipoHabitacion(tipoHabitacion).precioNoche(0.0).build();

    assertThrows(IllegalArgumentException.class, () -> tarifaService.crear(request));
    verify(tarifaRepository, never()).save(any());
  }

  @Test
  public void testCrearFallaConPrecioNegativo() {
    Tarifa request = Tarifa.builder().tipoHabitacion(tipoHabitacion).precioNoche(-50.0).build();

    assertThrows(IllegalArgumentException.class, () -> tarifaService.crear(request));
    verify(tarifaRepository, never()).save(any());
  }

  // ---------------------------------------------------------------------
  // crear() - tarifa continua (sin fechaFin)
  // ---------------------------------------------------------------------

  @Test
  public void testCrearContinuaSinVigentePrevia() {
    LocalDate fechaInicio = LocalDate.of(2026, 1, 1);
    Tarifa request =
        Tarifa.builder()
            .tipoHabitacion(tipoHabitacion)
            .fechaInicio(fechaInicio)
            .precioNoche(150.0)
            .build();

    when(tarifaRepository.findVigentes(1, fechaInicio)).thenReturn(Collections.emptyList());
    when(tarifaRepository.findVigentes(1, LocalDate.now())).thenReturn(Collections.emptyList());

    Tarifa resultado = tarifaService.crear(request);

    assertEquals(fechaInicio, resultado.getFechaInicio());
    assertNull(resultado.getFechaFin());
    assertEquals(150.0, resultado.getPrecioNoche());
    verify(tarifaRepository, times(1)).save(any(Tarifa.class));
    verify(tarifaRepository, never()).deleteById(any());

    verify(eventPublisherService).publicar(any(HabitacionEvent.class));
  }

  @Test
  public void testCrearContinuaConVigenteAnteriorQueEmpiezaAntes() {
    LocalDate fechaInicioVigente = LocalDate.of(2025, 1, 1);
    LocalDate fechaInicioNueva = LocalDate.of(2026, 1, 1);
    Tarifa vigenteAnterior = tarifa(5, fechaInicioVigente, null, 100.0);

    Tarifa request =
        Tarifa.builder()
            .tipoHabitacion(tipoHabitacion)
            .fechaInicio(fechaInicioNueva)
            .precioNoche(200.0)
            .build();

    when(tarifaRepository.findVigentes(1, fechaInicioNueva))
        .thenReturn(new ArrayList<>(List.of(vigenteAnterior)));
    when(tarifaRepository.findVigentes(1, LocalDate.now())).thenReturn(Collections.emptyList());

    Tarifa resultado = tarifaService.crear(request);

    // la vigente anterior se cierra un dia antes, no se borra
    assertEquals(fechaInicioNueva.minusDays(1), vigenteAnterior.getFechaFin());
    verify(tarifaRepository, never()).deleteById(5);
    verify(tarifaRepository).save(vigenteAnterior);

    assertEquals(fechaInicioNueva, resultado.getFechaInicio());
    assertNull(resultado.getFechaFin());
    assertEquals(200.0, resultado.getPrecioNoche());
  }

  @Test
  public void testCrearContinuaConVigenteAnteriorMismaFechaInicioSeBorra() {
    LocalDate fechaInicio = LocalDate.of(2026, 1, 1);
    Tarifa vigenteAnterior = tarifa(5, fechaInicio, null, 100.0);

    Tarifa request =
        Tarifa.builder()
            .tipoHabitacion(tipoHabitacion)
            .fechaInicio(fechaInicio)
            .precioNoche(200.0)
            .build();

    when(tarifaRepository.findVigentes(1, fechaInicio))
        .thenReturn(new ArrayList<>(List.of(vigenteAnterior)));
    when(tarifaRepository.findVigentes(1, LocalDate.now())).thenReturn(Collections.emptyList());

    tarifaService.crear(request);

    verify(tarifaRepository).deleteById(5);
    verify(tarifaRepository, never()).save(vigenteAnterior);
  }

  // ---------------------------------------------------------------------
  // crear() - tarifa promocional (con fechaFin)
  // ---------------------------------------------------------------------

  @Test
  public void testCrearPromocionalCreaContinuacionConPrecioDeVigenteAnterior() {
    LocalDate fechaInicioVigente = LocalDate.of(2025, 1, 1);
    LocalDate fechaInicioPromo = LocalDate.of(2026, 1, 1);
    LocalDate fechaFinPromo = LocalDate.of(2026, 1, 10);
    Tarifa vigenteAnterior = tarifa(5, fechaInicioVigente, null, 100.0);

    Tarifa request =
        Tarifa.builder()
            .tipoHabitacion(tipoHabitacion)
            .fechaInicio(fechaInicioPromo)
            .fechaFin(fechaFinPromo)
            .precioNoche(50.0)
            .build();

    when(tarifaRepository.findVigentes(1, fechaInicioPromo))
        .thenReturn(new ArrayList<>(List.of(vigenteAnterior)));
    when(tarifaRepository.findVigentes(1, LocalDate.now())).thenReturn(Collections.emptyList());

    Tarifa resultado = tarifaService.crear(request);

    // se guarda la promo
    assertEquals(fechaInicioPromo, resultado.getFechaInicio());
    assertEquals(fechaFinPromo, resultado.getFechaFin());
    assertEquals(50.0, resultado.getPrecioNoche());

    // la vigente anterior se cierra un dia antes de la promo
    assertEquals(fechaInicioPromo.minusDays(1), vigenteAnterior.getFechaFin());

    // se guarda: cierre de vigente anterior + promo + continuacion = 3 saves
    verify(tarifaRepository, times(3)).save(any(Tarifa.class));

    org.mockito.ArgumentCaptor<Tarifa> captor = org.mockito.ArgumentCaptor.forClass(Tarifa.class);
    verify(tarifaRepository, times(3)).save(captor.capture());
    List<Tarifa> guardadas = captor.getAllValues();

    Tarifa continuacion =
        guardadas.stream()
            .filter(t -> t.getFechaInicio().equals(fechaFinPromo.plusDays(1)))
            .findFirst()
            .orElse(null);

    assertTrue(continuacion != null, "Debe haberse creado la tarifa de continuacion");
    assertNull(continuacion.getFechaFin());
    // retoma el precio de la vigente anterior a la promo, no el de la promo
    assertEquals(100.0, continuacion.getPrecioNoche());
  }

  @Test
  public void testCrearPromocionalSinVigentePreviaRetomaPrecioDeLaPromo() {
    LocalDate fechaInicioPromo = LocalDate.of(2026, 1, 1);
    LocalDate fechaFinPromo = LocalDate.of(2026, 1, 10);

    Tarifa request =
        Tarifa.builder()
            .tipoHabitacion(tipoHabitacion)
            .fechaInicio(fechaInicioPromo)
            .fechaFin(fechaFinPromo)
            .precioNoche(50.0)
            .build();

    when(tarifaRepository.findVigentes(1, fechaInicioPromo)).thenReturn(Collections.emptyList());
    when(tarifaRepository.findVigentes(1, LocalDate.now())).thenReturn(Collections.emptyList());

    tarifaService.crear(request);

    org.mockito.ArgumentCaptor<Tarifa> captor = org.mockito.ArgumentCaptor.forClass(Tarifa.class);
    verify(tarifaRepository, times(2)).save(captor.capture());
    List<Tarifa> guardadas = captor.getAllValues();

    Tarifa continuacion =
        guardadas.stream()
            .filter(t -> t.getFechaInicio().equals(fechaFinPromo.plusDays(1)))
            .findFirst()
            .orElse(null);

    assertTrue(continuacion != null, "Debe haberse creado la tarifa de continuacion");
    assertEquals(50.0, continuacion.getPrecioNoche());
  }

  @Test
  public void testCrearPromocionalFallaSiFechaFinNoEsPosteriorAFechaInicio() {
    LocalDate fecha = LocalDate.of(2026, 1, 1);
    Tarifa request =
        Tarifa.builder()
            .tipoHabitacion(tipoHabitacion)
            .fechaInicio(fecha)
            .fechaFin(fecha)
            .precioNoche(50.0)
            .build();

    assertThrows(IllegalArgumentException.class, () -> tarifaService.crear(request));
    verify(tarifaRepository, never()).save(any());
  }

  @Test
  public void testCrearPromocionalFallaSiFechaFinAnteriorAFechaInicio() {
    LocalDate fechaInicio = LocalDate.of(2026, 1, 10);
    LocalDate fechaFin = LocalDate.of(2026, 1, 1);
    Tarifa request =
        Tarifa.builder()
            .tipoHabitacion(tipoHabitacion)
            .fechaInicio(fechaInicio)
            .fechaFin(fechaFin)
            .precioNoche(50.0)
            .build();

    assertThrows(IllegalArgumentException.class, () -> tarifaService.crear(request));
    verify(tarifaRepository, never()).save(any());
  }

  // ---------------------------------------------------------------------
  // eliminar()
  // ---------------------------------------------------------------------

  @Test
  public void testEliminarFallaSiEsLaUnicaTarifa() {
    Tarifa tarifa = tarifa(1, LocalDate.of(2026, 1, 1), null, 100.0);
    when(tarifaRepository.findById(1)).thenReturn(Optional.of(tarifa));
    when(tarifaRepository.findByTipoHabitacionIdOrderByFechaInicioDesc(1))
        .thenReturn(List.of(tarifa));

    assertThrows(IllegalArgumentException.class, () -> tarifaService.eliminar(1));
    verify(tarifaRepository, never()).deleteById(any());
  }

  @Test
  public void testEliminarTarifaNoVigenteSeBorraDirecto() {
    LocalDate hoy = LocalDate.now();
    Tarifa vigente = tarifa(1, hoy.minusDays(5), null, 100.0);
    Tarifa noVigenteFutura = tarifa(2, hoy.plusDays(10), null, 200.0);
    // Nota: como "vigente" no tiene fechaFin, para poder tener otra tarifa futura sin solaparse
    // conceptualmente alcanza para el test de mock (el servicio no valida solapamientos al borrar).

    when(tarifaRepository.findById(2)).thenReturn(Optional.of(noVigenteFutura));
    when(tarifaRepository.findByTipoHabitacionIdOrderByFechaInicioDesc(1))
        .thenReturn(new ArrayList<>(Arrays.asList(noVigenteFutura, vigente)));

    tarifaService.eliminar(2);

    verify(tarifaRepository).deleteById(2);
    // no se toca ninguna otra tarifa
    verify(tarifaRepository, never()).save(any());
    verify(eventPublisherService).publicar(any(HabitacionEvent.class));
  }

  @Test
  public void testEliminarTarifaVigenteReactivaLaAnteriorMasReciente() {
    LocalDate hoy = LocalDate.now();
    Tarifa antigua = tarifa(1, hoy.minusDays(30), hoy.minusDays(11), 80.0);
    Tarifa masReciente = tarifa(2, hoy.minusDays(10), hoy.minusDays(1), 90.0);
    Tarifa vigente = tarifa(3, hoy, null, 100.0);

    when(tarifaRepository.findById(3)).thenReturn(Optional.of(vigente));
    when(tarifaRepository.findByTipoHabitacionIdOrderByFechaInicioDesc(1))
        .thenReturn(new ArrayList<>(Arrays.asList(vigente, masReciente, antigua)));

    tarifaService.eliminar(3);

    verify(tarifaRepository).deleteById(3);
    assertNull(masReciente.getFechaFin());
    verify(tarifaRepository).save(masReciente);
    // la mas antigua no se toca
    assertEquals(hoy.minusDays(11), antigua.getFechaFin());
    verify(tarifaRepository, never()).save(antigua);
  }

  @Test
  public void testEliminarConIdInexistenteLanzaEntityNotFoundException() {
    when(tarifaRepository.findById(99)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> tarifaService.eliminar(99));
    verify(tarifaRepository, never()).deleteById(any());
  }

  // ---------------------------------------------------------------------
  // obtenerVigente() / obtenerPrecioVigente()
  // ---------------------------------------------------------------------

  @Test
  public void testObtenerVigenteDevuelveVacioSiNoHayVigentes() {
    when(tarifaRepository.findVigentes(anyInt(), any(LocalDate.class)))
        .thenReturn(Collections.emptyList());

    Optional<Tarifa> resultado = tarifaService.obtenerVigente(1, LocalDate.now());

    assertFalse(resultado.isPresent());
  }

  @Test
  public void testObtenerVigenteDevuelvePrimeraDeLaLista() {
    Tarifa vigente = tarifa(1, LocalDate.now(), null, 120.0);
    when(tarifaRepository.findVigentes(1, LocalDate.now())).thenReturn(List.of(vigente));

    Optional<Tarifa> resultado = tarifaService.obtenerVigente(1, LocalDate.now());

    assertTrue(resultado.isPresent());
    assertEquals(vigente, resultado.get());
  }

  @Test
  public void testObtenerPrecioVigenteDevuelveCeroSiNoHayVigentes() {
    when(tarifaRepository.findVigentes(anyInt(), any(LocalDate.class)))
        .thenReturn(Collections.emptyList());

    Double precio = tarifaService.obtenerPrecioVigente(1);

    assertEquals(0.0, precio);
  }

  @Test
  public void testObtenerPrecioVigenteDevuelvePrecioDeLaVigente() {
    Tarifa vigente = tarifa(1, LocalDate.now(), null, 175.0);
    when(tarifaRepository.findVigentes(1, LocalDate.now())).thenReturn(List.of(vigente));

    Double precio = tarifaService.obtenerPrecioVigente(1);

    assertEquals(175.0, precio);
  }
}
