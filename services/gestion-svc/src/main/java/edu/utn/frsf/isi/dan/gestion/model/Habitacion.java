package edu.utn.frsf.isi.dan.gestion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(name = "habitacion", schema = "tp_dan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habitacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "El numero de habitacion es requerido")
    @Positive(message = "El numero de habitacion debe ser mayor a cero")
    private Integer numero;

    @NotNull(message = "El piso es requerido")
    @Positive(message = "El piso debe ser mayor a cero")
    private Integer piso;
    @ManyToOne
    @JoinColumn(name = "id_tipo")
    private TipoHabitacion tipoHabitacion;
    @ManyToOne
    @JoinColumn(name = "id_hotel")
    private Hotel hotel;
    @Builder.Default
    private Boolean disponible = true;

}
