package edu.utn.frsf.isi.dan.reservas_svc.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {
  @NotBlank(message = "El metodo de pago es requerido")
  private String method;

  @NotBlank(message = "El id de transaccion es requerido")
  private String transactionId;

  @NotNull(message = "El monto es requerido")
  @Valid
  private Tarifa amount;

  private String status;
}
