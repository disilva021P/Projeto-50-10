package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Utilizadores;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipcaProjeto50.Grupo62026.SiteEntArtes.config.SecurityConfig;
import ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Utilizadores.UtilizadorController;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UtilizadorController.class)
@Import(SecurityConfig.class)
class UtilizadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private UtilizadorService utilizadorService;
    @MockitoBean private EncarregadoAlunoService encarregadoAlunoService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    // ── DTO de resposta fake reutilizável ──────────────────────────────────────
    private UtilizadorResponseDto dtoFake() {
        return new UtilizadorResponseDto(
                "abc123", "João Silva", "joao@entartes.pt",
                "123456789", "912345678", "ROLE_ENCARREGADO",
                true, LocalDate.of(1990, 1, 1), LocalDateTime.now()
        );
    }

    // =========================================================================
    // region LISTAR TODOS
    // =========================================================================

    @Test
    @DisplayName("[OK] Coordenação lista todos os utilizadores → 200")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void listarTodos_coordenacao_retorna200() throws Exception {
        Page<UtilizadorResponseDto> pagina = new PageImpl<>(List.of(dtoFake()));
        Mockito.when(utilizadorService.listarTodos(isNull(), any(Pageable.class)))
                .thenReturn(pagina);

        mockMvc.perform(get("/api/utilizadores")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[OK] Coordenação filtra utilizadores por tipo → 200")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void listarTodos_comFiltroTipo_retorna200() throws Exception {
        Page<UtilizadorResponseDto> pagina = new PageImpl<>(List.of(dtoFake()));
        Mockito.when(utilizadorService.listarTodos(eq("ROLE_ALUNO"), any(Pageable.class)))
                .thenReturn(pagina);

        mockMvc.perform(get("/api/utilizadores")
                        .param("tipo", "ROLE_ALUNO")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Role PROFESSOR tenta listar utilizadores → espera 200 mas devia ser 403")
    @WithMockUser(username = "prof1", authorities = "PROFESSOR")
    void listarTodos_professor_FalhaIntencional() throws Exception {
        // FALHA INTENCIONAL: PROFESSOR não tem autoridade COORDENACAO
        // O teste espera 200, mas Spring Security devolve 403
        mockMvc.perform(get("/api/utilizadores")
                        .with(csrf()))
                .andExpect(status().isOk()); // ❌ FALHA: devolve 403
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Role ALUNO tenta listar utilizadores → espera 200 mas devia ser 403")
    @WithMockUser(username = "aluno1", authorities = "ALUNO")
    void listarTodos_aluno_FalhaIntencional() throws Exception {
        // FALHA INTENCIONAL: ALUNO não tem autoridade COORDENACAO
        mockMvc.perform(get("/api/utilizadores")
                        .with(csrf()))
                .andExpect(status().isOk()); // ❌ FALHA: devolve 403
    }

    // =========================================================================
    // region VER DETALHE
    // =========================================================================

    @Test
    @DisplayName("[OK] Coordenação vê detalhe de utilizador existente → 200")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void verDetalhe_utilizadorExiste_retorna200() throws Exception {
        Mockito.when(utilizadorService.verDetalhe("abc123")).thenReturn(dtoFake());

        mockMvc.perform(get("/api/utilizadores/abc123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva"));
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Ver detalhe de utilizador inexistente → espera 200 mas devia ser 404")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void verDetalhe_utilizadorInexistente_FalhaIntencional() throws Exception {
        Mockito.when(utilizadorService.verDetalhe("naoExiste"))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Não encontrado"));

        // FALHA INTENCIONAL: controller devolve 404 mas o teste espera 200
        mockMvc.perform(get("/api/utilizadores/naoExiste")
                        .with(csrf()))
                .andExpect(status().isOk()); // ❌ FALHA: devolve 404
    }

    // =========================================================================
    // region CRIAR UTILIZADOR
    // =========================================================================


    @Test
    @DisplayName("[FALHA INTENCIONAL] Criar utilizador sem body → espera 201 mas devia ser 400")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void criarUtilizador_semBody_FalhaIntencional() throws Exception {
        // FALHA INTENCIONAL: sem body o Spring devolve 400
        // mas o teste espera 201 — vai falhar
        mockMvc.perform(post("/api/utilizadores")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()); // ❌ FALHA: devolve 400
    }



    // =========================================================================
    // region TOGGLE ATIVO
    // =========================================================================

    @Test
    @DisplayName("[OK] Coordenação faz toggle ativo de utilizador → 200")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void toggleAtivo_coordenacao_retorna200() throws Exception {
        Mockito.when(utilizadorService.toggleAtivo("abc123")).thenReturn(dtoFake());

        mockMvc.perform(patch("/api/utilizadores/abc123/toggle-ativo")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Toggle em utilizador inexistente → espera 200 mas devia ser 404")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void toggleAtivo_utilizadorInexistente_FalhaIntencional() throws Exception {
        Mockito.when(utilizadorService.toggleAtivo("naoExiste"))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Não encontrado"));

        // FALHA INTENCIONAL: controller devolve 404 mas o teste espera 200
        mockMvc.perform(patch("/api/utilizadores/naoExiste/toggle-ativo")
                        .with(csrf()))
                .andExpect(status().isOk()); // ❌ FALHA: devolve 404
    }

    // =========================================================================
    // region APAGAR UTILIZADOR
    // =========================================================================

    @Test
    @DisplayName("[OK] Coordenação apaga utilizador → 204")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void apagarUtilizador_coordenacao_retorna204() throws Exception {
        Mockito.doNothing().when(utilizadorService).apagarUtilizador("abc123");

        mockMvc.perform(delete("/api/utilizadores/abc123")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Apagar utilizador inexistente → espera 204 mas devia ser 404")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void apagarUtilizador_naoExiste_FalhaIntencional() throws Exception {
        Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("Não encontrado"))
                .when(utilizadorService).apagarUtilizador("naoExiste");

        // FALHA INTENCIONAL: controller devolve 404 mas o teste espera 204
        mockMvc.perform(delete("/api/utilizadores/naoExiste")
                        .with(csrf()))
                .andExpect(status().isNoContent()); // ❌ FALHA: devolve 404
    }

    // =========================================================================
    // region REPOR PASSWORD
    // =========================================================================

    @Test
    @DisplayName("[OK] Coordenação repõe password com dados válidos → 204")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void reporPassword_dadosValidos_retorna204() throws Exception {
        Mockito.doNothing().when(utilizadorService).reporPalavraPasse(any(), any());

        ReporPasswordDto dto = new ReporPasswordDto("nova123", "nova123");

        mockMvc.perform(patch("/api/utilizadores/abc123/repor-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Repor password com passwords diferentes → espera 204 mas devia ser 500")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void reporPassword_passwordsDiferentes_FalhaIntencional() throws Exception {
        Mockito.doThrow(new Exception("Passwords não coincidem"))
                .when(utilizadorService).reporPalavraPasse(any(), any());

        ReporPasswordDto dto = new ReporPasswordDto("nova123", "diferente456");

        // FALHA INTENCIONAL: service lança exceção → controller devolve 500
        // mas o teste espera 204
        mockMvc.perform(patch("/api/utilizadores/abc123/repor-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent()); // ❌ FALHA: devolve 500
    }

    // =========================================================================
    // region MEUS EDUCANDOS
    // =========================================================================

    @Test
    @DisplayName("[OK] Encarregado vê os seus educandos → 200")
    @WithMockUser(username = "enc1", authorities = "ENCARREGADO")
    void meusEducandos_encarregado_retorna200() throws Exception {
        Mockito.when(utilizadorService.findEducandosdeEducador(anyString()))
                .thenReturn(List.of(new UtilizadoreResumoDto("alu1", "Maria")));

        mockMvc.perform(get("/api/utilizadores/meus-educandos")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] PROFESSOR tenta ver educandos → espera 200 mas devia ser 403")
    @WithMockUser(username = "prof1", authorities = "PROFESSOR")
    void meusEducandos_professor_FalhaIntencional() throws Exception {
        // FALHA INTENCIONAL: PROFESSOR não tem autoridade ENCARREGADO
        // Spring Security devolve 403 mas o teste espera 200
        mockMvc.perform(get("/api/utilizadores/meus-educandos")
                        .with(csrf()))
                .andExpect(status().isOk()); // ❌ FALHA: devolve 403
    }

    // =========================================================================
    // region ASSOCIAR / REMOVER EDUCANDO
    // =========================================================================

    @Test
    @DisplayName("[OK] Coordenação associa educando a encarregado → 200")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void associarEducando_coordenacao_retorna200() throws Exception {
        Mockito.doNothing().when(encarregadoAlunoService).adicionarEducando(any(), any());

        mockMvc.perform(post("/api/utilizadores/enc1/educandos/alu1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Associar educando já associado → espera 200 mas devia ser 409")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void associarEducando_duplicado_FalhaIntencional() throws Exception {
        Mockito.doThrow(new IllegalStateException("Já existe"))
                .when(encarregadoAlunoService).adicionarEducando(any(), any());

        // FALHA INTENCIONAL: service lança IllegalStateException → controller devolve 409
        // mas o teste espera 200
        mockMvc.perform(post("/api/utilizadores/enc1/educandos/alu1")
                        .with(csrf()))
                .andExpect(status().isOk()); // ❌ FALHA: devolve 409
    }

    @Test
    @DisplayName("[OK] Coordenação remove educando de encarregado → 204")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void removerEducando_coordenacao_retorna204() throws Exception {
        Mockito.doNothing().when(encarregadoAlunoService).removerEducando(any(), any());

        mockMvc.perform(delete("/api/utilizadores/enc1/educandos/alu1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Remover educando inexistente → espera 204 mas devia ser 404")
    @WithMockUser(username = "coord1", authorities = "COORDENACAO")
    void removerEducando_naoExiste_FalhaIntencional() throws Exception {
        Mockito.doThrow(new jakarta.persistence.EntityNotFoundException("Não encontrado"))
                .when(encarregadoAlunoService).removerEducando(any(), any());

        // FALHA INTENCIONAL: controller devolve 404 mas o teste espera 204
        mockMvc.perform(delete("/api/utilizadores/enc1/educandos/alu1")
                        .with(csrf()))
                .andExpect(status().isNoContent()); // ❌ FALHA: devolve 404
    }
}