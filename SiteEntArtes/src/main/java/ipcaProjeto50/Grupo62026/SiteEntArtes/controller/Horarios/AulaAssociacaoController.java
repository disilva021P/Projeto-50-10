package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Horarios;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaAlunoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaProfessorDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.AulaAlunoService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.AulaProfessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aulas")
@RequiredArgsConstructor
public class AulaAssociacaoController {

    private final AulaAlunoService aulaAlunoService;
    private final AulaProfessorService aulaProfessorService;

    // =========================================================================
    // region AULA-ALUNO
    // =========================================================================

    @PostMapping("/{idAula}/alunos/{idAluno}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> adicionarAluno(
            @PathVariable String idAula,
            @PathVariable String idAluno) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(aulaAlunoService.save(new AulaAlunoDto(idAluno, idAula)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{idAula}/alunos")
    @PreAuthorize("hasAnyAuthority('COORDENACAO', 'PROFESSOR')")
    public ResponseEntity<?> listarAlunosDaAula(@PathVariable String idAula) {
        try {
            return ResponseEntity.ok(aulaAlunoService.findByAulaId(idAula));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/alunos/{idAluno}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> listarAulasdoAluno(@PathVariable String idAluno) {
        try {
            return ResponseEntity.ok(aulaAlunoService.findByAlunoId(idAluno));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{idAula}/alunos/{idAluno}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> removerAluno(
            @PathVariable String idAula,
            @PathVariable String idAluno) {
        try {
            aulaAlunoService.deleteById(idAluno, idAula);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =========================================================================
    // region AULA-PROFESSOR
    // =========================================================================

    @PostMapping("/{idAula}/professores/{idProfessor}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> adicionarProfessor(
            @PathVariable String idAula,
            @PathVariable String idProfessor) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(aulaProfessorService.save(new AulaProfessorDto(idProfessor, idAula)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{idAula}/professores")
    @PreAuthorize("hasAnyAuthority('COORDENACAO', 'PROFESSOR')")
    public ResponseEntity<?> listarProfessoresDaAula(@PathVariable String idAula) {
        try {
            return ResponseEntity.ok(aulaProfessorService.findByAulaId(idAula));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/professores/{idProfessor}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> listarAulasDoProfessor(@PathVariable String idProfessor) {
        try {
            return ResponseEntity.ok(aulaProfessorService.findByProfessorId(idProfessor));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{idAula}/professores/{idProfessor}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> removerProfessor(
            @PathVariable String idAula,
            @PathVariable String idProfessor) {
        try {
            aulaProfessorService.deleteById(idProfessor, idAula);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
