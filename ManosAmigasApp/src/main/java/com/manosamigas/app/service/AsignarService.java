package com.manosamigas.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.manosamigas.app.dto.AsignarDto;

//REQUERIMIENTO 3

@Service
public class AsignarService {
    @Autowired
    private JdbcTemplate jdbc;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public AsignarDto asignar(AsignarDto bean) {
        int idUsuario = bean.getIdUsuario();
        int idVoluntario = bean.getIdVoluntario();
        int idEvento = bean.getIdEvento();

        // Obtener rol
        String rol = obtenerRolUsuario(idUsuario);
        //Obtener estado de asignacion (Confirmado o Pendiente)
        String estadoAsignacion;
        //Si es coordinador
        switch (rol.toUpperCase()) {
            case "COORDINADOR" -> estadoAsignacion = "Confirmado";
            case "VOLUNTARIO" -> {
                // Validar si el voluntario está vinculado al usuario actual
                int idVoluntarioRelacionado = obtenerVoluntarioDesdeUsuario(idUsuario);
                if (idVoluntarioRelacionado != idVoluntario)
                    throw new RuntimeException("Un voluntario solo puede inscribirse a sí mismo.");
                if (!estaInvitado(idVoluntario, idEvento))
                    throw new RuntimeException("No estás invitado a este evento.");
                estadoAsignacion = "Pendiente";
            }
            default -> throw new RuntimeException("Rol no autorizado para asignar.");
        }
        //Validaciones
        validarEvento(idEvento);
        validarVoluntario(idVoluntario);
        validarAsignacion(idEvento, idVoluntario);
        validarInteres(idEvento, idVoluntario);
        validarDisponibilidad(idEvento, idVoluntario);
        String sql = """
            INSERT INTO Asignaciones (id_voluntario, id_evento, fecha_asignacion, estado_asignacion)
            VALUES (?, ?, GETDATE(), ?)
        """;
        jdbc.update(sql, idVoluntario, idEvento, estadoAsignacion);
        bean.setMensaje("Asignado correctamente.");
        return bean;
    }
    //Para verificar si es COORDINADOR o VOLUNTARIO
    private String obtenerRolUsuario(int idUsuario) {
        String sql = """
            SELECT r.nombre FROM Usuarios u
            JOIN Roles r ON r.id_rol = u.id_rol
            WHERE u.id_usuario = ?
        """;
        try {
            return jdbc.queryForObject(sql, String.class, idUsuario);
        } catch (Exception e) {
            throw new RuntimeException("Usuario no válido o sin rol asignado.");
        }
    }
    //Obtener el id_voluntario para el usuario
    private int obtenerVoluntarioDesdeUsuario(int idUsuario) {
        String sql = "SELECT id_voluntario FROM Usuarios WHERE id_usuario = ?";
        try {
            return jdbc.queryForObject(sql, Integer.class, idUsuario);
        } catch (Exception e) {
            throw new RuntimeException("El usuario no está vinculado a un voluntario válido.");
        }
    }
    //Se supone que un voluntario está invitado cuando el interes coincide con el int. requerido del evento
    private boolean estaInvitado(int idVoluntario, int idEvento) {
        String sql = """
            SELECT COUNT(1)
            FROM Eventos e
            JOIN VoluntarioIntereses vi ON vi.id_interes = e.id_interes_requerido
            WHERE e.id_evento = ? AND vi.id_voluntario = ?
        """;
        int count = jdbc.queryForObject(sql, Integer.class, idEvento, idVoluntario);
        return count>0;
    }

    private void validarEvento(int idEvento) {
        // 1. Verificar que el evento exista
        String sql = "SELECT COUNT(1) cont FROM Eventos WHERE id_evento = ?";
        if (jdbc.queryForObject(sql, Integer.class, idEvento) == 0)
            throw new RuntimeException("El evento no existe.");

        // 2. Verificar que el evento no haya ocurrido (fecha_inicio > NOW)
        sql = "SELECT fecha_inicio FROM Eventos WHERE id_evento = ?";
        java.sql.Timestamp fechaInicio = jdbc.queryForObject(sql, java.sql.Timestamp.class, idEvento);
        if (fechaInicio.toLocalDateTime().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("No se puede asignar a un evento que ya ocurrió.");
        }

        // 3. Verificar que aún hay cupos disponibles, tomando en cuenta solo las confirmadas
        sql = """
        SELECT e.cupo_maximo - COUNT(a.id_asignacion)
        FROM Eventos e\s
        LEFT JOIN Asignaciones a ON e.id_evento = a.id_evento AND a.estado_asignacion = 'Confirmado'
        WHERE e.id_evento = ?
        GROUP BY e.cupo_maximo
    """;
        int cupo = jdbc.queryForObject(sql, Integer.class, idEvento);
        if (cupo <= 0)
            throw new RuntimeException("No hay cupos disponibles para este evento.");
    }

    private void validarVoluntario(int idVoluntario) {
        String sql = "SELECT COUNT(1) FROM Voluntarios WHERE id_voluntario = ?";
        if (jdbc.queryForObject(sql, Integer.class, idVoluntario) == 0)
            throw new RuntimeException("El voluntario no existe.");
    }

    private void validarAsignacion(int idEvento, int idVoluntario) {
        String sql = "SELECT COUNT(1) cont FROM Asignaciones WHERE id_evento = ? AND id_voluntario = ?";
        if (jdbc.queryForObject(sql, Integer.class, idEvento, idVoluntario) > 0)
            throw new RuntimeException("El voluntario ya está asignado o inscrito.");
    }

    private void validarInteres(int idEvento, int idVoluntario) {
        String sql = "SELECT id_interes_requerido FROM Eventos WHERE id_evento = ?";
        Integer interes = jdbc.queryForObject(sql, Integer.class, idEvento);
        if (interes != null) {
            sql = "SELECT COUNT(1) cont FROM VoluntarioIntereses WHERE id_voluntario = ? AND id_interes = ?";
            int cont = jdbc.queryForObject(sql, Integer.class, idVoluntario, interes);
            if (cont == 0)
                throw new RuntimeException("El voluntario no tiene el interés requerido.");
        }
    }

    private void validarDisponibilidad(int idEvento, int idVoluntario) {
        String sql = "SELECT DATEPART(WEEKDAY, fecha_inicio) FROM Eventos WHERE id_evento = ?";
        int dia = jdbc.queryForObject(sql, Integer.class, idEvento);

        sql = "SELECT COUNT(1) cont FROM VoluntarioDisponibilidad WHERE id_voluntario = ? AND id_dia = ?";
        if (jdbc.queryForObject(sql, Integer.class, idVoluntario, dia) == 0)
            throw new RuntimeException("El voluntario no está disponible en la fecha del evento.");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String confirmarAsignacion(AsignarDto bean) {

        int idUsuario = bean.getIdUsuario();
        int idVoluntario = bean.getIdVoluntario();
        int idEvento = bean.getIdEvento();

        // 1. Verificar que el usuario que confirma sea coordinador
        String rol = obtenerRolUsuario(idUsuario);
        if (!"COORDINADOR".equalsIgnoreCase(rol)) {
            throw new RuntimeException("Solo los coordinadores pueden confirmar inscripciones.");
        }
        // 2. Verificar que exista una asignación pendiente
        String sql = """
        SELECT COUNT(1) cont FROM Asignaciones
        WHERE id_voluntario = ? AND id_evento = ? AND estado_asignacion = 'Pendiente'
    """;
        int cont = jdbc.queryForObject(sql, Integer.class, idVoluntario, idEvento);
        if (cont == 0) {
            throw new RuntimeException("No hay ninguna inscripción pendiente para confirmar.");
        }
        // 3. Confirmar la asignación
        sql = """
        UPDATE Asignaciones
        SET estado_asignacion = 'Confirmado'
        WHERE id_voluntario = ? AND id_evento = ?
    """;
        jdbc.update(sql, idVoluntario, idEvento);
        // 4. Verificar si se llenaron los cupos luego de esta confirmación
        sql = """
        SELECT e.cupo_maximo - COUNT(a.id_asignacion)
        FROM Eventos e
        LEFT JOIN Asignaciones a ON e.id_evento = a.id_evento AND a.estado_asignacion = 'Confirmado'
        WHERE e.id_evento = ?
        GROUP BY e.cupo_maximo
        """;
        int cupoRestante = jdbc.queryForObject(sql, Integer.class, idEvento);
        if (cupoRestante <= 0) {
            // 5. Rechazar las asignaciones pendientes restantes
            sql = """
            UPDATE Asignaciones
            SET estado_asignacion = 'Rechazado'
            WHERE id_evento = ? AND estado_asignacion = 'Pendiente'
            """;
            jdbc.update(sql, idEvento);
        }
        return "Inscripción confirmada correctamente.";
    }
}