package ipcaProjeto50.Grupo62026.SiteEntArtes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipcaProjeto50.Grupo62026.SiteEntArtes.config.SecurityConfig;
import ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Pagamentos.PagamentosController;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JwtService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.PagamentoService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.UtilizadorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;


import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagamentosController.class)
@Import(SecurityConfig.class)
public class PagamentosTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private PagamentoService pagamentoService;
    @MockitoBean private UtilizadorService utilizadorService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // --- Mocks Auxiliares para evitar repetição ---
    private UtilizadoreResumoDto criarResumo(String id) {
        return new UtilizadoreResumoDto(id, "Nome do Aluno");
    }

    // =========================================================================
    // BPMN PAGAMENTOS — COORDENAÇÃO (Exemplo de um teste OK)
    // =========================================================================

    @Test
    @DisplayName("[OK] Coordenacao lista todos os pagamentos -> 200 OK")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void coordenacaoListaTodosPagamentos() throws Exception {
        Mockito.when(pagamentoService.listarTodos()).thenReturn(List.of(new PagamentoDto(null, null, null, null, null, null, null, null, null, null)));
        mockMvc.perform(get("/api/pagamentos").with(csrf())).andExpect(status().isOk());
    }

    // =========================================================================
    // BPMN PAGAMENTOS — ENCARREGADO (CORRIGIDO)
    // =========================================================================

    @Test
    @DisplayName("[OK] Encarregado lista pagamentos de educando seu -> 200 OK")
    @WithMockUser(username = "pai1", authorities = "ENCARREGADO")
    void encarregadoListaPagamentosEducandoSeu() throws Exception {
        // CORREÇÃO: Utilizar UtilizadoreResumoDto (conforme o Service) e anyString() para o ID
        UtilizadoreResumoDto educando = criarResumo("educando1");

        Mockito.when(utilizadorService.findEducandosdeEducador(anyString()))
                .thenReturn(List.of(educando));

        Mockito.when(pagamentoService.listarPorUtilizador("educando1", 0))
                .thenReturn(List.of(new PagamentoDto(null, null, null, null, null, null, null, null, null, null)));

        mockMvc.perform(get("/api/pagamentos/educando/{idEducando}", "educando1")
                        .param("offset", "0")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[ERRO] Encarregado tenta aceder pagamentos de educando alheio -> 403 Forbidden")
    @WithMockUser(username = "pai1", authorities = "ENCARREGADO")
    void encarregadoListaPagamentosEducandoAlheio() throws Exception {
        // Mock devolve "outro", mas pedimos "educando1" -> Segurança bloqueia
        UtilizadoreResumoDto educando = criarResumo("outroEducando");

        Mockito.when(utilizadorService.findEducandosdeEducador(anyString()))
                .thenReturn(List.of(educando));

        mockMvc.perform(get("/api/pagamentos/educando/{idEducando}", "educando1")
                        .param("offset", "0")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        Mockito.verifyNoInteractions(pagamentoService);
    }

    @Test
    @DisplayName("[OK] Encarregado lista pagamentos paginados de educando seu -> 200 OK")
    @WithMockUser(username = "pai1", authorities = "ENCARREGADO")
    void encarregadoListaPagamentosEducandoPaginado() throws Exception {
        UtilizadoreResumoDto educando = criarResumo("educando1");
        Mockito.when(utilizadorService.findEducandosdeEducador(anyString())).thenReturn(List.of(educando));

        var page = new PageImpl<>(List.of(new PagamentoDto(null, null, null, null, null, null, null, null, null, null)), PageRequest.of(0, 10), 1);
        Mockito.when(pagamentoService.findAllPorUtilizador(eq("educando1"), any())).thenReturn(new PagedModel<>(page));

        mockMvc.perform(get("/api/pagamentos/educando/{idEducando}/paginado", "educando1").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[ERRO] Encarregado lista paginado de educando alheio -> 403 Forbidden")
    @WithMockUser(username = "pai1", authorities = "ENCARREGADO")
    void encarregadoListaPaginadoEducandoAlheio() throws Exception {
        UtilizadoreResumoDto educando = criarResumo("outroEducando");
        Mockito.when(utilizadorService.findEducandosdeEducador(anyString())).thenReturn(List.of(educando));

        mockMvc.perform(get("/api/pagamentos/educando/{idEducando}/paginado", "educando1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[OK] Encarregado consulta estatisticas de educando seu -> 200 OK")
    @WithMockUser(username = "pai1", authorities = "ENCARREGADO")
    void encarregadoConsultaEstatisticasEducandoSeu() throws Exception {
        UtilizadoreResumoDto educando = criarResumo("educando1");
        Mockito.when(utilizadorService.findEducandosdeEducador(anyString())).thenReturn(List.of(educando));
        Mockito.when(pagamentoService.obterEstatisticasAluno("educando1", 0)).thenReturn(new AlunoEstatisiticaDto());

        mockMvc.perform(get("/api/pagamentos/educando/{idEducando}/estatisticas", "educando1")
                        .param("offset", "0")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}