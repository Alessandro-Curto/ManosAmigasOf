package com.manosamigas.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.manosamigas.app.dto.PendienteAsistenciaDto;
import com.manosamigas.app.dto.RegistrarAsistenciaDto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//REQUERIMIENTO 4

@Service
public class RegistrarAsistenciaService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public RegistrarAsistenciaDto registrarAsistencia(RegistrarAsistenciaDto bean) {

        int idUsuario = bean.getIdUsuario();
        int idAsignacion = bean.getIdAsignacion();
        if (bean.getAsistio() == null) {
            throw new RuntimeException("Debe indicar si el voluntario asistió o no.");
        }
        boolean asistio = bean.getAsistio();
        int horas = bean.getHorasTrabajadas();

        // Validaciones
        validarUsuario(idUsuario);
        validarCoordinador(idUsuario);
        validarAsignacionConfirmada(idAsignacion);
        validarEventoFinalizado(idAsignacion);
        validarAsistenciaNoRegistrada(idAsignacion);
        validarHoras(asistio, horas);

        // Proceso
        String sql = """
            UPDATE Asignaciones
            SET asistio = ?, horas_trabajadas = ?
            WHERE id_asignacion = ?
        """;
        jdbcTemplate.update(sql, asistio, horas, idAsignacion);

        return bean;
    }
    private void validarUsuario(int idUsuario) {
        String sql = """
                SELECT COUNT(1) cont FROM Usuarios WHERE id_usuario=?
                """;
        int count = jdbcTemplate.queryForObject(sql, Integer.class, idUsuario);
        if (count == 0) throw new RuntimeException("ERROR: Usuario no existe.");
    }
    private void validarCoordinador(int idUsuario) {
        String sql = """
            SELECT r.nombre FROM Usuarios u
            JOIN Roles r ON r.id_rol = u.id_rol
            WHERE u.id_usuario = ?
        """;
        String rol = jdbcTemplate.queryForObject(sql, String.class, idUsuario);
        if (!"COORDINADOR".equalsIgnoreCase(rol)) {
            throw new RuntimeException("Solo los coordinadores pueden registrar asistencia.");
        }
    }

    private void validarAsignacionConfirmada(int idAsignacion) {
        String sql = """
            SELECT COUNT(1) cont FROM Asignaciones
            WHERE id_asignacion = ? AND estado_asignacion = 'Confirmado'
        """;
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idAsignacion);
        if (cont == 0) {
            throw new RuntimeException("La asignación no existe o no está confirmada.");
        }
    }

    private void validarEventoFinalizado(int idAsignacion) {
        String sql = """
            SELECT e.fecha_inicio
            FROM Asignaciones a
            JOIN Eventos e ON e.id_evento = a.id_evento
            WHERE a.id_asignacion = ?
        """;
        Timestamp fechaEvento = jdbcTemplate.queryForObject(sql, Timestamp.class, idAsignacion);
        if (fechaEvento.toLocalDateTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("No se puede registrar asistencia antes del evento.");
        }
    }

    private void validarAsistenciaNoRegistrada(int idAsignacion) {
        String sql = "SELECT asistio FROM Asignaciones WHERE id_asignacion = ?";
        Boolean yaRegistrada = jdbcTemplate.queryForObject(sql, Boolean.class, idAsignacion);
        if (yaRegistrada != null) {
            throw new RuntimeException("La asistencia ya fue registrada para esta asignación.");
        }
    }

    private void validarHoras(boolean asistio, int horas) {
        if (asistio && horas <= 0) {
            throw new RuntimeException("Debe registrar al menos 1 hora si asistió.");
        }
        if (asistio && horas > 24) {
            throw new RuntimeException("No puedes registrar más de 24 horas para un solo evento.");
        }
        if (!asistio && horas > 0) {
            throw new RuntimeException("No se deben registrar horas si el voluntario no asistió.");
        }
    }

    private void validarEventoExiste(int idEvento) {
        String sql = "SELECT COUNT(1) FROM Eventos WHERE id_evento = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, idEvento);
        if (count == 0) {
            throw new RuntimeException("El evento no existe.");
        }
    }

    //Lista voluntarios pendientes de asistencia con estado de asignacion confirmado de un evento ya ocurrido
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Map<String,Object> listarPendientesDeAsistencia(int idEvento) {
        validarEventoExiste(idEvento);
        String sql = """
        SELECT a.id_asignacion, v.nombre AS voluntario, e.nombre AS evento
        FROM Asignaciones a
        JOIN Voluntarios v ON v.id_voluntario = a.id_voluntario
        JOIN Eventos e ON e.id_evento = a.id_evento
        WHERE a.estado_asignacion = 'Confirmado'
          AND a.asistio IS NULL
          AND e.fecha_inicio <= GETDATE()
          AND a.id_evento = ?
        ORDER BY v.nombre
    """;
        List<PendienteAsistenciaDto> pendientes = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(PendienteAsistenciaDto.class),
                idEvento
        );
        Map<String,Object> rec1 = new HashMap<>();
        rec1.put("mensaje", "Voluntarios pendientes de registrar asistencia");
        rec1.put("pendientes", pendientes);
        return rec1;
    }

}