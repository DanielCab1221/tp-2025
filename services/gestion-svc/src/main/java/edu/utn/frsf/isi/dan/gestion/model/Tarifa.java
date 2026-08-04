package edu.utn.frsf.isi.dan.gestion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "tarifa", schema = "tp_dan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarifa {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  // Sin valor: se asume "hoy" (tarifa continua). Ver TarifaService.crear.
  private LocalDate fechaInicio;

  // Sin valor: tarifa continua (sin fecha de fin). Ver TarifaService.crear.
  private LocalDate fechaFin;

  @NotNull(message = "El tipo de habitacion es requerido")
  @ManyToOne
  @JoinColumn(name = "id_tipo_habitacion")
  private TipoHabitacion tipoHabitacion;

  @NotNull(message = "El precio por noche es requerido")
  @Positive(message = "El precio por noche debe ser mayor a cero")
  private Double precioNoche;
}
