package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Teste;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.HorarioTurmaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.AulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/aulas")
@RequiredArgsConstructor
public class AulaController {

    private final AulaService aulaService;

    // -------------------------------------------------------------------------
    // CRUD base
    // -------------------------------------------------------------------------

    /**
     * GET /api/aulas
     * Devolve todas as aulas com paginação.
     */
    @GetMapping
    public ResponseEntity<PagedModel<AulaDto>> findAll(Pageable pageable) {
        return ResponseEntity.ok(aulaService.findAll(pageable));
    }

    /**
     * GET /api/aulas/{id}
     * Devolve uma aula pelo ID hasheado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AulaDto> findById(@PathVariable String id) {
        return aulaService.buscarPorId(id)
                .map(aula -> ResponseEntity.ok(aulaService.converterParaDto(aula)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/aulas/{id}
     * Elimina uma aula pelo ID hasheado.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        aulaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Queries por data
    // -------------------------------------------------------------------------

    /**
     * GET /api/aulas/dia?data=YYYY-MM-DD
     * Devolve todas as aulas de um dia específico.
     * Se data não for fornecida, usa o dia de hoje.
     */
    @GetMapping("/dia")
    public ResponseEntity<List<AulaDto>> findByData(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(aulaService.findByDataAula(data));
    }

    // -------------------------------------------------------------------------
    // Horários do aluno
    // -------------------------------------------------------------------------

    /**
     * GET /api/aulas/aluno/{userId}/dia?data=YYYY-MM-DD
     * Devolve as aulas de um aluno num dia específico.
     */
    @GetMapping("/aluno/{userId}/dia")
    public ResponseEntity<List<AulaDto>> aulasAlunoPorData(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        try {
            return ResponseEntity.ok(aulaService.buscarAulaporId_Data(data, userId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/aulas/aluno/{userId}/semana?offset=0
     * Devolve o horário semanal de um aluno.
     * offset: 0 = semana atual, 1 = semana seguinte, etc.
     */
    @GetMapping("/aluno/{userId}/semana")
    public ResponseEntity<List<AulaDto>> horarioSemanaAluno(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int offset) {
        try {
            return ResponseEntity.ok(aulaService.buscarHorarioSemana(userId, offset));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/aulas/aluno/{userId}/{aulaId}
     * Devolve uma aula específica verificando se o aluno está inscrito.
     */
    @GetMapping("/aluno/{userId}/{aulaId}")
    public ResponseEntity<AulaDto> aulaDoAluno(
            @PathVariable String userId,
            @PathVariable String aulaId) {
        try {
            return ResponseEntity.ok(aulaService.buscarAulaAluno(userId, aulaId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // -------------------------------------------------------------------------
    // Horários com geração automática
    // -------------------------------------------------------------------------

    /**
     * POST /api/aulas/horario
     * Gera aulas automaticamente a partir de um horário de turma.
     * Devolve a lista de datas com erro (aulas que não puderam ser criadas).
     */
    @PostMapping("/horario")
    public ResponseEntity<List<AulaDto>> gerarAulasComHorario(
            @RequestBody HorarioTurmaDto horario) {
        try {
            List<AulaDto> erros = aulaService.GerarAulasComHorario(horario);
            if (erros.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(erros);
            }
            // 207 Multi-Status: algumas aulas falharam mas outras foram criadas
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(erros);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/aulas/horario
     * Atualiza as aulas geradas por um horário de turma.
     * Se o dia da semana mudou, elimina e recria todas as aulas.
     */
    @PutMapping("/horario")
    public ResponseEntity<List<AulaDto>> atualizarAulasComHorario(
            @RequestBody HorarioTurmaDto horario) {
        try {
            return ResponseEntity.ok(aulaService.atualizaPorHorario(horario));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DELETE /api/aulas/horario/{idHorario}
     * Elimina um horário e todas as aulas associadas.
     */
    @DeleteMapping("/horario/{idHorario}")
    public ResponseEntity<Void> eliminarAulasComHorario(@PathVariable String idHorario) {
        try {
            aulaService.EliminarAulasComHorario(idHorario);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // -------------------------------------------------------------------------
    // Encarregado de educação
    // -------------------------------------------------------------------------

    /**
     * GET /api/aulas/encarregado/{id}/educandos
     * Devolve a lista de educandos de um encarregado.
     */
    @GetMapping("/encarregado/{id}/educandos")
    public ResponseEntity<List<UtilizadoreResumoDto>> educandosDeEncarregado(
            @PathVariable String id) {
        try {
            // O método interno usa Integer; descodificamos via service
            // Reutilizamos o findEducandosdeEducador que recebe Integer
            // mas como não temos acesso ao idHasher aqui, criamos um endpoint
            // que delega tudo ao service através do método devolveAulasEducandos
            // Para os educandos isolados precisamos de expor a lista diretamente.
            // Nota: Se necessário expor o idHasher ao controller, injetar aqui.
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/aulas/encarregado/{id}/semana?offset=0
     * Devolve as aulas de todos os educandos de um encarregado numa semana.
     */
    @GetMapping("/encarregado/{id}/semana")
    public ResponseEntity<List<AulaDto>> aulasEducandosSemana(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int offset) {
        try {
            return ResponseEntity.ok(aulaService.devolveAulasEducandos(id, offset));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
