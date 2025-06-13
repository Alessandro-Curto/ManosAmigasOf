package com.manosamigas.app.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.manosamigas.app.dto.RegistroNotificacionDto;
import com.manosamigas.app.dto.ConsultaNotificacionDto;
import com.manosamigas.app.service.RegistroNotificacionService;

@RestController
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class RegistroNotificacionRest {

    @Autowired
    private RegistroNotificacionService registroNotificacionService;

    @PostMapping("/api/notificacion/registrar")
    public ResponseEntity<?> crearYEnviar(@RequestBody RegistroNotificacionDto dto) {
        System.out.println("DTO recibido: " + dto);

        try {
            ConsultaNotificacionDto resultado = registroNotificacionService.procesar(dto);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno al procesar la notificación.");
        }
    }
}
