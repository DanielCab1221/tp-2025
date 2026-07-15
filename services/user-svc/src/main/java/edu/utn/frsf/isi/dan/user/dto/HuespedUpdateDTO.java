package edu.utn.frsf.isi.dan.user.dto;

import java.time.LocalDate;

public record HuespedUpdateDTO(
    String nombre,
    String email,
    String telefono,
    LocalDate fechaNacimiento
) {}
