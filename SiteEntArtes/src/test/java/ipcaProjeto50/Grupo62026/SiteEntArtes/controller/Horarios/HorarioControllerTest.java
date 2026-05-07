package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Horarios;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.Utils;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HorarioControllerTest {

    @Mock private AulaService aulaService;
    @Mock private AulaCoachingService aulaCoachingService;
    @Mock private DisponibilidadeService disponibilidadeService;
    @Mock private AulaFixaService aulaFixaService;
    @Mock private UtilizadorService utilizadorService;

    @InjectMocks
    private HorarioController horarioController;

    // Helper: simula getUserId() via SecurityContextHolder
    private void mockUserId(String userId) {
        UserDetails userDetails = mock(UserDetails.class);
        lenient().when(userDetails.getUsername()).thenReturn(userId);
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getPrincipal()).thenReturn(userDetails);
        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }


    // =========================================================================
    // region PROFESSOR
    // =========================================================================

    @Test
    @DisplayName("Professor insere as suas disponibilidades")
    void professorInsereDisponibilidade() {
        mockUserId("prof1");

        var dto = new DisponibilidadeProfessorDtoRequest(
                "prof1", 5,
                LocalTime.of(10, 0), LocalTime.of(12, 0),
                LocalDate.now(), LocalDate.now().plusDays(30)
        );

        ResponseEntity<?> response = horarioController.criar(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Professor consulta os seus coachings pendentes")
    void professorConsultaCoachingsPendentes() {
        mockUserId("prof1");
        when(aulaCoachingService.findPendentesByProfessorId(eq("prof1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<?> response = horarioController.coachingPendentes(Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Professor confirma uma aula de coaching (Sim no BPMN)")
    void professorConfirmaCoaching() {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("prof1");

            ResponseEntity<?> response = horarioController.confirmarCoaching("aula456");

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Test
    @DisplayName("Professor rejeita uma aula de coaching (Não no BPMN)")
    void professorRejeitaCoaching() {
        mockUserId("prof1");

        ResponseEntity<?> response = horarioController.rejeitarCoaching("aula456");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Professor remove uma disponibilidade")
    void professorRemoveDisponibilidade() {
        ResponseEntity<?> response = horarioController.eliminar("disp1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Professor tenta confirmar coaching inexistente → espera 404")
    void professorConfirmaCoachingInexistente_FalhaIntencional() throws Exception {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("prof1");
            when(aulaCoachingService.confirmar(eq("naoExiste"), any()))
                    .thenThrow(new RuntimeException("Coaching não encontrado"));

            ResponseEntity<?> response = horarioController.confirmarCoaching("naoExiste");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }


    // =========================================================================
    // region ENCARREGADO
    // =========================================================================

    @Test
    @DisplayName("Encarregado marca aula de coaching ao seu educando")
    void encarregadoMarcaCoaching() throws Exception {
        mockUserId("pai1");
        doNothing().when(utilizadorService).verificaPermissaoEducando("aluno123", "pai1");

        var dto = new AulaCoachingRequestDto(
                "prof1", "estudio1",
                LocalDate.now().plusDays(5),
                LocalTime.of(14, 0), LocalTime.of(15, 0),
                8, "mod_piano"
        );

        ResponseEntity<?> response = horarioController.marcarCoachingEducando("aluno123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Encarregado sem permissão sobre o educando recebe 400")
    void encarregadoSemPermissaoSobreEducando() throws Exception {
        mockUserId("pai1");
        doThrow(new RuntimeException("Sem permissão"))
                .when(utilizadorService).verificaPermissaoEducando("alunoAlheio", "pai1");

        var dto = new AulaCoachingRequestDto(
                "prof1", "est1",
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                1, "PIANO"
        );

        ResponseEntity<?> response = horarioController.marcarCoachingEducando("alunoAlheio", dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Encarregado tenta marcar coaching fora do horário de disponibilidade do professor")
    void erroHorarioForaDaDisponibilidade() throws Exception {
        mockUserId("pai1");
        doNothing().when(utilizadorService).verificaPermissaoEducando("aluno123", "pai1");
        when(aulaCoachingService.salvarMarcarCoaching(any(AulaCoachingRequestDto.class), eq("aluno123")))
                .thenThrow(new RuntimeException("O professor não tem disponibilidade neste horário"));

        var dto = new AulaCoachingRequestDto(
                "prof1", "est1",
                LocalDate.now().plusDays(1),
                LocalTime.of(14, 0), LocalTime.of(15, 0),
                1, "PIANO"
        );

        ResponseEntity<?> response = horarioController.marcarCoachingEducando("aluno123", dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Encarregado tenta marcar aula em dia bloqueado pela Coordenação")
    void erroCoordenacaoBloqueiaData() throws Exception {
        mockUserId("pai1");
        doNothing().when(utilizadorService).verificaPermissaoEducando("aluno123", "pai1");
        when(aulaCoachingService.salvarMarcarCoaching(any(), eq("aluno123")))
                .thenThrow(new RuntimeException("Data indisponível: Bloqueio pela Coordenação"));

        var dto = new AulaCoachingRequestDto(
                "prof1", "est1",
                LocalDate.now(),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                1, "PIANO"
        );

        ResponseEntity<?> response = horarioController.marcarCoachingEducando("aluno123", dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Encarregado marca coaching sem body → espera 400")
    void encarregadoMarcaCoachingSemBody_FalhaIntencional() throws Exception {
        mockUserId("pai1");
        doNothing().when(utilizadorService).verificaPermissaoEducando("aluno123", "pai1");
        when(aulaCoachingService.salvarMarcarCoaching(isNull(), eq("aluno123")))
                .thenThrow(new RuntimeException("Body em falta"));

        ResponseEntity<?> response = horarioController.marcarCoachingEducando("aluno123", null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    // =========================================================================
    // region COORDENACAO
    // =========================================================================

    @Test
    @DisplayName("Coordenação lista todos os horários")
    void coordenacaoListaHorarios() {
        when(aulaFixaService.findAll(any(Pageable.class))).thenReturn(new PagedModel<>(new PageImpl<>(List.of())));
        ResponseEntity<?> response = horarioController.listarHorarios(Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Coordenação cria horário de turma")
    void coordenacaoCriaHorario() {
        mockUserId("coord1");

        var dto = new HorarioTurmaRequestDto(
                null, "coord1", "turma1",
                LocalDate.now(), LocalDate.now().plusMonths(6),
                2, 60,
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                "estudio1"
        );

        ResponseEntity<?> response = horarioController.criarHorario(dto, "prof1");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("Coordenação cria horário sem idturma → 400")
    void coordenacaoCriaHorarioSemTurma() {
        mockUserId("coord1");

        var dto = new HorarioTurmaRequestDto(
                null, "coord1", null,
                LocalDate.now(), LocalDate.now().plusMonths(6),
                2, 60,
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                "estudio1"
        );

        ResponseEntity<?> response = horarioController.criarHorario(dto, "prof1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Coordenação cria horário com hora de fim anterior à de início → 400")
    void coordenacaoCriaHorarioHorasInvalidas() {
        mockUserId("coord1");

        var dto = new HorarioTurmaRequestDto(
                null, "coord1", "turma1",
                LocalDate.now(), LocalDate.now().plusMonths(6),
                2, 0,
                LocalTime.of(10, 0), LocalTime.of(8, 0),
                "estudio1"
        );

        ResponseEntity<?> response = horarioController.criarHorario(dto, "prof1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Coordenação lista todos os coachings")
    void coordenacaoListaTodosCoachings() {
        when(aulaCoachingService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<?> response = horarioController.listarTodosCoachings(Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Coordenação elimina um coaching")
    void coordenacaoEliminaCoaching() throws Exception {
        doNothing().when(aulaCoachingService).eliminar("aula1");

        ResponseEntity<?> response = horarioController.eliminarCoaching("aula1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Coordenação cria coaching diretamente para um aluno")
    void coordenacaoCriaCoachingParaAluno() {
        var dto = new AulaCoachingRequestDto(
                "prof1", "estudio1",
                LocalDate.now().plusDays(3),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                1, "PIANO"
        );

        ResponseEntity<?> response = horarioController.criarCoachingPorAluno("aluno1", dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("Coordenação elimina horário inexistente → espera 500")
    void coordenacaoEliminaHorarioInexistente_FalhaIntencional() throws Exception {
        doThrow(new RuntimeException("Horário não encontrado"))
                .when(aulaService).EliminarAulasComHorario("naoExiste");

        ResponseEntity<?> response = horarioController.eliminarHorario("naoExiste");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}