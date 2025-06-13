package com.manosamigas.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manosamigas.app.dto.ConsultaNotificacionDto;

import java.util.Map;

@Service
public class ConsultaNotificacionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public ConsultaNotificacionDto consultarNotificacion(int id) {
        String sql = """
            SELECT id_evento, asunto, cuerpo, fecha_programada, fecha_envio, estado
            FROM notificaciones
            WHERE id_notificacion = ?
        """;

        Map<String, Object> row = jdbcTemplate.queryForMap(sql, id);

        return new ConsultaNotificacionDto(
                id,
                (int) row.get("id_evento"),
                (String) row.get("asunto"),
                (String) row.get("cuerpo"),
                row.get("fecha_programada").toString(),
                row.get("fecha_envio") != null ? row.get("fecha_envio").toString() : null,
                (String) row.get("estado")
        );
    }
}