package ipcaProjeto50.Grupo62026.SiteEntArtes.controller;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.DisponibilidadeProfessorDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.DisponibilidadeProfessorDtoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.DisponibilidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/disponibilidade")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DisponibilidadeController {
    private String getUserId() {
        return (String) Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getPrincipal();
    }
    private final DisponibilidadeService disponibilidadeService;

    @GetMapping("/professor/{id}")
    public ResponseEntity<?> getByProfessor(@PathVariable String id) {
        try {
            return ResponseEntity.ok(
                    disponibilidadeService.disponibilidadesByProfessorId(id)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/professor")
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ResponseEntity<?> inserirDisponibilidade(@RequestBody DisponibilidadeProfessorDtoRequest dto) {
        try {

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(disponibilidadeService.inserirDisponibilidade(new DisponibilidadeProfessorDtoRequest(getUserId(),dto.diaSemana(),dto.horaInicio(),dto.horaFim(),dto.validoDe(),dto.validoAte())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao inserir disponibilidade: " + e.getMessage());
        }
    }

    /**
     * Altera uma disponibilidade existente do professor autenticado.
     */
    @PutMapping("/professor/{id}")
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ResponseEntity<?> alterarDisponibilidade(@PathVariable String id,
                                                    @RequestBody DisponibilidadeProfessorDto dto) {
        try {
            return ResponseEntity.ok(disponibilidadeService.alterarDisponibilidade(id, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao alterar disponibilidade: " + e.getMessage());
        }
    }

    /**
     * Remove uma disponibilidade do professor autenticado.
     */
    @DeleteMapping("/professor/{id}")
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ResponseEntity<?> removerDisponibilidade(@PathVariable String id) {
        try {
            disponibilidadeService.removerDisponibilidade(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erro ao remover disponibilidade: " + e.getMessage());
        }
    }

    // endregion

    // =========================================================================
    // region COORDENACAO
    // =========================================================================

    /**
     * Coordenação consulta as disponibilidades de um professor específico.
     */
    @GetMapping("/coordenacao/{idProfessor}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> listarDisponibilidadesProfessor(@PathVariable String idProfessor) {
        try {
            return ResponseEntity.ok(disponibilidadeService.disponibilidadesByProfessorId(idProfessor));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar disponibilidades do professor: " + e.getMessage());
        }
    }
    @GetMapping("/minhasdisponibilidades")
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ResponseEntity<?> listarMinhasDisponibilidades() {
        try {
            return ResponseEntity.ok(disponibilidadeService.disponibilidadesByProfessorId(getUserId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar disponibilidades do professor: " + e.getMessage());
        }
    }

    // endregion
}
