package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Teste;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.HorarioTurmaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.TurmaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.AulaFixaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
public class AulaFixaController {

    private final AulaFixaService aulaFixaService;

    // -------------------------------------------------------------------------
    // CRUD base
    // -------------------------------------------------------------------------

    /**
     * GET /api/horarios
     * Devolve todos os horários fixos com paginação.
     */
    @GetMapping
    public ResponseEntity<PagedModel<HorarioTurmaDto>> findAll(Pageable pageable) {
        return ResponseEntity.ok(aulaFixaService.findAll(pageable));
    }

    /**
     * GET /api/horarios/{id}
     * Devolve um horário fixo pelo ID hasheado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HorarioTurmaDto> findById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(aulaFixaService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/horarios
     * Cria um novo horário fixo.
     * Nota: Para gerar as aulas automaticamente usa POST /api/aulas/horario.
     */
    @PostMapping
    public ResponseEntity<HorarioTurmaDto> save(@RequestBody HorarioTurmaDto horario) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(aulaFixaService.convertToDto(aulaFixaService.save(horario)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/horarios/{id}
     * Atualiza um horário fixo existente (sem regenerar as aulas).
     * Para atualizar horário + aulas usa PUT /api/aulas/horario.
     */
    @PutMapping("/{id}")
    public ResponseEntity<HorarioTurmaDto> update(
            @PathVariable String id,
            @RequestBody HorarioTurmaDto horario) {
        try {
            return ResponseEntity.ok(
                    aulaFixaService.convertToDto(aulaFixaService.update(id, horario))
            );
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/horarios/{id}
     * Elimina apenas o horário fixo (sem eliminar as aulas).
     * Para eliminar horário + aulas usa DELETE /api/aulas/horario/{id}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        try {
            aulaFixaService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // -------------------------------------------------------------------------
    // Queries por turma
    // -------------------------------------------------------------------------

    /**
     * GET /api/horarios/turma/{idTurma}
     * Devolve todos os horários fixos de uma turma.
     */
    @GetMapping("/turma/{idTurma}")
    public ResponseEntity<List<HorarioTurmaDto>> findByTurma(@PathVariable String idTurma) {
        try {
            return ResponseEntity.ok(aulaFixaService.findByIdTurma(idTurma));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/horarios/turmas
     * Devolve os horários agrupados por turma, dado uma lista de IDs de turmas.
     * Body: ["hashedId1", "hashedId2", ...]
     */
    @PostMapping("/turmas")
    public ResponseEntity<Map<TurmaDto, List<HorarioTurmaDto>>> findHorariosPorTurmas(
            @RequestBody List<String> idsTurmas) {
        return ResponseEntity.ok(aulaFixaService.findHorariosPorTurmas(idsTurmas));
    }
}