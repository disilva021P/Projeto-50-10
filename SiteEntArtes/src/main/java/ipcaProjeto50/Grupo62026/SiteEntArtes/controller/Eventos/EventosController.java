package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Eventos;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.CriarEventosDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.EventoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.EventoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventosController
{
    private final EventoService eventoService;

    public EventosController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    // Qualquer utilizador autenticado pode ver eventos futuros
    @GetMapping
    public ResponseEntity<List<EventoDto>> listarEventosFuturos() {
        return ResponseEntity.ok(eventoService.findEventosFuturos());
}

    // Qualquer utilizador autenticado pode ver um evento específico
    @GetMapping("/{id}")
    public ResponseEntity<EventoDto> getEvento(@PathVariable String id) {
        try {
            return ResponseEntity.ok(eventoService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Só a coordenação pode criar eventos
    @PreAuthorize("hasAuthority('ROLE_COORDENACAO')")
    @PostMapping
    public ResponseEntity<EventoDto> criarEvento(
            @AuthenticationPrincipal String userId,
            @RequestBody CriarEventosDto dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(eventoService.criarEvento(userId, dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Só a coordenação pode editar eventos
    @PreAuthorize("hasAuthority('ROLE_COORDENACAO')")
    @PutMapping("/{id}")
    public ResponseEntity<EventoDto> editarEvento(
            @PathVariable String id,
            @RequestBody CriarEventosDto dto) {
        try {
            return ResponseEntity.ok(eventoService.update(id, dto));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Só a coordenação pode apagar eventos
    @PreAuthorize("hasAuthority('ROLE_COORDENACAO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> apagarEvento(@PathVariable String id) {
        try {
            eventoService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Adicionar participante a um evento (coordenação)
    @PreAuthorize("hasAuthority('ROLE_COORDENACAO')")
    @PostMapping("/{id}/participantes/{utilizadorId}")
    public ResponseEntity<Void> adicionarParticipante(
            @PathVariable String id,
            @PathVariable String utilizadorId) {
        try {
            eventoService.adicionarParticipante(id, utilizadorId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Remover participante de um evento (coordenação)
    @PreAuthorize("hasAuthority('ROLE_COORDENACAO')")
    @DeleteMapping("/{id}/participantes/{utilizadorId}")
    public ResponseEntity<Void> removerParticipante(
            @PathVariable String id,
            @PathVariable String utilizadorId) {
        try {
            eventoService.removerParticipante(id, utilizadorId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}