package edu.utn.frsf.isi.dan.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PropietarioUpdateDTO(
    @NotBlank(message = "El nombre no puede estar vacío")
    String nombre,
    
    @Email(message = "El email debe ser válido")
    String email,
    
    String telefono,
    
    Long idHotel
) {}
