package edu.utn.frsf.isi.dan.reservas_svc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

public class DisponibilidadServiceTest {

  @InjectMocks private DisponibilidadService disponibilidadService;

  @Mock private MongoTemplate mongoTemplate;

  private final Instant checkIn = Instant.now();
  private final Instant checkOut = checkIn.plus(3, ChronoUnit.DAYS);

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testTieneReservaQueSolapaDevuelveTrueSiHayReservaConflictiva() {
    // Arrange
    String idHabitacion = "hab-1";
    Reserva reservaExistente = Reserva.builder().idHabitacion(idHabitacion).build();
    when(mongoTemplate.find(any(Query.class), eq(Reserva.class)))
        .thenReturn(List.of(reservaExistente));

    // Act
    boolean resultado =
        disponibilidadService.tieneReservaQueSolapa(idHabitacion, checkIn, checkOut);

    // Assert
    assertTrue(resultado);
  }

  @Test
  public void testTieneReservaQueSolapaDevuelveFalseSiNoHayReservas() {
    // Arrange
    String idHabitacion = "hab-1";
    when(mongoTemplate.find(any(Query.class), eq(Reserva.class))).thenReturn(List.of());

    // Act
    boolean resultado =
        disponibilidadService.tieneReservaQueSolapa(idHabitacion, checkIn, checkOut);

    // Assert
    assertFalse(resultado);
  }

  @Test
  public void testIdsConReservaQueSolapaDevuelveSoloLosIdsConConflicto() {
    // Arrange
    String idHabitacionConConflicto = "hab-1";
    String idHabitacionSinConflicto = "hab-2";
    String idHabitacionSinReservas = "hab-3";
    Reserva reservaConflictiva = Reserva.builder().idHabitacion(idHabitacionConConflicto).build();
    when(mongoTemplate.find(any(Query.class), eq(Reserva.class)))
        .thenReturn(List.of(reservaConflictiva));

    // Act
    Set<String> resultado =
        disponibilidadService.idsConReservaQueSolapa(
            List.of(idHabitacionConConflicto, idHabitacionSinConflicto, idHabitacionSinReservas),
            checkIn,
            checkOut);

    // Assert
    assertEquals(1, resultado.size());
    assertTrue(resultado.contains(idHabitacionConConflicto));
    assertFalse(resultado.contains(idHabitacionSinConflicto));
    assertFalse(resultado.contains(idHabitacionSinReservas));
  }
}
