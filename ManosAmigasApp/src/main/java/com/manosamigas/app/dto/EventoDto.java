package com.manosamigas.app.dto;

import lombok.AllArgsConstructor; // <-- Asegúrate de que este import exista
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor // <-- ESTA ES LA ANOTACIÓN QUE PROBABLEMENTE FALTA
public class EventoDto {

    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String ubicacion;
    private int cupoMaximo;
    private Long idInteresRequerido;
}