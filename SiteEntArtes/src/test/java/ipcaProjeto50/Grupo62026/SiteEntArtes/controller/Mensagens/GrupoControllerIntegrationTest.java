package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Mensagens;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.GrupoRequestDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.GrupoService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JwtService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GrupoController.class)
@ActiveProfiles("test")
class GrupoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private GrupoService grupoService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private UtilizadoreRepository utilizadoreRepository;
    @MockitoBean private IdHasher idHasher;

    private final String ADMIN_HASH = "adminHash123";

    @Test
    @DisplayName("GET /api/grupos/{grupoId}/membros - Sucesso")
    @WithMockUser
    void listarMembros_Sucesso() throws Exception {
        UtilizadoreResumoDto membro = new UtilizadoreResumoDto("userHash", "Membro Teste");
        when(grupoService.listarMembrosDoGrupo("grupoHash")).thenReturn(List.of(membro));

        mockMvc.perform(get("/api/grupos/{grupoId}/membros", "grupoHash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Membro Teste"));
    }

    @Test
    @DisplayName("POST /api/grupos - Sucesso")
    @WithMockUser(username = ADMIN_HASH)
    void criarGrupo_Sucesso() throws Exception {
        GrupoRequestDto dto = new GrupoRequestDto("Grupo de Pintura", List.of("membro1"));

        doNothing().when(grupoService).criarGrupoPrivado(anyString(), anyString(), anyList());

        mockMvc.perform(post("/api/grupos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Grupo criado com sucesso!"));
    }

    @Test
    @DisplayName("PUT /api/grupos/{grupoId}/adicionar/{membroId} - Sucesso")
    @WithMockUser(username = ADMIN_HASH)
    void adicionarMembro_Sucesso() throws Exception {
        doNothing().when(grupoService).adicionarMembro(anyString(), anyString(), anyString());

        // Alterado para put() e o path exato do teu controller
        mockMvc.perform(put("/api/grupos/{grupoId}/adicionar/{membroId}", "grupoHash", "membroHash")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Membro adicionado com sucesso."));
    }

    @Test
    @DisplayName("DELETE /api/grupos/{grupoId}/remover/{membroId} - Sucesso")
    @WithMockUser(username = ADMIN_HASH)
    void removerMembro_Sucesso() throws Exception {
        doNothing().when(grupoService).removerMembro(anyString(), anyString(), anyString());

        // Ajustado o path de /membros/ para /remover/
        mockMvc.perform(delete("/api/grupos/{grupoId}/remover/{membroId}", "grupoHash", "membroHash")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Membro removido com sucesso."));
    }
}