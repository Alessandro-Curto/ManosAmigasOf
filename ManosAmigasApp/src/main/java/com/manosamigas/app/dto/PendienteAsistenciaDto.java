package com.manosamigas.app.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString

public class PendienteAsistenciaDto {
    private int idAsignacion;
    private String voluntario;
    private String evento;
}


