package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Horarios;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HorarioController.class)
@ActiveProfiles("test")
class HorarioControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // --- Beans do controller ---
    @MockitoBean private AulaService aulaService;
    @MockitoBean private AulaCoachingService aulaCoachingService;
    @MockitoBean private DisponibilidadeService disponibilidadeService;
    @MockitoBean private AulaFixaService aulaFixaService;
    @MockitoBean private UtilizadorService utilizadorService;

    // --- Beans exigidos pelo JwtAuthenticationFilter ---
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private UtilizadoreRepository utilizadoreRepository;
    @MockitoBean private IdHasher idHasher;

    // =========================================================================
    // ALUNO
    // =========================================================================

    @Test
    @DisplayName("GET /api/horario/semana deve retornar 200 para ALUNO")
    @WithMockUser(authorities = "ALUNO")
    void horarioSemanaAluno_comAluno_retorna200() throws Exception {
        when(aulaService.buscarHorarioSemana(any(), any(Integer.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/horario/semana"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/horario/semana sem autenticação deve retornar 401")
    void horarioSemanaAluno_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(get("/api/horario/semana"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/horario/marcarcoaching deve retornar 200 para ALUNO")
    @WithMockUser(authorities = "ALUNO")
    void marcarCoachingAluno_comAluno_retorna200() throws Exception {
        AulaCoachingRequestDto dto = new AulaCoachingRequestDto(
                "profHash", "estudioHash",
                LocalDate.now().plusDays(3),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                1, "PIANO"
        );
        when(aulaCoachingService.salvarMarcarCoaching(any(), any())).thenReturn(null);

        mockMvc.perform(post("/api/horario/marcarcoaching")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/horario/cancelarCoaching/{aulaId} deve retornar 204 para ALUNO")
    @WithMockUser(authorities = "ALUNO")
    void cancelarCoachingAluno_comAluno_retorna204() throws Exception {
        doNothing().when(aulaCoachingService).cancelarInscricao(any(), any());

        mockMvc.perform(delete("/api/horario/cancelarCoaching/aulaHash")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    // =========================================================================
    // ENCARREGADO
    // =========================================================================

    @Test
    @DisplayName("GET /api/horario/semana/educando/{id} deve retornar 200 para ENCARREGADO")
    @WithMockUser(authorities = "ENCARREGADO")
    void horarioSemanaEducando_comEncarregado_retorna200() throws Exception {
        when(aulaService.buscarHorarioSemana(any(), any(Integer.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/horario/semana/educando/alunoHash"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/horario/marcarcoaching/educando/{id} deve retornar 200 para ENCARREGADO")
    @WithMockUser(authorities = "ENCARREGADO")
    void marcarCoachingEducando_comEncarregado_retorna200() throws Exception {
        AulaCoachingRequestDto dto = new AulaCoachingRequestDto(
                "profHash", "estudioHash",
                LocalDate.now().plusDays(3),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                1, "PIANO"
        );
        doNothing().when(utilizadorService).verificaPermissaoEducando(any(), any());
        when(aulaCoachingService.salvarMarcarCoaching(any(), any())).thenReturn(null);

        mockMvc.perform(post("/api/horario/marcarcoaching/educando/alunoHash")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // PROFESSOR
    // =========================================================================

    @Test
    @DisplayName("GET /api/horario/professor/coaching/pendentes deve retornar 200 para PROFESSOR")
    @WithMockUser(authorities = "PROFESSOR")
    void coachingPendentes_comProfessor_retorna200() throws Exception {
        when(aulaCoachingService.findPendentesByProfessorId(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/horario/professor/coaching/pendentes"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/horario/insereDisponibilidade deve retornar 200 para PROFESSOR")
    @WithMockUser(authorities = "PROFESSOR")
    void insereDisponibilidade_comProfessor_retorna200() throws Exception {
        DisponibilidadeProfessorDtoRequest dto = new DisponibilidadeProfessorDtoRequest(
                "profHash", 5,
                LocalTime.of(10, 0), LocalTime.of(12, 0),
                LocalDate.now(), LocalDate.now().plusDays(30)
        );
        when(disponibilidadeService.inserirDisponibilidade(any())).thenReturn(null);

        mockMvc.perform(post("/api/horario/insereDisponibilidade")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/horario/removeDisponibilidade/{id} deve retornar 204 para PROFESSOR")
    @WithMockUser(authorities = "PROFESSOR")
    void removeDisponibilidade_comProfessor_retorna204() throws Exception {
        doNothing().when(disponibilidadeService).removerDisponibilidade("dispHash");

        mockMvc.perform(delete("/api/horario/removeDisponibilidade/dispHash")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /api/horario/professor/coaching/{id}/confirmar deve retornar 200 para PROFESSOR")
    @WithMockUser(authorities = "PROFESSOR")
    void confirmarCoaching_comProfessor_retorna200() throws Exception {
        when(aulaCoachingService.confirmar(any(), any())).thenReturn(null);

        mockMvc.perform(put("/api/horario/professor/coaching/aulaHash/confirmar")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // =========================================================================
    // COORDENACAO
    // =========================================================================

    @Test
    @DisplayName("GET /api/horario deve retornar 200 para COORDENACAO")
    @WithMockUser(authorities = "COORDENACAO")
    void listarHorarios_comCoordenacao_retorna200() throws Exception {
        when(aulaFixaService.findAll(any())).thenReturn(new PagedModel<>(new PageImpl<>(List.of())));

        mockMvc.perform(get("/api/horario"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/horario sem autenticação deve retornar 401")
    void listarHorarios_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(get("/api/horario"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/horario/coaching/todos deve retornar 200 para COORDENACAO")
    @WithMockUser(authorities = "COORDENACAO")
    void listarTodosCoachings_comCoordenacao_retorna200() throws Exception {
        when(aulaCoachingService.findAll(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/horario/coaching/todos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/horario/coaching/{aulaId} deve retornar 204 para COORDENACAO")
    @WithMockUser(authorities = "COORDENACAO")
    void eliminarCoaching_comCoordenacao_retorna204() throws Exception {
        doNothing().when(aulaCoachingService).eliminar("aulaHash");

        mockMvc.perform(delete("/api/horario/coaching/aulaHash")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/horario/coaching/criar/aluno/{id} deve retornar 201 para COORDENACAO")
    @WithMockUser(authorities = "COORDENACAO")
    void criarCoachingPorAluno_comCoordenacao_retorna201() throws Exception {
        AulaCoachingRequestDto dto = new AulaCoachingRequestDto(
                "profHash", "estudioHash",
                LocalDate.now().plusDays(3),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                1, "PIANO"
        );
        when(aulaCoachingService.salvarMarcarCoaching(any(), any())).thenReturn(null);

        mockMvc.perform(post("/api/horario/coaching/criar/aluno/alunoHash")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("DELETE /api/horario/{id} deve retornar 204 para COORDENACAO")
    @WithMockUser(authorities = "COORDENACAO")
    void eliminarHorario_comCoordenacao_retorna204() throws Exception {
        doNothing().when(aulaService).EliminarAulasComHorario("horarioHash");

        mockMvc.perform(delete("/api/horario/horarioHash")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}