package com.manosamigas.app.service;
import com.manosamigas.app.dto.EventoDto;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventoService {
    private final JdbcTemplate jdbcTemplate;

    @Data
    private static class Evento { // Modelo de datos interno
        private Long id;
        private String nombre;
        private String descripcion;
        private LocalDateTime fechaInicio;
        private LocalDateTime fechaFin;
        private String ubicacion;
        private int cupoMaximo;
        private Long idInteresRequerido;
    }

    public EventoService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @Transactional
    public EventoDto crearEvento(EventoDto dto) {
        Evento evento = convertirDtoAEntidad(dto);
        String sql = "INSERT INTO Eventos (nombre, descripcion, fecha_inicio, fecha_fin, ubicacion, cupo_maximo, id_interes_requerido) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, evento.getNombre());
            ps.setString(2, evento.getDescripcion());
            ps.setObject(3, evento.getFechaInicio());
            ps.setObject(4, evento.getFechaFin());
            ps.setString(5, evento.getUbicacion());
            ps.setInt(6, evento.getCupoMaximo());
            ps.setObject(7, evento.getIdInteresRequerido());
            return ps;
        }, keyHolder);
        if(keyHolder.getKey() != null) evento.setId(keyHolder.getKey().longValue());
        return convertirEntidadADto(evento);
    }

    public List<EventoDto> listarTodos() {
        String sql = "SELECT * FROM Eventos";
        return jdbcTemplate.query(sql, new EventoRowMapper()).stream()
                .map(this::convertirEntidadADto)
                .collect(Collectors.toList());
    }

    private static class EventoRowMapper implements RowMapper<Evento> {
        @Override
        public Evento mapRow(ResultSet rs, int rowNum) throws SQLException {
            Evento evento = new Evento();
            evento.setId(rs.getLong("id_evento"));
            evento.setNombre(rs.getString("nombre"));
            evento.setDescripcion(rs.getString("descripcion"));
            evento.setFechaInicio(rs.getObject("fecha_inicio", LocalDateTime.class));
            evento.setFechaFin(rs.getObject("fecha_fin", LocalDateTime.class));
            evento.setUbicacion(rs.getString("ubicacion"));
            evento.setCupoMaximo(rs.getInt("cupo_maximo"));
            evento.setIdInteresRequerido(rs.getObject("id_interes_requerido", Long.class));
            return evento;
        }
    }

    private EventoDto convertirEntidadADto(Evento e) { return new EventoDto(e.getId(), e.getNombre(), e.getDescripcion(), e.getFechaInicio(), e.getFechaFin(), e.getUbicacion(), e.getCupoMaximo(), e.getIdInteresRequerido()); }
    private Evento convertirDtoAEntidad(EventoDto dto) { Evento e = new Evento(); e.setId(dto.getId()); e.setNombre(dto.getNombre()); e.setDescripcion(dto.getDescripcion()); e.setFechaInicio(dto.getFechaInicio()); e.setFechaFin(dto.getFechaFin()); e.setUbicacion(dto.getUbicacion()); e.setCupoMaximo(dto.getCupoMaximo()); e.setIdInteresRequerido(dto.getIdInteresRequerido()); return e; }
}