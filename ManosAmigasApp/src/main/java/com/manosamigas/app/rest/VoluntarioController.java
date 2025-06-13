package com.manosamigas.app.rest;
import com.manosamigas.app.dto.VoluntarioRequest;
import com.manosamigas.app.service.VoluntarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/voluntarios")
public class VoluntarioController {
    private final VoluntarioService voluntarioService;
    public VoluntarioController(VoluntarioService voluntarioService) { this.voluntarioService = voluntarioService; }
    @PostMapping
    public ResponseEntity<?> registrarVoluntario(@RequestBody VoluntarioRequest request) {
        try {
            voluntarioService.registrarVoluntario(request);
            return ResponseEntity.ok().body("{\"message\": \"Voluntario registrado exitosamente.\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}