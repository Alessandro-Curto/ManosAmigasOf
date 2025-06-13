package com.manosamigas.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class NotificacionAutomaticaService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmailService emailService;

    // ⏰ Ejecuta esta tarea todos los días a las 8:00 AM
    @Scheduled(cron = "0 */30 * * * *")
    public void enviarNotificacionesPrevias() {
        LocalDate fechaObjetivo = LocalDate.now().plusDays(1); // mañana

        String sql = """
            SELECT n.id_notificacion, n.asunto, n.cuerpo, v.email
            FROM notificaciones n
            JOIN notificacion_voluntarios nv ON n.id_notificacion = nv.id_notificacion
            JOIN voluntarios v ON nv.id_voluntario = v.id_voluntario
            WHERE CONVERT(DATE, n.fecha_programada) = ?
              AND nv.enviado = 0
        """;

        List<Map<String, Object>> pendientes = jdbcTemplate.queryForList(sql, fechaObjetivo);

        for (Map<String, Object> fila : pendientes) {
            String email = (String) fila.get("email");
            String asunto = (String) fila.get("asunto");
            String cuerpo = (String) fila.get("cuerpo");
            int idNotificacion = (int) fila.get("id_notificacion");

            try {
                emailService.enviarCorreo(email, asunto, cuerpo);
                // Marca como enviado
                jdbcTemplate.update("""
                    UPDATE notificacion_voluntarios
                    SET enviado = 1, fecha_envio = GETDATE()
                    WHERE id_notificacion = ? AND id_voluntario = (
                        SELECT id_voluntario FROM voluntarios WHERE email = ?
                    )
                """, idNotificacion, email);
                System.out.println(" Correo enviado a " + email);
            } catch (Exception e) {
                System.err.println(" Error al enviar correo a " + email + ": " + e.getMessage());
            }
        }

        System.out.println("🕗 Notificaciones revisadas para " + fechaObjetivo);
    }
}

