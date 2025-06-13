package com.manosamigas.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manosamigas.app.dto.RegistroNotificacionDto;
import com.manosamigas.app.dto.ConsultaNotificacionDto;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
public class RegistroNotificacionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmailService emailService;

    @Transactional(rollbackFor = Exception.class)
    public ConsultaNotificacionDto procesar(RegistroNotificacionDto dto) {











        validarEvento(dto.getIdEvento());

        // Insertar notificación
        String insertSQL = """
            INSERT INTO Notificaciones(id_evento, asunto, cuerpo, fecha_programada, estado)
            OUTPUT inserted.id_notificacion
            VALUES (?, ?, ?, ?, ?)
        """;

        LocalDateTime fechaProgramada = LocalDateTime.parse(dto.getFechaProgramada(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        int idNotificacion = jdbcTemplate.queryForObject(
                insertSQL, Integer.class,
                dto.getIdEvento(), dto.getAsunto(), dto.getCuerpo(), fechaProgramada, "PENDIENTE");

        for (Integer idVol : dto.getIdVoluntarios()) {
            validarVoluntario(idVol);
            String email = obtenerEmail(idVol);

            boolean enviado = false;
            String error = null;
            LocalDateTime fechaEnvio = null;

            try {
                emailService.enviarCorreo(email, dto.getAsunto(), dto.getCuerpo());
                enviado = true;
                fechaEnvio = LocalDateTime.now();
            } catch (Exception e) {
                error = e.getMessage();
            }

            jdbcTemplate.update("""
                INSERT INTO NotificacionVoluntarios(id_notificacion, id_voluntario, enviado, fecha_envio, error)
                VALUES (?, ?, ?, ?, ?)
            """, idNotificacion, idVol, enviado, fechaEnvio, error);
        }

        jdbcTemplate.update("""
            UPDATE Notificaciones SET estado = ?, fecha_envio = ? WHERE id_notificacion = ?
        """, "ENVIADO", LocalDateTime.now(), idNotificacion);

        return new ConsultaNotificacionDto(
                idNotificacion,
                dto.getIdEvento(),
                dto.getAsunto(),
                dto.getCuerpo(),
                dto.getFechaProgramada(),
                LocalDateTime.now().toString(),
                "ENVIADO"
        );
    }

    private void validarEvento(int idEvento) {
        String sql = "SELECT COUNT(*) FROM Eventos WHERE id_evento = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idEvento);
        if (count == null || count == 0) throw new RuntimeException("El evento no existe.");
    }

    private void validarVoluntario(int idVol) {
        String sql = "SELECT COUNT(*) FROM Voluntarios WHERE id_voluntario = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idVol);
        if (count == null || count == 0) throw new RuntimeException("El voluntario no existe.");
    }

    private String obtenerEmail(int idVol) {
        return jdbcTemplate.queryForObject(
                "SELECT email FROM Voluntarios WHERE id_voluntario = ?",
                String.class, idVol);
    }
}