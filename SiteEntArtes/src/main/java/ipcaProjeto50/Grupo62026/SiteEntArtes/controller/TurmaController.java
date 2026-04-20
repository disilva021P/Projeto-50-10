package ipcaProjeto50.Grupo62026.SiteEntArtes.controller;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.TurmaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.TurmaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/turmas")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TurmaController {

    private final TurmaService turmaService;

    // --- LEITURA ---

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(turmaService.findAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao listar turmas: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(turmaService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // --- ESCRITA (COORDENACAO) ---

    @PostMapping
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> create(@RequestBody TurmaDto dto) {
        try {
            return ResponseEntity.ok(turmaService.create(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar turma: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody TurmaDto dto) {
        try {
            return ResponseEntity.ok(turmaService.update(id, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro ao atualizar turma: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            turmaService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro ao eliminar turma: " + e.getMessage());
        }
    }
}