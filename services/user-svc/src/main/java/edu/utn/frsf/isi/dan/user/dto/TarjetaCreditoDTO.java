package edu.utn.frsf.isi.dan.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TarjetaCreditoDTO(
    @NotBlank(message = "El número de tarjeta es requerido")
        @Pattern(regexp = "\\d{16}", message = "El número de tarjeta debe tener 16 dígitos")
        String numero,
    @NotBlank(message = "El nombre del titular es requerido") String nombreTitular,
    @NotBlank(message = "La fecha de vencimiento es requerida")
        @Pattern(regexp = "\\d{2}/\\d{2}", message = "Formato debe ser MM/YY")
        String fechaVencimiento,
    @NotBlank(message = "El código de seguridad es requerido")
        @Pattern(regexp = "\\d{3,4}", message = "El CVC debe tener 3 o 4 dígitos")
        String cvc,
    Boolean esPrincipal,
    @NotNull(message = "El ID del banco es requerido") Integer idBanco) {}
