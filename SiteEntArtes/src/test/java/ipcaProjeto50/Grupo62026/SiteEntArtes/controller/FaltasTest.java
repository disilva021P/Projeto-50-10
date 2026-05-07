package ipcaProjeto50.Grupo62026.SiteEntArtes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipcaProjeto50.Grupo62026.SiteEntArtes.config.SecurityConfig;
import ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Faltas.CancelamentoController;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.CancelamentoService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JustificacaoService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CancelamentoController.class)
@Import(SecurityConfig.class)
public class FaltasTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private CancelamentoService cancelamentoService;
    @MockitoBean private JustificacaoService justificacaoService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;


    // =========================================================================
    // BPMN FALTAS — PROFESSOR
    // Faltou? -> Nao -> Marca faltas a alunos -> Envia faltas
    // Faltou? -> Sim -> Envia justificacao
    // =========================================================================

    @Test
    @DisplayName("[OK] Professor marca falta a um aluno -> 201 Created")
    @WithMockUser(username = "prof1", roles = "PROFESSOR")
    void professorMarcaFalta() throws Exception {
        var dto = new FaltaDto("aula1", "aluno1", null, false, null,"PENDENTE");

        Mockito.when(cancelamentoService.marcarFalta(any(FaltaDto.class), eq("prof1")))
                .thenReturn(dto);

        mockMvc.perform(post("/api/faltas/marcar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("[OK] Professor lista faltas pendentes apos enviar")
    @WithMockUser(username = "prof1", roles = "PROFESSOR")
    void professorListaFaltasPendentes() throws Exception {
        Mockito.when(cancelamentoService.listarPendentes()).thenReturn(List.of());

        mockMvc.perform(get("/api/faltas/pendentes").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[OK] Professor lista todas as faltas")
    @WithMockUser(username = "prof1", roles = "PROFESSOR")
    void professorListaTodasFaltas() throws Exception {
        Mockito.when(cancelamentoService.listarTodas()).thenReturn(List.of());

        mockMvc.perform(get("/api/faltas").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[OK] Professor consulta faltas de um utilizador em detalhe")
    @WithMockUser(username = "prof1", roles = "PROFESSOR")
    void professorConsultaFaltasUtilizador() throws Exception {
        Mockito.when(cancelamentoService.listarFaltasPorUtilizador("hashAluno1"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/faltas/utilizador/{idHash}/detalhe", "hashAluno1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[OK] Professor consulta estatísticas de um aluno")
    @WithMockUser(username = "prof1", roles = "PROFESSOR")
    void professorConsultaEstatisticasAluno() throws Exception {
        // Dados simulados: total=2, justificadas=1, pendentes=1, injustificadas=0
        FaltaResumoDto resumoFake = new FaltaResumoDto(2L, 1L, 1L, 0L);

        Mockito.when(cancelamentoService.obterResumoEstatisticas("aluno1"))
                .thenReturn(resumoFake);

        mockMvc.perform(get("/api/faltas/aluno/{alunoId}/estatisticas", "aluno1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Opcional: Verificar se os valores no JSON estão corretos
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.injustificadas").value(0));
    }

    @Test
    @DisplayName("[ERRO] Professor marca falta com dados invalidos -> 400")
    @WithMockUser(username = "prof1", roles = "PROFESSOR")
    void professorMarcaFaltaDadosInvalidos() throws Exception {
        var dto = new FaltaDto(null, null, null, false, null,"PENDENTE");

        Mockito.when(cancelamentoService.marcarFalta(any(), any()))
                .thenThrow(new RuntimeException("Aula ou aluno nao encontrado"));

        mockMvc.perform(post("/api/faltas/marcar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[BPMN - Professor faltou -> Sim] Submete justificacao com PDF -> endpoint nao existe ainda -> 404")
    @WithMockUser(username = "prof1", roles = "PROFESSOR")
    void professorSubmeteJustificacao_EndpointEmFalta() throws Exception {
        // justificacaoService.submeterJustificacao() existe no Service mas nao ha
        // nenhum endpoint POST /api/faltas/{id}/justificar no controller
        // Este teste documenta a lacuna: quando o endpoint for criado deve passar a 200
        var pdf = new MockMultipartFile("pdf", "justificacao.pdf",
                MediaType.APPLICATION_PDF_VALUE, "conteudo".getBytes());

        mockMvc.perform(multipart("/api/faltas/{id}/justificar", "falta1")
                        .file(pdf)
                        .param("motivo", "Estive doente")
                        .with(csrf()))
                .andExpect(status().isNotFound()); // endpoint em falta no controller
    }


    // =========================================================================
    // BPMN FALTAS — ENCARREGADO
    // Justifica? -> Sim -> Preenche justificacao -> Envia justificacao
    // Justifica? -> Nao -> Falta Injustificada (termina)
    // =========================================================================

    @Test
    @DisplayName("[BPMN - Encarregado envia justificacao] Controller bloqueia com 403 - bug documentado")
    @WithMockUser(username = "pai1", roles = "ENCARREGADO")
    void encarregadoEnviaJustificacao_Bloqueado() throws Exception {
        // O BPMN mostra Encarregado a enviar justificacao mas o PUT /{id}
        // usa hasAnyRole('PROFESSOR','COORDENACAO') -> ENCARREGADO recebe 403
        // BUG: devia existir endpoint proprio com role ENCARREGADO
        var dto = new FaltaDto("aula1", "aluno1", "Filho esteve doente", false, null,"EM ANALISE");

        mockMvc.perform(put("/api/faltas/{id}", "falta1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[BPMN - Encarregado envia PDF] Endpoint POST /justificar nao existe -> 404")
    @WithMockUser(username = "pai1", roles = "ENCARREGADO")
    void encarregadoSubmeteJustificacaoComPdf_EndpointEmFalta() throws Exception {
        var pdf = new MockMultipartFile("pdf", "doc.pdf",
                MediaType.APPLICATION_PDF_VALUE, "pdf-bytes".getBytes());

        mockMvc.perform(multipart("/api/faltas/{id}/justificar", "falta1")
                        .file(pdf)
                        .param("motivo", "Filho esteve doente")
                        .with(csrf()))
                .andExpect(status().isNotFound()); // endpoint em falta
    }

    @Test
    @DisplayName("[OK] Aluno consulta as suas proprias faltas em detalhe -> 200")
    @WithMockUser(username = "aluno1", roles = "ALUNO")
    void alunoConsultaFaltasDetalhe() throws Exception {
        Mockito.when(cancelamentoService.listarFaltasPorUtilizador("hashAluno1"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/faltas/utilizador/{idHash}/detalhe", "hashAluno1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[OK] Aluno consulta as suas estatísticas de faltas -> 200")
    @WithMockUser(username = "aluno1", roles = "ALUNO")
    void alunoConsultaEstatisticas() throws Exception {
        // Definindo os 4 valores: total=3, justificadas=1, pendentes=2, injustificadas=0
        FaltaResumoDto resumoFake = new FaltaResumoDto(3L, 1L, 2L, 0L);

        Mockito.when(cancelamentoService.obterResumoEstatisticas("aluno1"))
                .thenReturn(resumoFake);

        mockMvc.perform(get("/api/faltas/aluno/{alunoId}/estatisticas", "aluno1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Verificação adicional para garantir que os dados do DTO chegam ao JSON
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.pendentes").value(2));
    }

    // =========================================================================
    // BPMN FALTAS — COORDENACAO
    // Recebe justificacao -> Justificacao valida?
    //   -> Sim -> Falta Justificada  (notifica Encarregado + Professor)
    //   -> Nao -> Falta Injustificada (notifica Encarregado + Professor)
    // =========================================================================

    @Test
    @DisplayName("[BPMN - Justificacao valida? Sim] Coordenacao aprova -> Falta Justificada -> 200")
    @WithMockUser(username = "coord1", roles = "COORDENACAO")
    void coordenacaoAprovaJustificacao() throws Exception {
        mockMvc.perform(patch("/api/faltas/{id}/validar", "falta1")
                        .with(csrf())
                        .param("aprovada", "true"))
                .andExpect(status().isOk());

        // Verifica que o service dispara notificacao de "Justificada"
        Mockito.verify(justificacaoService).validarFalta(eq("falta1"), eq(true), any());
    }

    @Test
    @DisplayName("[BPMN - Justificacao valida? Nao] Coordenacao rejeita -> Falta Injustificada -> 200")
    @WithMockUser(username = "coord1", roles = "COORDENACAO")
    void coordenacaoRejeitaJustificacao() throws Exception {
        mockMvc.perform(patch("/api/faltas/{id}/validar", "falta1")
                        .with(csrf())
                        .param("aprovada", "false"))
                .andExpect(status().isOk());

        // Verifica que o service dispara notificacao de "Injustificada"
        Mockito.verify(justificacaoService).validarFalta(eq("falta1"), eq(false), any());
    }

    @Test
    @DisplayName("[BPMN - Coordenacao consulta PDF] Endpoint GET /pdf nao existe -> 404")
    @WithMockUser(username = "coord1", roles = "COORDENACAO")
    void coordenacaoConsultaPdfJustificacao_EndpointEmFalta() throws Exception {
        // justificacaoService.verConteudoPdf() existe no Service
        // mas GET /api/faltas/{id}/pdf nao existe no controller
        Mockito.when(justificacaoService.verConteudoPdf("falta1"))
                .thenReturn("conteudo-pdf".getBytes());

        mockMvc.perform(get("/api/faltas/{id}/pdf", "falta1").with(csrf()))
                .andExpect(status().isNotFound()); // endpoint em falta
    }

    @Test
    @DisplayName("[OK] Coordenacao tambem pode marcar falta -> 201 Created")
    @WithMockUser(username = "coord1", roles = "COORDENACAO")
    void coordenacaoMarcaFalta() throws Exception {
        var dto = new FaltaDto("aula1", "aluno1", null, false, null,"PENDENTE");

        Mockito.when(cancelamentoService.marcarFalta(any(), eq("coord1"))).thenReturn(dto);

        mockMvc.perform(post("/api/faltas/marcar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("[OK] Coordenacao atualiza uma falta -> 200")
    @WithMockUser(username = "coord1", roles = "COORDENACAO")
    void coordenacaoAtualizaFalta() throws Exception {
        var dto = new FaltaDto("aula1", "aluno1", "Justificacao corrigida", true, null,"PENDENTE");

        Mockito.when(cancelamentoService.atualizarFalta(eq("falta1"), any())).thenReturn(dto);

        mockMvc.perform(put("/api/faltas/{id}", "falta1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[OK] Coordenacao remove uma falta -> 204 No Content")
    @WithMockUser(username = "coord1", roles = "COORDENACAO")
    void coordenacaoRemoveFalta() throws Exception {
        mockMvc.perform(delete("/api/faltas/{id}", "falta1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("[ERRO] Coordenacao valida sem parametro 'aprovada' -> 400")
    @WithMockUser(username = "coord1", roles = "COORDENACAO")
    void coordenacaoValidaSemParametro() throws Exception {
        mockMvc.perform(patch("/api/faltas/{id}/validar", "falta1").with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[ERRO] Coordenacao valida falta inexistente -> 400")
    @WithMockUser(username = "coord1", roles = "COORDENACAO")
    void coordenacaoValidaFaltaInexistente() throws Exception {
        Mockito.doThrow(new RuntimeException("Falta nao encontrada"))
                .when(justificacaoService).validarFalta(eq("naoExiste"), anyBoolean(), any());

        mockMvc.perform(patch("/api/faltas/{id}/validar", "naoExiste")
                        .with(csrf())
                        .param("aprovada", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[ERRO] Coordenacao remove falta inexistente -> 500")
    @WithMockUser(username = "coord1", roles = "COORDENACAO")
    void coordenacaoRemoveFaltaInexistente() throws Exception {
        Mockito.doThrow(new RuntimeException("Nao encontrada"))
                .when(cancelamentoService).removerFalta("naoExiste");

        mockMvc.perform(delete("/api/faltas/{id}", "naoExiste").with(csrf()))
                .andExpect(status().isInternalServerError());
    }


    // =========================================================================
    // SEGURANCA — ROLES ERRADAS
    // =========================================================================

    @Test
    @DisplayName("[SEGURANCA] ALUNO nao pode marcar faltas -> 403")
    @WithMockUser(username = "aluno1", roles = "ALUNO")
    void alunoNaoPodeMarcarFalta() throws Exception {
        mockMvc.perform(post("/api/faltas/marcar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FaltaDto("aula1", "aluno1", null, false, null,"PENDENTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[SEGURANCA] ALUNO nao pode validar justificacao -> 403")
    @WithMockUser(username = "aluno1", roles = "ALUNO")
    void alunoNaoPodeValidarJustificacao() throws Exception {
        mockMvc.perform(patch("/api/faltas/{id}/validar", "falta1")
                        .with(csrf()).param("aprovada", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[SEGURANCA] PROFESSOR nao pode remover falta (so COORDENACAO) -> 403")
    @WithMockUser(username = "prof1", roles = "PROFESSOR")
    void professorNaoPodeRemoverFalta() throws Exception {
        mockMvc.perform(delete("/api/faltas/{id}", "falta1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[SEGURANCA] ENCARREGADO nao pode listar todas as faltas -> 403")
    @WithMockUser(username = "pai1", roles = "ENCARREGADO")
    void encarregadoNaoPodeListarTodasFaltas() throws Exception {
        mockMvc.perform(get("/api/faltas").with(csrf()))
                .andExpect(status().isForbidden());
    }


    // =========================================================================
    // FALHAS INTENCIONAIS
    // =========================================================================

    @Test
    @DisplayName("[FALHA INTENCIONAL] Professor marca falta -> espera 200 mas controller devolve 201")
    @WithMockUser(username = "prof1", roles = "PROFESSOR")
    void professorMarcaFalta_FalhaIntencional() throws Exception {
        var dto = new FaltaDto("aula1", "aluno1", null, false, null,"PENDENTE");
        Mockito.when(cancelamentoService.marcarFalta(any(), any())).thenReturn(dto);

        mockMvc.perform(post("/api/faltas/marcar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk()); // FALHA: devolve 201
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Coordenacao remove inexistente -> espera 404 mas devolve 500")
    @WithMockUser(username = "coord1", roles = "COORDENACAO")
    void coordenacaoRemoveFaltaInexistente_FalhaIntencional() throws Exception {
        Mockito.doThrow(new RuntimeException("Nao encontrado"))
                .when(cancelamentoService).removerFalta("naoExiste");

        mockMvc.perform(delete("/api/faltas/{id}", "naoExiste").with(csrf()))
                .andExpect(status().isNotFound()); // FALHA: controller usa INTERNAL_SERVER_ERROR
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] ALUNO lista todas as faltas -> espera 200 mas devolve 403")
    @WithMockUser(username = "aluno1", roles = "ALUNO")
    void alunoListaTodasFaltas_FalhaIntencional() throws Exception {
        mockMvc.perform(get("/api/faltas").with(csrf()))
                .andExpect(status().isOk()); // FALHA: devolve 403
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Coordenacao atualiza inexistente -> espera 200 mas devolve 404")
    @WithMockUser(username = "coord1", roles = "COORDENACAO")
    void coordenacaoAtualizaFaltaInexistente_FalhaIntencional() throws Exception {
        var dto = new FaltaDto("aulaX", "alunoX", "Justificacao", true, null,"PENDENTE");
        Mockito.when(cancelamentoService.atualizarFalta(eq("naoExiste"), any()))
                .thenThrow(new RuntimeException("Nao encontrada"));

        mockMvc.perform(put("/api/faltas/{id}", "naoExiste")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk()); // FALHA: devolve 404
    }
}