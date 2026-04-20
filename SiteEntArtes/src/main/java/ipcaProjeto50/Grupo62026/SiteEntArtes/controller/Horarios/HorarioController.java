package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Horarios;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.AulaCoachingService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.AulaFixaService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.AulaService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.DisponibilidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.Objects;

@RestController
@RequestMapping("/horario")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HorarioController {

    private final AulaService aulaService;
    private final AulaCoachingService aulaCoachingService;
    private final DisponibilidadeService disponibilidadeService;
    private final AulaFixaService aulaFixaService;

    private String getUserId() {
        return (String) Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getPrincipal();
    }

    // =========================================================================
    // region ALUNO
    // =========================================================================

    @GetMapping("/semana")
    @PreAuthorize("hasAuthority('ALUNO')")
    public ResponseEntity<?> horarioSemanaAluno(@RequestParam(defaultValue = "0") int offset) {
        try {
            return ResponseEntity.ok(aulaService.buscarHorarioSemana(getUserId(), offset));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao buscar horário semanal: " + e.getMessage());
        }
    }

    @GetMapping("/coaching")
    @PreAuthorize("hasAuthority('ALUNO')")
    public ResponseEntity<?> coachingAluno(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(aulaCoachingService.findAllbyAlunoIdPage(getUserId(), pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao buscar coachings: " + e.getMessage());
        }
    }

    @PostMapping("/coaching")
    @PreAuthorize("hasAuthority('ALUNO')")
    public ResponseEntity<?> marcarCoachingAluno(@RequestBody AulaCoachingDto dto) {
        try {
            return ResponseEntity.ok(aulaCoachingService.salvar(dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao marcar coaching: " + e.getMessage());
        }
    }

    /**
     * O aluno valida a sua própria presença numa aula de coaching.
     * Delega para o service que confirma a presença e atualiza o estado.
     */
    @PutMapping("/coaching/{aulaId}/validar-presenca")
    @PreAuthorize("hasAuthority('ALUNO')")
    public ResponseEntity<?> validarPresencaAluno(@PathVariable String aulaId) {
        try {
            return ResponseEntity.ok(aulaCoachingService.validarPresenca(getUserId(), aulaId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro ao validar presença: " + e.getMessage());
        }
    }

    // endregion

    // =========================================================================
    // region ENCARREGADO
    // =========================================================================

    @GetMapping("/semana/educando/{educandoId}")
    @PreAuthorize("hasAuthority('ENCARREGADO')")
    public ResponseEntity<?> horarioSemanaEducando(@PathVariable String educandoId,
                                                   @RequestParam(defaultValue = "0") int offset) {
        try {
            return ResponseEntity.ok(aulaService.buscarHorarioSemana(educandoId, offset));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao buscar horário do educando: " + e.getMessage());
        }
    }

    @GetMapping("/coaching/educando/{educandoId}")
    @PreAuthorize("hasAuthority('ENCARREGADO')")
    public ResponseEntity<?> coachingEducando(@PathVariable String educandoId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(aulaCoachingService.findAllbyAlunoIdPage(educandoId, PageRequest.of(page, size)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao buscar coachings do educando: " + e.getMessage());
        }
    }

    @PostMapping("/coaching/educando/{educandoId}")
    @PreAuthorize("hasAuthority('ENCARREGADO')")
    public ResponseEntity<?> marcarCoachingEducando(@PathVariable String educandoId,
                                                    @RequestBody AulaCoachingDto dto) {
        try {
            return ResponseEntity.ok(aulaCoachingService.salvar(dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao marcar coaching para educando: " + e.getMessage());
        }
    }

    /**
     * O encarregado valida a presença do seu educando numa aula de coaching.
     */
    @PutMapping("/coaching/{aulaId}/validar-presenca/educando/{educandoId}")
    @PreAuthorize("hasAuthority('ENCARREGADO')")
    public ResponseEntity<?> validarPresencaEducando(@PathVariable String aulaId,
                                                     @PathVariable String educandoId) {
        try {
            return ResponseEntity.ok(aulaCoachingService.validarPresenca(educandoId, aulaId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro ao validar presença do educando: " + e.getMessage());
        }
    }

    // endregion

    // =========================================================================
    // region PROFESSOR
    // =========================================================================

    @GetMapping("/professor/semana")
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ResponseEntity<?> horarioSemanaProfessor(@RequestParam(defaultValue = "0") int offset) {
        try {
            return ResponseEntity.ok(aulaService.buscarHorarioSemana(getUserId(), offset));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao buscar horário do professor: " + e.getMessage());
        }
    }

    /**
     * Lista os coachings pendentes de confirmação pelo professor autenticado.
     */
    @GetMapping("/professor/coaching/pendentes")
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ResponseEntity<?> coachingPendentes(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(aulaCoachingService.findPendentesByProfessorId(getUserId(), pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao buscar coachings pendentes: " + e.getMessage());
        }
    }

    /**
     * Professor confirma que vai realizar o coaching — estado passa a CONFIRMADA.
     */
    @PutMapping("/professor/coaching/{aulaId}/confirmar")
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ResponseEntity<?> confirmarCoaching(@PathVariable String aulaId) {
        try {
            return ResponseEntity.ok(aulaCoachingService.confirmar(aulaId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro ao confirmar coaching: " + e.getMessage());
        }
    }

    /**
     * Professor valida que o coaching foi realizado — estado passa a REALIZADA.
     */
    @PutMapping("/professor/coaching/{aulaId}/validar")
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ResponseEntity<?> validarRealizacaoProfessor(@PathVariable String aulaId) {
        try {
            return ResponseEntity.ok(aulaCoachingService.validarRealizacao(aulaId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro ao validar realização: " + e.getMessage());
        }
    }

    // endregion

    // =========================================================================
    // region COORDENACAO
    // =========================================================================

    @GetMapping
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> listarHorarios(Pageable pageable) {
        try {
            return ResponseEntity.ok(aulaFixaService.findAll(pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao listar horários: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> buscarHorario(@PathVariable String id) {
        try {
            return ResponseEntity.ok(aulaFixaService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Horário não encontrado: " + e.getMessage());
        }
    }

    @PostMapping("/criar")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> criarHorario(@RequestBody HorarioTurmaRequestDto dto) {
        try {
            if (dto.idturma() == null || dto.estudioId() == null) {
                return ResponseEntity.badRequest().body("Os IDs da turma e do estúdio são obrigatórios.");
            }

            long duracaoCalculada = java.time.Duration.between(dto.horaInicio(), dto.horaFim()).toMinutes();
            if (duracaoCalculada <= 0) {
                return ResponseEntity.badRequest().body("A hora de fim deve ser posterior à hora de início.");
            }

            HorarioTurmaRequestDto dtoComAutor = new HorarioTurmaRequestDto(
                    dto.id(), getUserId(), dto.idturma(), dto.dataInicio(),
                    dto.dataValidade(), dto.diaSemana(), (int) duracaoCalculada,
                    dto.horaInicio(), dto.horaFim(), dto.estudioId()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(aulaService.GerarAulasComHorario(dtoComAutor));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar horário: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> atualizarHorario(@PathVariable String id,
                                              @RequestBody HorarioTurmaRequestDto dto) {
        try {
            if (id == null) throw new Exception("Id inválido");
            long duracaoCalculada = java.time.Duration.between(dto.horaInicio(), dto.horaFim()).toMinutes();
            if (duracaoCalculada <= 0) {
                return ResponseEntity.badRequest().body("A hora de fim deve ser posterior à hora de início.");
            }
            HorarioTurmaRequestDto dtoComAutor = new HorarioTurmaRequestDto(
                    dto.id(), getUserId(), dto.idturma(), dto.dataInicio(),
                    dto.dataValidade(), dto.diaSemana(), (int) duracaoCalculada,
                    dto.horaInicio(), dto.horaFim(), dto.estudioId()
            );
            return ResponseEntity.ok(aulaService.atualizaPorHorario(dtoComAutor, id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar horário: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> eliminarHorario(@PathVariable String id) {
        try {
            aulaService.EliminarAulasComHorario(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao eliminar horário: " + e.getMessage());
        }
    }

    /**
     * Coordenação lista todos os coachings (para supervisão).
     */
    @GetMapping("/coaching/todos")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> listarTodosCoachings(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(aulaCoachingService.findAll(pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao listar coachings: " + e.getMessage());
        }
    }

    /**
     * Coordenação valida a realização de um coaching — estado passa a REALIZADA.
     * Serve como segunda validação/supervisão após o professor.
     */
    @PutMapping("/coaching/{aulaId}/validar")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> validarRealizacaoCoordenacao(@PathVariable String aulaId) {
        try {
            return ResponseEntity.ok(aulaCoachingService.validarRealizacao(aulaId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro ao validar coaching pela coordenação: " + e.getMessage());
        }
    }

    // endregion
}