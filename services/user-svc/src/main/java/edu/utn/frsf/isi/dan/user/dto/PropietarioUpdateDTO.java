package edu.utn.frsf.isi.dan.user.dto;

public record PropietarioUpdateDTO(
    String nombre,
    String email,
    String telefono,
    Long idHotel
) {}
