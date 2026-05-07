package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.InventarioDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.InventarioUnidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.InventarioUnidadeRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.InventarioService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JwtService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventarioController.class)
@ActiveProfiles("test")
class InventarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Beans do controller ---
    @MockitoBean private InventarioService inventarioService;
    @MockitoBean private InventarioUnidadeRepository unidadeRepository;
    @MockitoBean private IdHasher idHasher;

    // --- Beans exigidos pelo JwtAuthenticationFilter ---
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private UtilizadoreRepository utilizadoreRepository;

    private InventarioDto inventarioDto;

    @BeforeEach
    void setUp() {
        inventarioDto = new InventarioDto(
                "hash123",
                "Cadeira Antiga",
                "Cadeira em bom estado",
                "M",
                "Castanho",
                "Bom",
                1,
                "Disponível",
                true,
                "Armazém A",
                "Sem notas",
                Instant.now(),
                "imgHash1",
                List.of("imgHash1", "imgHash2")
        );
    }

    // ─── GET /api/inventario ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/inventario deve retornar 200 com JSON paginado")
    @WithMockUser
    void listar_deveRetornar200ComJsonCorreto() throws Exception {
        when(inventarioService.filtrarInventario(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(inventarioDto)));

        mockMvc.perform(get("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value("hash123"))
                .andExpect(jsonPath("$.content[0].nomeArtigo").value("Cadeira Antiga"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/inventario sem autenticação deve retornar 401")
    void listar_semAutenticacao_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/inventario com página vazia deve retornar 200 com lista vazia")
    @WithMockUser
    void listar_paginaVazia_deveRetornar200ComArrayVazio() throws Exception {
        when(inventarioService.filtrarInventario(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── GET /api/inventario/unidades ─────────────────────────────────────────

    @Test
    @DisplayName("GET /api/inventario/unidades sem autenticação deve retornar 401")
    void getUnidadesDisponiveis_semAutenticacao_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/inventario/unidades"))
                .andExpect(status().isUnauthorized());
    }
}