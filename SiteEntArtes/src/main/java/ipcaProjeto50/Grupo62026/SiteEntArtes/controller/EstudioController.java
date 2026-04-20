package ipcaProjeto50.Grupo62026.SiteEntArtes.controller;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.EstudioDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.EstudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/estudios")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class EstudioController {

    private final EstudioService estudioService;

    // --- LEITURA ---

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(estudioService.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao listar estúdios: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(estudioService.findByIdDto(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // --- ESCRITA (COORDENACAO) ---

    @PostMapping
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> create(@RequestBody EstudioDto dto) {
        try {
            return ResponseEntity.ok(estudioService.create(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar estúdio: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody EstudioDto dto) {
        try {
            return ResponseEntity.ok(estudioService.update(id, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro ao atualizar: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            estudioService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro ao eliminar: " + e.getMessage());
        }
    }

    // --- RELAÇÕES ---

    @PostMapping("/{estudioId}/modalidade/{modalidadeId}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> adicionarModalidade(@PathVariable String estudioId, @PathVariable String modalidadeId) {
        try {
            return ResponseEntity.ok(estudioService.adicionarModalidade(estudioId, modalidadeId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao associar modalidade: " + e.getMessage());
        }
    }

    @DeleteMapping("/{estudioId}/modalidade/{modalidadeId}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> removerModalidade(@PathVariable String estudioId, @PathVariable String modalidadeId) {
        try {
            estudioService.removerModalidade(estudioId, modalidadeId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao remover associação: " + e.getMessage());
        }
    }
}