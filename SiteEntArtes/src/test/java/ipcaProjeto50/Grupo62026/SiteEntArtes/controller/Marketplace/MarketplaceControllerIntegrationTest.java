package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Marketplace;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ImagensUnidadeRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JwtService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.MarketplaceService;
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
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MarketplaceController.class)
@ActiveProfiles("test")
class MarketplaceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Beans do controller ---
    @MockitoBean private MarketplaceService marketplaceService;
    @MockitoBean private ImagensUnidadeRepository imagensUnidadeRepository;
    @MockitoBean private IdHasher idHasher;

    // --- Beans exigidos pelo JwtAuthenticationFilter e SecurityConfig ---
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private UtilizadoreRepository utilizadoreRepository;

    private ArtigoDto artigoDto;

    @BeforeEach
    void setUp() {
        artigoDto = new ArtigoDto(
                "abc123",
                "Vestido Azul",
                "Vestido em bom estado",
                "M",
                "Azul",
                "Bom",
                "user1",
                "João Silva",
                true,
                false,
                false,
                new BigDecimal("15.00"),
                null,
                Instant.now(),
                1,
                "Disponível",
                "img1",
                List.of("img1", "img2")
        );
    }

    // Valida que o Spring arranca, o routing está correto e o JSON é serializado bem
    @Test
    @DisplayName("GET /api/marketplace deve retornar 200 com JSON paginado")
    @WithMockUser
    void listarArtigos_deveRetornar200ComJsonCorreto() throws Exception {
        when(marketplaceService.filtrarArtigos(
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(artigoDto)));

        mockMvc.perform(get("/api/marketplace")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value("abc123"))
                .andExpect(jsonPath("$.content[0].nome").value("Vestido Azul"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }


    //verificar que um pedido sem token é bloqueado com 401/403
    @Test
    @DisplayName("GET /api/marketplace sem autenticação deve retornar 401")
    void listarArtigos_semAutenticacao_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/marketplace"))
                .andExpect(status().isUnauthorized());
    }
}