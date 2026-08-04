package edu.utn.frsf.isi.dan.gestion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "hotel", schema = "tp_dan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotBlank(message = "El nombre es requerido")
  private String nombre;

  @NotBlank(message = "El CUIT es requerido")
  private String cuit;

  @NotBlank(message = "El domicilio es requerido")
  private String domicilio;

  @NotNull(message = "La latitud es requerida")
  private Double latitud;

  @NotNull(message = "La longitud es requerida")
  private Double longitud;

  private String telefono;
  private String correoContacto;
  private Integer categoria;

  @Builder.Default private Boolean cerrado = false;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "hotel")
  @JsonIgnore
  private List<Habitacion> habitaciones;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "hotel")
  private List<AmenityHotel> amenities;
}
