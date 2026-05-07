package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Notificacoes;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.NotificacoeDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JwtService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.NotificacoesService;
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

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificacoesController.class)
@ActiveProfiles("test")
class NotificacoesControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private NotificacoesService notificacoesService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private UtilizadoreRepository utilizadoreRepository;
    @MockitoBean private IdHasher idHasher;

    private NotificacoeDto notificacoeDto;

    @BeforeEach
    void setUp() {
        UtilizadoreResumoDto userResumo = new UtilizadoreResumoDto("userHash123", "Nome Teste");

        notificacoeDto = new NotificacoeDto(
                "notifHash123",
                userResumo,
                userResumo,
                "Nova Proposta",
                "Conteudo",
                "NEGOCIACAO",
                "ref456",
                false,
                Instant.now()
        );
    }

    // ─── TESTE: BUSCAR MINHAS NOTIFICAÇÕES (GET com RequestParam) ────────────
    @Test
    @DisplayName("GET /api/notificacoes/me - Deve retornar 200 com página de notificações")
    @WithMockUser
    void getMinhasNotificacoes_Sucesso() throws Exception {
        when(notificacoesService.findNotificacoesUtilizador(anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(notificacoeDto)));

        mockMvc.perform(get("/api/notificacoes/me")
                        .param("userId", "userHash123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("notifHash123"))
                .andExpect(jsonPath("$.content[0].titulo").value("Nova Proposta"));
    }

    // ─── TESTE: MARCAR COMO LIDA (PUT com PathVariable) ──────────────────────
    @Test
    @DisplayName("PUT /api/notificacoes/{id}/ler - Deve retornar 200")
    @WithMockUser
    void marcarComoLida_Sucesso() throws Exception {
        doNothing().when(notificacoesService).marcarComoLida("notifHash123");

        mockMvc.perform(put("/api/notificacoes/{id}/ler", "notifHash123")
                        .with(csrf())) // Proteção contra 403
                .andExpect(status().isOk());
    }
}