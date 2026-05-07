package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Utilizadores;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ProfessoreDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JwtService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.ProfessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfessorController.class)
@ActiveProfiles("test")
class ProfessorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ProfessorService professorService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private UtilizadoreRepository utilizadoreRepository;
    @MockitoBean private IdHasher idHasher;

    private ProfessoreDto professorExemplo;

    @BeforeEach
    void setUp() {
        UtilizadoreResumoDto resumo = new UtilizadoreResumoDto("profHash", "Professor Teste");
        professorExemplo = new ProfessoreDto(resumo, new BigDecimal("40.00"), true);
    }

    @Test
    @DisplayName("GET /api/professores - Deve retornar 200")
    @WithMockUser
    void getProfessores_Sucesso() throws Exception {
        when(professorService.findAllPageable(any()))
                .thenReturn(new PageImpl<>(List.of(professorExemplo)));

        mockMvc.perform(get("/api/professores")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].utilizadores.nome").value("Professor Teste"));
    }

    @Test
    @DisplayName("GET /api/professores/selecionar - Deve retornar 200")
    @WithMockUser
    void getProfessoresSelecionar_Sucesso() throws Exception {
        when(professorService.findAllUtilizador())
                .thenReturn(List.of(professorExemplo.utilizadores()));

        mockMvc.perform(get("/api/professores/selecionar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("profHash"));
    }

    @Test
    @DisplayName("POST /api/professores/{id}/modalidade/{mid} - Sucesso (Requer COORDENACAO)")
    @WithMockUser(authorities = "COORDENACAO")
    void adicionarModalidade_Sucesso() throws Exception {
        when(professorService.adicionarModalidade(anyString(), anyString()))
                .thenReturn(professorExemplo);

        mockMvc.perform(post("/api/professores/{professorId}/modalidade/{modalidadeId}", "p1", "m1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/professores/{id}/modalidade/{mid} - Sucesso")
    @WithMockUser(authorities = "COORDENACAO")
    void removerModalidade_Sucesso() throws Exception {
        doNothing().when(professorService).removerModalidade(anyString(), anyString());

        mockMvc.perform(delete("/api/professores/p1/modalidade/m1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/professores/{modalidadeId} - Sucesso")
    @WithMockUser
    void getProfessoresPorModalidade_Sucesso() throws Exception {
        when(professorService.findByModalidade(anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(professorExemplo)));

        // No teu controller o modalidadeId vem por @RequestParam no método com path {modalidadeId}
        // Nota: Verifica se queres @PathVariable ou @RequestParam, aqui usei como está no teu código
        mockMvc.perform(get("/api/professores/mod123")
                        .param("modalidadeId", "mod123"))
                .andExpect(status().isOk());
    }
}