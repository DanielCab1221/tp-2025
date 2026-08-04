package edu.utn.frsf.isi.dan.reservas_svc.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "reserva")
public class Reserva {
  @Id private String _id;

  @NotBlank(message = "El id de la habitacion es requerido")
  private String idHabitacion;

  private Long hotelId;
  private Instant createdAt;

  @NotNull(message = "El checkIn es requerido")
  private Instant checkIn;

  @NotNull(message = "El checkOut es requerido")
  private Instant checkOut;

  private Double precioNoche;
  private Double precioTotal;
  private String status;

  @NotNull(message = "Los datos del huesped son requeridos")
  @Valid
  private Huesped huesped;

  private List<Pago> pago;
  private Review clientReview;
  private Review hostReview;
  private EstadoReserva estadoReserva;
}
