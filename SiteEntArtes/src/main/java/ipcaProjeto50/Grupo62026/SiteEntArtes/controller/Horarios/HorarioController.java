package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Horarios;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.Utils;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/horario")
@RequiredArgsConstructor
public class HorarioController {

    private final AulaService aulaService;
    private final AulaCoachingService aulaCoachingService;
    private final DisponibilidadeService disponibilidadeService;
    private final AulaFixaService aulaFixaService;
    private final UtilizadorService utilizadorService;

    private String getUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        }

        return principal.toString();
    }

    // =========================================================================
    // region ALUNO
    // =========================================================================

    @GetMapping("/semana")
    @PreAuthorize("hasAuthority('ALUNO')")
    public ResponseEntity<?> horarioSemanaAluno(@RequestParam(name = "offset", defaultValue = "0") int offset) {
        try {
            return ResponseEntity.ok(aulaService.buscarHorarioSemana(getUserId(), offset));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao buscar horário semanal: " + e.getMessage());
        }
    }

    @GetMapping("/coaching")
    @PreAuthorize("hasAuthority('ALUNO')")
    public ResponseEntity<?> coachingAluno(
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(aulaCoachingService.findAllbyAlunoIdPage(getUserId(), pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar coachings: " + e.getMessage());
        }
    }

    @PostMapping("/marcarcoaching")
    @PreAuthorize("hasAuthority('ALUNO')")
    public ResponseEntity<?> marcarCoachingAluno(@RequestBody AulaCoachingRequestDto dto) {
        try {
            return ResponseEntity.ok(aulaCoachingService.salvarMarcarCoaching(dto, getUserId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao marcar coaching: " + e.getMessage());
        }
    }

    @GetMapping("/coachingsdisponiveis")
    @PreAuthorize("hasAuthority('ALUNO')")
    public ResponseEntity<? > getAulasDisponiveis(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "modalidade", required = false) String modalidade,
            @PageableDefault(size = 10, sort = "dataAula") Pageable pageable) {
        try {
            Page<AulaCoachingDto> aulas = aulaCoachingService.findAllPorAlunoIDModalidadePage(
                    getUserId(), modalidade, offset, pageable);
            return ResponseEntity.ok(aulas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/inscreverEmCoaching/{id}")
    @PreAuthorize("hasAuthority('ALUNO')")
    public ResponseEntity<?> inscreverCoachingAluno(@PathVariable("id") String aulaId) {
        try {
            return ResponseEntity.ok(aulaCoachingService.inscrever(getUserId(), aulaId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao inscrever no coaching: " + e.getMessage());
        }
    }

    @DeleteMapping("/cancelarCoaching/{aulaId}")
    @PreAuthorize("hasAuthority('ALUNO')")
    public ResponseEntity<?> cancelarCoachingAluno(@PathVariable(name = "aulaId") String aulaId) {
        try {
            aulaCoachingService.cancelarInscricao(getUserId(), aulaId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao cancelar inscrição no coaching: " + e.getMessage());
        }
    }
    

    // endregion

    // =========================================================================
    // region ENCARREGADO
    // =========================================================================

    @GetMapping("/semana/educando/{educandoId}")
    @PreAuthorize("hasAuthority('ENCARREGADO')")
    public ResponseEntity<?> horarioSemanaEducando(
            @PathVariable(name = "educandoId") String educandoId,
            @RequestParam(name = "offset", defaultValue = "0") int offset) {
        try {
            return ResponseEntity.ok(aulaService.buscarHorarioSemana(educandoId, offset));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar horário do educando: " + e.getMessage());
        }
    }

    @GetMapping("/coaching/educando/{educandoId}")
    @PreAuthorize("hasAuthority('ENCARREGADO')")
    public ResponseEntity<?> coachingEducando(
            @PathVariable(name = "educandoId") String educandoId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
                utilizadorService.verificaPermissaoEducando(educandoId, getUserId());
            return ResponseEntity.ok(aulaCoachingService.findAllbyAlunoIdPage(educandoId, PageRequest.of(page, size)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar coachings do educando: " + e.getMessage());
        }
    }

    @GetMapping("/coachingsdisponiveis/educando/{educandoId}")
    @PreAuthorize("hasAuthority('ENCARREGADO')")
    public ResponseEntity<?> coachingsDisponiveisEducando(
            @PathVariable(name = "educandoId") String educandoId,
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "modalidade", required = false) String modalidade,
            @PageableDefault(size = 10, sort = "dataAula") Pageable pageable) {
        try {
            utilizadorService.verificaPermissaoEducando(educandoId, getUserId());
            return ResponseEntity.ok(aulaCoachingService.findAllPorAlunoIDModalidadePage(educandoId, modalidade, offset, pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao buscar coachings disponíveis: " + e.getMessage());
        }
    }

    @PostMapping("/marcarcoaching/educando/{educandoId}")
    @PreAuthorize("hasAuthority('ENCARREGADO')")
    public ResponseEntity<?> marcarCoachingEducando(
            @PathVariable(name = "educandoId") String educandoId,
            @RequestBody AulaCoachingRequestDto dto) {
        try {
            utilizadorService.verificaPermissaoEducando(educandoId, getUserId());
            return ResponseEntity.ok(aulaCoachingService.salvarMarcarCoaching(dto, educandoId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao marcar coaching para educando: " + e.getMessage());
        }
    }

    @PostMapping("/inscreverEmCoaching/{aulaId}/educando/{educandoId}")
    @PreAuthorize("hasAuthority('ENCARREGADO')")
    public ResponseEntity<?> inscreverCoachingEducando(
            @PathVariable(name = "educandoId") String educandoId,
            @PathVariable(name="aulaId") String aulaId) {
        try {
            utilizadorService.verificaPermissaoEducando(educandoId, getUserId());
            return ResponseEntity.ok(aulaCoachingService.inscrever(educandoId, aulaId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao inscrever educando no coaching: " + e.getMessage());
        }
    }

    @DeleteMapping("/cancelarCoaching/{aulaId}/educando/{educandoId}")
    @PreAuthorize("hasAuthority('ENCARREGADO')")
    public ResponseEntity<?> cancelarCoachingEducando(
            @PathVariable(name = "aulaId") String aulaId,
            @PathVariable(name = "educandoId") String educandoId) {
        try {
            utilizadorService.verificaPermissaoEducando(educandoId, getUserId());
            aulaCoachingService.cancelarInscricao(educandoId, aulaId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao cancelar inscrição do educando: " + e.getMessage());
        }
    }

    @PutMapping("/coaching/{aulaId}/validar-presenca/educando/{educandoId}")
    @PreAuthorize("hasAuthority('ENCARREGADO')")
    public ResponseEntity<?> validarPresencaEducando(
            @PathVariable(name = "aulaId") String aulaId,
            @PathVariable(name = "educandoId") String educandoId) {
        try {
            utilizadorService.verificaPermissaoEducando(educandoId, getUserId());
            return ResponseEntity.ok(aulaCoachingService.validarPresenca(educandoId, aulaId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erro ao validar presença do educando: " + e.getMessage());
        }
    }

    // endregion

    // =========================================================================
    // region PROFESSOR
    // =========================================================================
    @GetMapping("/professor/horario")
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public List<AulaDto> getHorarioProfessor(
            @RequestParam(name = "offset", defaultValue = "0") int offset
    ) throws Exception {
        return aulaService.buscarAulasProfessorSemana(getUserId(), offset);
    }

    @GetMapping("/professor/coaching/pendentes")
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ResponseEntity<?> coachingPendentes(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(aulaCoachingService.findPendentesByProfessorId(getUserId(), pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao buscar coachings pendentes: " + e.getMessage());
        }
    }

    @PutMapping("/professor/coaching/{aulaId}/confirmar")
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ResponseEntity<?> confirmarCoaching(@PathVariable(name = "aulaId") String aulaId) {
        try {
            return ResponseEntity.ok(aulaCoachingService.confirmar(aulaId, Utils.getAuthenticatedUserId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro ao confirmar coaching: " + e.getMessage());
        }
    }

    @PutMapping("/professor/coaching/{aulaId}/validar")
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ResponseEntity<?> validarRealizacaoProfessor(@PathVariable(name = "aulaId") String aulaId) {
        try {
            return ResponseEntity.ok(aulaService.validarRealizacao(aulaId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro ao validar realização: " + e.getMessage());
        }
    }

    @PutMapping("/professor/coaching/rejeitar/{id}")
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ResponseEntity<?> rejeitarCoaching(@PathVariable(name = "id") String id){
        try {
            aulaCoachingService.professorRejeitaCoaching(id, getUserId());
            return ResponseEntity.ok().body("Removido com Sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro ao rejeitar: " + e.getMessage());
        }
    }


    @PostMapping("/insereDisponibilidade")
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ResponseEntity<?> criar(@RequestBody DisponibilidadeProfessorDtoRequest dto) {
        try {
            return ResponseEntity.ok(disponibilidadeService.inserirDisponibilidade(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/removeDisponibilidade/{id}")
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ResponseEntity<?> eliminar(@PathVariable(name = "id") String id) {
        try {
            disponibilidadeService.removerDisponibilidade(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
    public ResponseEntity<?> buscarHorario(@PathVariable(name = "id") String id) {
        try {
            return ResponseEntity.ok(aulaFixaService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Horário não encontrado: " + e.getMessage());
        }
    }

    @PostMapping("/criar")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> criarHorario(
            @RequestBody HorarioTurmaRequestDto dto,
            @RequestParam(name = "idProfessor") String idProfessor) {
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

            return ResponseEntity.status(HttpStatus.CREATED).body(aulaService.GerarAulasComHorario(dtoComAutor, idProfessor));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar horário: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> atualizarHorario(
            @PathVariable(name = "id") String id,
            @RequestBody HorarioTurmaRequestDto dto,
            @RequestParam(name = "idProfessor") String idProfessor) {
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
            return ResponseEntity.ok(aulaService.atualizaPorHorario(dtoComAutor, id, idProfessor,Utils.getAuthenticatedUserId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar horário: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> eliminarHorario(@PathVariable(name = "id") String id) {
        try {
            aulaService.EliminarAulasComHorario(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao eliminar horário: " + e.getMessage());
        }
    }

    @GetMapping("/coaching/todos")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> listarTodosCoachings(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(aulaCoachingService.findAll(pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao listar coachings: " + e.getMessage());
        }
    }

    @PutMapping("/coaching/{aulaId}/validar")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> validarRealizacaoCoordenacao(@PathVariable(name = "aulaId") String aulaId) {
        try {
            return ResponseEntity.ok(aulaService.validarRealizacao(aulaId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro ao validar coaching: " + e.getMessage());
        }
    }

    @PostMapping("/coaching/criar/aluno/{alunoId}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> criarCoachingPorAluno(
            @PathVariable(name = "alunoId") String alunoId,
            @RequestBody AulaCoachingRequestDto dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(aulaCoachingService.salvarMarcarCoaching(dto, alunoId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao criar coaching: " + e.getMessage());
        }
    }

    @DeleteMapping("/coaching/{aulaId}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> eliminarCoaching(@PathVariable(name = "aulaId") String aulaId) {
        try {
            aulaCoachingService.eliminar(aulaId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro ao eliminar coaching: " + e.getMessage());
        }
    }


}