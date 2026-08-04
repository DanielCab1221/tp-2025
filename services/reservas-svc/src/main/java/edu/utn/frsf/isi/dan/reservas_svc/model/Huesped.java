package edu.utn.frsf.isi.dan.reservas_svc.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Huesped {

  @NotBlank(message = "El id de usuario es requerido")
  private String idUsuario;

  @NotBlank(message = "El nombre y apellido son requeridos")
  private String nombreApellido;

  private String email;
}
