package edu.utn.frsf.isi.dan.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record HuespedUpdateDTO(
    @NotBlank(message = "El nombre no puede estar vacío")
    String nombre,
    
    @Email(message = "El email debe ser válido")
    String email,
    
    String telefono,
    
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    LocalDate fechaNacimiento
) {}
