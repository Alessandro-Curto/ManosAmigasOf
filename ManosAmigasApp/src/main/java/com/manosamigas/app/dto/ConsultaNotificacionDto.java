package com.manosamigas.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsultaNotificacionDto {

    @JsonProperty("idNotificacion")
    private int idNotificacion;

    @JsonProperty("idEvento")
    private int idEvento;

    @JsonProperty("asunto")
    private String asunto;

    @JsonProperty("cuerpo")
    private String cuerpo;

    @JsonProperty("fechaProgramada")
    private String fechaProgramada;

    @JsonProperty("fechaEnvio")
    private String fechaEnvio;

    @JsonProperty("estado")
    private String estado;
}
