package edu.utn.frsf.isi.dan.reservas_svc.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarifa {
  @NotNull(message = "El precio es requerido")
  @Positive(message = "El precio debe ser mayor a cero")
  private Double precio;

  @NotBlank(message = "La moneda es requerida")
  private String moneda;
}
