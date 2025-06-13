package com.manosamigas.app.rest;
import com.manosamigas.app.dto.EventoDto;
import com.manosamigas.app.service.EventoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {
    private final EventoService eventoService;
    public EventoController(EventoService eventoService) { this.eventoService = eventoService; }

    @PostMapping
    public ResponseEntity<EventoDto> crearEvento(@RequestBody EventoDto eventoDto) {
        return ResponseEntity.ok(eventoService.crearEvento(eventoDto));
    }
    @GetMapping
    public ResponseEntity<List<EventoDto>> listarEventos() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }
}