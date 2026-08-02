package edu.utn.frsf.isi.dan.user.dto;

import jakarta.validation.constraints.NotBlank;

public record BancoDTO(@NotBlank(message = "El nombre del banco es requerido") String nombre) {}
