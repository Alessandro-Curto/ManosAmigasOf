package com.manosamigas.app.service;

import com.manosamigas.app.dto.DisponibilidadDTO;
import com.manosamigas.app.dto.VoluntarioRequest;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Service
public class VoluntarioService {

    private final JdbcTemplate jdbcTemplate;

    @Data
    private static class Voluntario { // Modelo de datos interno
        private Long id;
        private String nombre;
        private String email;
        private String telefono;
    }

    public VoluntarioService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Voluntario registrarVoluntario(VoluntarioRequest request) {
        String emailCheckSql = "SELECT COUNT(*) FROM Voluntarios WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(emailCheckSql, Integer.class, request.getEmail());
        if (count != null && count > 0) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }

        Voluntario voluntario = new Voluntario();
        voluntario.setNombre(request.getNombre());
        voluntario.setEmail(request.getEmail());
        voluntario.setTelefono(request.getTelefono());

        String sql = "INSERT INTO Voluntarios (nombre, email, telefono) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, voluntario.getNombre());
            ps.setString(2, voluntario.getEmail());
            ps.setString(3, voluntario.getTelefono());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            voluntario.setId(keyHolder.getKey().longValue());
        } else {
            throw new RuntimeException("Fallo al crear voluntario, no se obtuvo ID.");
        }

        if (request.getIdsInteres() != null && !request.getIdsInteres().isEmpty()) {
            String interesSql = "INSERT INTO VoluntarioIntereses (id_voluntario, id_interes) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(interesSql, request.getIdsInteres(), request.getIdsInteres().size(),
                    (ps, idInteres) -> {
                        ps.setLong(1, voluntario.getId());
                        ps.setLong(2, idInteres);
                    });
        }
        if (request.getDisponibilidad() != null && !request.getDisponibilidad().isEmpty()) {
            String dispoSql = "INSERT INTO VoluntarioDisponibilidad (id_voluntario, id_dia, turno) VALUES (?, ?, ?)";
            jdbcTemplate.batchUpdate(dispoSql, request.getDisponibilidad(), request.getDisponibilidad().size(),
                    (ps, d) -> {
                        ps.setLong(1, voluntario.getId());
                        ps.setLong(2, d.getIdDia());
                        ps.setString(3, d.getTurno());
                    });
        }
        return voluntario;
    }
}