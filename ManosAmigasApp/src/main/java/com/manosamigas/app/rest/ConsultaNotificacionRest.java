package com.manosamigas.app.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.manosamigas.app.dto.ConsultaNotificacionDto;
import com.manosamigas.app.service.ConsultaNotificacionService;

@RestController
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class ConsultaNotificacionRest {

    @Autowired
    private ConsultaNotificacionService consultaNotificacionService;

    @GetMapping("/api/notificacion/consulta/{id}")
    public ResponseEntity<?> consultar(@PathVariable int id) {
        try {
            ConsultaNotificacionDto dto = consultaNotificacionService.consultarNotificacion(id);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Notificación no encontrada: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al consultar la notificación.");
        }
    }
}

