package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Faltas;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.CancelamentoService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JustificacaoService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CancelamentoController.class)
@ActiveProfiles("test")
class CancelamentoControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // --- Beans do controller ---
    @MockitoBean private CancelamentoService cancelamentoService;
    @MockitoBean private JustificacaoService justificacaoService;

    // --- Beans exigidos pelo JwtAuthenticationFilter ---
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private UtilizadoreRepository utilizadoreRepository;
    @MockitoBean private IdHasher idHasher;

    private FaltaDto faltaDto;
    private FaltaResumoDto resumoDto;

    @BeforeEach
    void setUp() {
        faltaDto = new FaltaDto("hash1", "aulaHash", "userHash", false, null, "PENDENTE");
        resumoDto = new FaltaResumoDto(10, 3, 4, 3);
    }

    // ─── GET /api/faltas (COORDENACAO) ────────────────────────────────────────

    @Test
    @DisplayName("GET /api/faltas deve retornar 200 com lista para COORDENACAO")
    @WithMockUser(authorities = "COORDENACAO")
    void listarTodas_comCoordenacao_retorna200() throws Exception {
        when(cancelamentoService.listarTodas()).thenReturn(List.of(faltaDto));

        mockMvc.perform(get("/api/faltas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("hash1"))
                .andExpect(jsonPath("$[0].estado").value("PENDENTE"));
    }

    @Test
    @DisplayName("GET /api/faltas sem autenticação deve retornar 401")
    void listarTodas_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(get("/api/faltas"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/faltas/pendentes (COORDENACAO) ──────────────────────────────

    @Test
    @DisplayName("GET /api/faltas/pendentes deve retornar 200 com lista para COORDENACAO")
    @WithMockUser(authorities = "COORDENACAO")
    void listarPendentes_comCoordenacao_retorna200() throws Exception {
        when(cancelamentoService.listarPendentes()).thenReturn(List.of(faltaDto));

        mockMvc.perform(get("/api/faltas/pendentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].estado").value("PENDENTE"));
    }

    @Test
    @DisplayName("GET /api/faltas/pendentes sem autenticação deve retornar 401")
    void listarPendentes_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(get("/api/faltas/pendentes"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/faltas/meu-perfil/detalhe (ALUNO/PROFESSOR) ────────────────

    @Test
    @DisplayName("GET /api/faltas/meu-perfil/detalhe deve retornar 200 para ALUNO")
    @WithMockUser(authorities = "ALUNO")
    void listarMinhasFaltas_comAluno_retorna200() throws Exception {
        when(cancelamentoService.listarFaltasPorUtilizador(any())).thenReturn(List.of(faltaDto));

        mockMvc.perform(get("/api/faltas/meu-perfil/detalhe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("hash1"));
    }

    // ─── GET /api/faltas/meu-perfil/estatisticas (ALUNO/PROFESSOR) ───────────

    @Test
    @DisplayName("GET /api/faltas/meu-perfil/estatisticas deve retornar 200 com resumo")
    @WithMockUser(authorities = "ALUNO")
    void obterMinhasEstatisticas_comAluno_retorna200() throws Exception {
        when(cancelamentoService.obterResumoEstatisticas(any())).thenReturn(resumoDto);

        mockMvc.perform(get("/api/faltas/meu-perfil/estatisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.justificadas").value(3))
                .andExpect(jsonPath("$.pendentes").value(4))
                .andExpect(jsonPath("$.injustificadas").value(3));
    }

    // ─── GET /api/faltas/utilizador/{idHash}/detalhe (COORDENACAO) ───────────

    @Test
    @DisplayName("GET /api/faltas/utilizador/{idHash}/detalhe deve retornar 200 para COORDENACAO")
    @WithMockUser(authorities = "COORDENACAO")
    void listarFaltasPorUtilizador_comCoordenacao_retorna200() throws Exception {
        when(cancelamentoService.listarFaltasPorUtilizador("hash1")).thenReturn(List.of(faltaDto));

        mockMvc.perform(get("/api/faltas/utilizador/hash1/detalhe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("hash1"));
    }

    // ─── POST /api/faltas/marcar (PROFESSOR/COORDENACAO) ─────────────────────

    @Test
    @DisplayName("POST /api/faltas/marcar deve retornar 201 para PROFESSOR")
    @WithMockUser(authorities = "PROFESSOR")
    void marcar_comProfessor_retorna201() throws Exception {
        when(cancelamentoService.marcarFalta(any(), any())).thenReturn(faltaDto);

        mockMvc.perform(post("/api/faltas/marcar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(faltaDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("hash1"));
    }


    // ─── DELETE /api/faltas/{id} (COORDENACAO) ────────────────────────────────

    @Test
    @DisplayName("DELETE /api/faltas/{id} deve retornar 204 para COORDENACAO")
    @WithMockUser(authorities = "COORDENACAO")
    void remover_comCoordenacao_retorna204() throws Exception {
        mockMvc.perform(delete("/api/faltas/hash1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}