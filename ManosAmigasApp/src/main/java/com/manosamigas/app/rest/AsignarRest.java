package com.manosamigas.app.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.manosamigas.app.dto.AsignarDto;
import com.manosamigas.app.service.AsignarService;

@RestController
@RequestMapping("/api/asignaciones")
public class AsignarRest {

    @Autowired
    private AsignarService asignarService;

    @PostMapping("/asignar")
    public ResponseEntity<?> asignar(@RequestBody AsignarDto bean) {
        try {
            AsignarDto result = asignarService.asignar(bean);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarInscripcion(@RequestBody AsignarDto bean) {
        try {
            String mensaje = asignarService.confirmarAsignacion(bean);
            return ResponseEntity.ok(mensaje); // Devuelve solo el texto
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}