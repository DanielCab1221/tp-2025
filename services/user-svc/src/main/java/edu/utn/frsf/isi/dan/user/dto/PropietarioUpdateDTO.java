package edu.utn.frsf.isi.dan.user.dto;

import jakarta.validation.constraints.Email;

public record PropietarioUpdateDTO(
    String nombre,
    @Email(message = "El email debe ser válido") String email,
    String telefono,
    Long idHotel) {}
