package com.manosamigas.app.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString

public class RegistrarAsistenciaDto {
    private int idUsuario;
    private int idAsignacion;
    private Boolean asistio;
    private int horasTrabajadas;
}