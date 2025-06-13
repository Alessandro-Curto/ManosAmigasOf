package com.manosamigas.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistroNotificacionDto {

    @JsonProperty("idEvento")
    private int idEvento;

    @JsonProperty("asunto")
    private String asunto;

    @JsonProperty("cuerpo")
    private String cuerpo;

    @JsonProperty("fechaProgramada")
    private String fechaProgramada;

    @JsonProperty("idVoluntarios")
    private List<Integer> idVoluntarios;
}
