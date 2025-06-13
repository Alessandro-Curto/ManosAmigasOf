package com.manosamigas.app.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder

public class AsignarDto {
    private int idUsuario;
    private int idVoluntario;
    private int idEvento;
    private String Mensaje;
}
