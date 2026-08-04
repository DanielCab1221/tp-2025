package edu.utn.frsf.isi.dan.gestion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "tipo_habitacion", schema = "tp_dan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoHabitacion {
  @Id private Integer id;

  @NotBlank(message = "El nombre es requerido")
  private String nombre;

  private String descripcion;
  private Integer capacidad;
}
