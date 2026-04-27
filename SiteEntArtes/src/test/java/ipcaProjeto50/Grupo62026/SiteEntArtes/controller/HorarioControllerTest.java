package ipcaProjeto50.Grupo62026.SiteEntArtes.controller;

// Imports Java e Jackson
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// Imports JUnit e Mockito
import ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Horarios.HorarioController;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

// Imports Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

// Imports Spring Security Test
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

// Imports MockMvc (Estáticos)
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

// Imports do Projeto
import ipcaProjeto50.Grupo62026.SiteEntArtes.config.SecurityConfig;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaCoachingRequestDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.DisponibilidadeProfessorDtoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.HorarioTurmaRequestDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.*;

@WebMvcTest(HorarioController.class)
@Import(SecurityConfig.class)
public class HorarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Mocks dos Serviços do Controller ---
    @MockitoBean private AulaService aulaService;
    @MockitoBean private AulaCoachingService aulaCoachingService;
    @MockitoBean private DisponibilidadeService disponibilidadeService;
    @MockitoBean private AulaFixaService aulaFixaService;
    @MockitoBean private UtilizadorService utilizadorService;

    // --- Mocks para Segurança e Contexto ---
    @MockitoBean private JwtService jwtService;
    @MockitoBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;


    // =========================================================================
    // region PROFESSOR
    // =========================================================================

    @Test
    @DisplayName("[OK] Professor insere as suas disponibilidades")
    @WithMockUser(username = "prof1", authorities = "PROFESSOR")
    void professorInsereDisponibilidade() throws Exception {
        var dto = new DisponibilidadeProfessorDtoRequest(
                "prof1", 5,
                LocalTime.of(10, 0), LocalTime.of(12, 0),
                LocalDate.now(), LocalDate.now().plusDays(30)
        );

        mockMvc.perform(post("/api/horario/insereDisponibilidade")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[OK] Professor consulta os seus coachings pendentes")
    @WithMockUser(username = "prof1", authorities = "PROFESSOR")
    void professorConsultaCoachingsPendentes() throws Exception {
        mockMvc.perform(get("/api/horario/professor/coaching/pendentes")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[OK] Professor confirma uma aula de coaching (Sim no BPMN)")
    @WithMockUser(username = "prof1", authorities = "PROFESSOR")
    void professorConfirmaCoaching() throws Exception {
        mockMvc.perform(put("/api/horario/professor/coaching/{aulaId}/confirmar", "aula456")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[OK] Professor rejeita uma aula de coaching (Não no BPMN)")
    @WithMockUser(username = "prof1", authorities = "PROFESSOR")
    void professorRejeitaCoaching() throws Exception {
        mockMvc.perform(put("/api/horario/professor/coaching/rejeitar/{id}", "aula456")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[OK] Professor remove uma disponibilidade")
    @WithMockUser(username = "prof1", authorities = "PROFESSOR")
    void professorRemoveDisponibilidade() throws Exception {
        mockMvc.perform(delete("/api/horario/removeDisponibilidade/{id}", "disp1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Professor tenta confirmar coaching inexistente → espera 200 mas devia ser 404")
    @WithMockUser(username = "prof1", authorities = "PROFESSOR")
    void professorConfirmaCoachingInexistente_FalhaIntencional() throws Exception {
        // FALHA INTENCIONAL: configuramos o service para lançar exceção (coaching não existe)
        // mas o teste espera 200 — vai falhar porque o controller devolve 404
        Mockito.when(aulaCoachingService.confirmar(eq("naoExiste"), any()))
                .thenThrow(new RuntimeException("Coaching não encontrado"));

        mockMvc.perform(put("/api/horario/professor/coaching/{aulaId}/confirmar", "naoExiste")
                        .with(csrf()))
                .andExpect(status().isOk()); // ❌ FALHA: controller devolve 404
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Role ALUNO tenta aceder a endpoint de PROFESSOR → espera 200 mas devia ser 403")
    @WithMockUser(username = "aluno1", authorities = "ALUNO")
    void alunoAcessaEndpointProfessor_FalhaIntencional() throws Exception {
        // FALHA INTENCIONAL: um ALUNO não tem autoridade PROFESSOR
        // O teste espera 200, mas o Spring Security devolve 403
        mockMvc.perform(post("/api/horario/insereDisponibilidade")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk()); // ❌ FALHA: devolve 403
    }


    // =========================================================================
    // region ENCARREGADO
    // =========================================================================

    @Test
    @DisplayName("[OK] Encarregado marca aula de coaching ao seu educando")
    @WithMockUser(username = "pai1", authorities = "ENCARREGADO")
    void encarregadoMarcaCoaching() throws Exception {
        String educandoId = "aluno123";

        Mockito.when(utilizadorService.findEducandosdeEducador("pai1"))
                .thenReturn(List.of(new UtilizadoreResumoDto(educandoId, "Nome Aluno")));

        var dto = new AulaCoachingRequestDto(
                "prof1", "estudio1",
                LocalDate.now().plusDays(5),
                LocalTime.of(14, 0), LocalTime.of(15, 0),
                8, "mod_piano"
        );

        mockMvc.perform(post("/api/horario/marcarcoaching/educando/{educandoId}", educandoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[OK] Encarregado sem permissão sobre o educando recebe 400")
    @WithMockUser(username = "pai1", authorities = "ENCARREGADO")
    void encarregadoSemPermissaoSobreEducando() throws Exception {
        // pai1 não tem educandos → verificaPermissaoEducando lança exceção
        Mockito.when(utilizadorService.findEducandosdeEducador("pai1"))
                .thenReturn(List.of());

        var dto = new AulaCoachingRequestDto(
                "prof1", "est1",
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                1, "PIANO"
        );

        mockMvc.perform(post("/api/horario/marcarcoaching/educando/{educandoId}", "alunoAlheio")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[ERRO] Encarregado tenta marcar coaching fora do horário de disponibilidade do professor")
    @WithMockUser(username = "pai1", authorities = "ENCARREGADO")
    void erroHorarioForaDaDisponibilidade() throws Exception {
        String educandoId = "aluno123";

        Mockito.when(utilizadorService.findEducandosdeEducador("pai1"))
                .thenReturn(List.of(new UtilizadoreResumoDto(educandoId, "Nome")));

        Mockito.when(aulaCoachingService.salvarMarcarCoaching(any(AulaCoachingRequestDto.class), eq(educandoId)))
                .thenThrow(new RuntimeException("O professor não tem disponibilidade neste horário (Disponível apenas das 10h às 12h)"));

        var dto = new AulaCoachingRequestDto(
                "prof1", "est1",
                LocalDate.now().plusDays(1),
                LocalTime.of(14, 0), LocalTime.of(15, 0),
                1, "PIANO"
        );

        mockMvc.perform(post("/api/horario/marcarcoaching/educando/{educandoId}", educandoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[ERRO] Encarregado tenta marcar aula em dia bloqueado pela Coordenação")
    @WithMockUser(username = "pai1", authorities = "ENCARREGADO")
    void erroCoordenacaoBloqueiaData() throws Exception {
        String educandoId = "aluno123";

        Mockito.when(utilizadorService.findEducandosdeEducador("pai1"))
                .thenReturn(List.of(new UtilizadoreResumoDto(educandoId, "Nome")));

        Mockito.when(aulaCoachingService.salvarMarcarCoaching(any(), eq(educandoId)))
                .thenThrow(new RuntimeException("Data indisponível: Bloqueio pela Coordenação"));

        var dto = new AulaCoachingRequestDto(
                "prof1", "est1",
                LocalDate.now(),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                1, "PIANO"
        );

        mockMvc.perform(post("/api/horario/marcarcoaching/educando/{educandoId}", educandoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Encarregado marca coaching sem body → espera 200 mas devia ser 400")
    @WithMockUser(username = "pai1", authorities = "ENCARREGADO")
    void encarregadoMarcaCoachingSemBody_FalhaIntencional() throws Exception {
        String educandoId = "aluno123";

        Mockito.when(utilizadorService.findEducandosdeEducador("pai1"))
                .thenReturn(List.of(new UtilizadoreResumoDto(educandoId, "Nome")));

        // FALHA INTENCIONAL: envia pedido sem body → Spring devolve 400
        // mas o teste espera 200
        mockMvc.perform(post("/api/horario/marcarcoaching/educando/{educandoId}", educandoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // ❌ FALHA: devolve 400 (body obrigatório)
    }


    // =========================================================================
    // region COORDENACAO
    // =========================================================================

    @Test
    @DisplayName("[OK] Coordenação lista todos os horários")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void coordenacaoListaHorarios() throws Exception {
        mockMvc.perform(get("/api/horario")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[OK] Coordenação cria horário de turma (insere compatibilidades/bloqueios)")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void coordenacaoCriaHorario() throws Exception {
        var dto = new HorarioTurmaRequestDto(
                null,             // id
                "coord1",         // criadorId
                "turma1",         // idturma
                LocalDate.now(),  // dataInicio
                LocalDate.now().plusMonths(6), // dataValidade
                2,                // diaSemana (Terça)
                60,               // duracao em minutos
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "estudio1"        // estudioId
        );

        mockMvc.perform(post("/api/horario/criar")
                        .with(csrf())
                        .param("idProfessor", "prof1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("[ERRO] Coordenação cria horário sem idturma → 400")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void coordenacaoCriaHorarioSemTurma() throws Exception {
        // idturma = null → controller devolve 400 explicitamente
        var dto = new HorarioTurmaRequestDto(
                null, "coord1", null, // idturma nulo
                LocalDate.now(), LocalDate.now().plusMonths(6),
                2, 60,
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                "estudio1"
        );

        mockMvc.perform(post("/api/horario/criar")
                        .with(csrf())
                        .param("idProfessor", "prof1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[ERRO] Coordenação cria horário com hora de fim anterior à de início → 400")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void coordenacaoCriaHorarioHorasInvalidas() throws Exception {
        // horaFim (08:00) < horaInicio (10:00) → duração negativa → 400
        var dto = new HorarioTurmaRequestDto(
                null, "coord1", "turma1",
                LocalDate.now(), LocalDate.now().plusMonths(6),
                2, 0,
                LocalTime.of(10, 0), LocalTime.of(8, 0), // ← inválido
                "estudio1"
        );

        mockMvc.perform(post("/api/horario/criar")
                        .with(csrf())
                        .param("idProfessor", "prof1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[OK] Coordenação lista todos os coachings")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void coordenacaoListaTodosCoachings() throws Exception {
        mockMvc.perform(get("/api/horario/coaching/todos")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[OK] Coordenação elimina um coaching")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void coordenacaoEliminaCoaching() throws Exception {
        mockMvc.perform(delete("/api/horario/coaching/{aulaId}", "aula1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("[OK] Coordenação cria coaching diretamente para um aluno")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void coordenacaoCriaCoachingParaAluno() throws Exception {
        var dto = new AulaCoachingRequestDto(
                "prof1", "estudio1",
                LocalDate.now().plusDays(3),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                1, "PIANO"
        );

        mockMvc.perform(post("/api/horario/coaching/criar/aluno/{alunoId}", "aluno1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Role PROFESSOR tenta aceder a endpoint de COORDENACAO → espera 200 mas devia ser 403")
    @WithMockUser(username = "prof1", authorities = "PROFESSOR")
    void professorAcessaEndpointCoordenacao_FalhaIntencional() throws Exception {
        // FALHA INTENCIONAL: PROFESSOR não tem autoridade COORDENACAO
        // O teste espera 200, mas Spring Security devolve 403
        mockMvc.perform(get("/api/horario/coaching/todos")
                        .with(csrf()))
                .andExpect(status().isOk()); // ❌ FALHA: devolve 403
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Coordenação elimina horário inexistente → espera 204 mas devia ser 500")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void coordenacaoEliminaHorarioInexistente_FalhaIntencional() throws Exception {
        // FALHA INTENCIONAL: service lança exceção → controller devolve 500
        // mas o teste espera 204 (No Content)
        Mockito.doThrow(new RuntimeException("Horário não encontrado"))
                .when(aulaService).EliminarAulasComHorario("naoExiste");

        mockMvc.perform(delete("/api/horario/{id}", "naoExiste")
                        .with(csrf()))
                .andExpect(status().isNoContent()); // ❌ FALHA: devolve 500
    }
}