package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Utilizadores;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.Utils;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.EncarregadoAlunoService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JwtService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.UtilizadorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UtilizadorController.class)
@ActiveProfiles("test")
class UtilizadorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private UtilizadorService utilizadorService;
    @MockitoBean private EncarregadoAlunoService encarregadoAlunoService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private UtilizadoreRepository utilizadoreRepository;
    @MockitoBean private IdHasher idHasher;

    private UtilizadorResponseDto utilizadorDto;

    @BeforeEach
    void setUp() {
        utilizadorDto = new UtilizadorResponseDto(
                "hash-123", "Teste", "teste@email.pt", "123", "912",
                "ROLE_ALUNO", true, LocalDate.now(), LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /api/utilizadores/geraTokenEmail - Sucesso")
    @WithMockUser
    void geraTokenEmail_Sucesso() throws Exception {
        mockMvc.perform(post("/api/utilizadores/geraTokenEmail")
                        .param("email", "teste@email.pt")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Token enviado para o e-mail"));
    }

    @Test
    @DisplayName("GET /api/utilizadores - Sucesso (Requer COORDENACAO)")
    @WithMockUser(authorities = "COORDENACAO")
    void listarTodos_Sucesso() throws Exception {
        when(utilizadorService.listarTodos(any(), any()))
                .thenReturn(new PageImpl<>(List.of(utilizadorDto)));

        mockMvc.perform(get("/api/utilizadores")
                        .param("tipo", "ROLE_ALUNO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("teste@email.pt"));
    }

    @Test
    @DisplayName("GET /api/utilizadores/meu-perfil - Sucesso com Mock de Utils")
    @WithMockUser(username = "hash-123")
    void verMeuPerfil_Sucesso() throws Exception {
        try (MockedStatic<Utils> utilsMock = mockStatic(Utils.class)) {
            utilsMock.when(Utils::getAuthenticatedUserId).thenReturn("hash-123");
            when(utilizadorService.verMeuPerfil("hash-123")).thenReturn(utilizadorDto);

            mockMvc.perform(get("/api/utilizadores/meu-perfil"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("hash-123"));
        }
    }

    @Test
    @DisplayName("POST /api/utilizadores - Sucesso (Criar Utilizador)")
    @WithMockUser(authorities = "COORDENACAO")
    void criarUtilizador_Sucesso() throws Exception {
        // Criar um DTO simplificado para o request
        String json = "{\"nome\":\"Novo\",\"email\":\"novo@email.pt\",\"password\":\"123456\",\"nif\":\"111\",\"telemovel\":\"999\",\"role\":\"ALUNO\",\"dataNascimento\":\"2000-01-01\"}";

        when(utilizadorService.criarUtilizador(any())).thenReturn(utilizadorDto);

        mockMvc.perform(post("/api/utilizadores")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/utilizadores/associar-aluno-encarregado - Sucesso")
    @WithMockUser(authorities = "COORDENACAO")
    void associarAluno_Sucesso() throws Exception {
        doNothing().when(utilizadorService).associarAlunoAEncarregado(anyString(), anyString());

        mockMvc.perform(post("/api/utilizadores/associar-aluno-encarregado")
                        .param("idAluno", "alu-1")
                        .param("idEncarregado", "enc-1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Associação criada com sucesso."));
    }

    @Test
    @DisplayName("DELETE /api/utilizadores/{id} - Sucesso")
    @WithMockUser(authorities = "COORDENACAO")
    void apagarUtilizador_Sucesso() throws Exception {
        mockMvc.perform(delete("/api/utilizadores/{id}", "hash-123")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}