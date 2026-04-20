package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Horarios;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.AulaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@PreAuthorize("hasAuthority('PROFESSOR')")
@RestController
@RequestMapping("/api/professor/horarios")
public class HorariosProfessorController {

    private final AulaService aulaService;

    public HorariosProfessorController(AulaService aulaService) {
        this.aulaService = aulaService;
    }

    /*
     * Página principal — devolve aulas da semana atual ou seguinte
     */
    @GetMapping
    public ResponseEntity<List<AulaDto>> buscarHorarioSemanal(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) Integer offset) throws Exception {

        if (offset == null) offset = 0;
        if (offset != 0 && offset != 1) return ResponseEntity.badRequest().build();

        return ResponseEntity.ok(aulaService.buscarHorarioSemana(userId, offset));
    }

    /*
     * Devolve uma aula específica com a lista de alunos inscritos
     */
    @GetMapping("/{id}")
    public ResponseEntity<AulaDto> buscarAula(
            @AuthenticationPrincipal String userId,
            @PathVariable String id) {
        // TODO: implementar busca de aula por ID com lista de alunos
        return ResponseEntity.ok().build();
    }

    /*
     * Envia/atualiza disponibilidade do professor
     */
    @PostMapping("/disponibilidades")
    public ResponseEntity<Void> definirDisponibilidade(
            @AuthenticationPrincipal String userId,
            @RequestBody Object disponibilidadeDto) {
        // TODO: implementar criação de disponibilidade
        return ResponseEntity.ok().build();
    }

    /*
     * Remove uma disponibilidade do professor
     */
    @DeleteMapping("/disponibilidades/{id}")
    public ResponseEntity<Void> removerDisponibilidade(
            @AuthenticationPrincipal String userId,
            @PathVariable String id) {
        // TODO: verificar se existem aulas marcadas antes de remover
        return ResponseEntity.ok().build();
    }

    /*
     * Cancela uma aula com justificação
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarAula(
            @AuthenticationPrincipal String userId,
            @PathVariable String id,
            @RequestParam String motivo) {
        // TODO: implementar cancelamento de aula pelo professor
        return ResponseEntity.ok().build();
    }

    /*
     * Confirma a realização de uma aula (inicia processo de validação)
     */
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<Void> confirmarAula(
            @AuthenticationPrincipal String userId,
            @PathVariable String id) {
        // TODO: implementar confirmação — notifica encarregado/aluno
        return ResponseEntity.ok().build();
    }

    /*
     * Regista falta de um aluno numa aula
     */
    @PostMapping("/{id}/faltas")
    public ResponseEntity<Void> registarFalta(
            @AuthenticationPrincipal String userId,
            @PathVariable String id,
            @RequestParam String alunoId,
            @RequestParam(required = false) String motivo) {
        // TODO: implementar registo de falta — distinguir falta aluno vs professor
        return ResponseEntity.ok().build();
    }

    /*
     * Submete justificação de falta própria (falta do professor)
     */
    @PostMapping("/{id}/justificar")
    public ResponseEntity<Void> justificarFalta(
            @AuthenticationPrincipal String userId,
            @PathVariable String id,
            @RequestParam String justificacao) {
        // TODO: notificar coordenação
        return ResponseEntity.ok().build();
    }
}