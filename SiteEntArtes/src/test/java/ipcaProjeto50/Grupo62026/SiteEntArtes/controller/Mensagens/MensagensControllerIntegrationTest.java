package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Mensagens;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagemCriarDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagenDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JwtService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.MensagemService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MensagensController.class)
@ActiveProfiles("test")
class MensagensControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private MensagemService mensagemService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private UtilizadoreRepository utilizadoreRepository;
    @MockitoBean private IdHasher idHasher;

    private MensagenDto mensagemExemplo;

    @BeforeEach
    void setUp() {
        UtilizadoreResumoDto user = new UtilizadoreResumoDto("hash1", "Admin");
        mensagemExemplo = new MensagenDto("msgHash", user, user, "Teste", LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/mensagens/previews - Deve retornar 200")
    @WithMockUser(username = "teste@ipca.pt")
    void getPreviews_Sucesso() throws Exception {
        when(mensagemService.buscarPreviewMensagens(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/mensagens/previews"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/mensagens/conversa - Deve retornar 200")
    @WithMockUser(username = "teste@ipca.pt")
    void getConversa_Sucesso() throws Exception {
        // Usamos any() para o email porque o @AuthenticationPrincipal pode vir como objeto User
        when(mensagemService.mensagensConversa(any(), anyString()))
                .thenReturn(List.of(mensagemExemplo));

        mockMvc.perform(get("/api/mensagens/conversa")
                        .param("conversaId", "hashConversa123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("msgHash"));
    }

    @Test
    @DisplayName("POST /api/mensagens - Deve retornar 200")
    @WithMockUser(username = "teste@ipca.pt")
    void criarMensagem_Sucesso() throws Exception {
        MensagemCriarDto novoDto = new MensagemCriarDto("destinatarioHash", "Olá Mundo");

        when(mensagemService.criar(any(), any(MensagemCriarDto.class)))
                .thenReturn(mensagemExemplo);

        mockMvc.perform(post("/api/mensagens")
                        .with(csrf()) // Resolve o erro 403
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("msgHash"));
    }

    @Test
    @DisplayName("DELETE /api/mensagens/{id} - Deve retornar 204")
    @WithMockUser
    void eliminar_Sucesso() throws Exception {
        mockMvc.perform(delete("/api/mensagens/{id}", "msgHash")
                        .with(csrf())) // Resolve o erro 403
                .andExpect(status().isNoContent());
    }
}