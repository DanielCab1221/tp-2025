package edu.utn.frsf.isi.dan.user.dto;

public record TarjetaCreditoDTO(
    String numero,
    String nombreTitular,
    String fechaVencimiento,
    String cvc,
    Boolean esPrincipal,
    Integer idBanco
) {}
