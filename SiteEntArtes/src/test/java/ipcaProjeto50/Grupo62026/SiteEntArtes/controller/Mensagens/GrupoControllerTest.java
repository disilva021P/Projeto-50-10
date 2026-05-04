package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Mensagens;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.GrupoRequestDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.GrupoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrupoControllerTest {

    @Mock
    private GrupoService grupoService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private GrupoController grupoController;

    private final String ADMIN_HASH = "adminHash123";

    @BeforeEach
    void setUp() {
        // Configurar o Mock do SecurityContext para devolver o nosso adminHash
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(ADMIN_HASH);
        SecurityContextHolder.setContext(securityContext);
    }

    // ─── listarMembros ───────────────────────────────────────────────────────

    @Test
    void listarMembros_comSucesso_retorna200() throws Exception {
        UtilizadoreResumoDto membro = new UtilizadoreResumoDto("userHash", "Membro Teste");
        when(grupoService.listarMembrosDoGrupo("grupoHash")).thenReturn(List.of(membro));

        ResponseEntity<?> response = grupoController.listarMembros("grupoHash");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(grupoService).listarMembrosDoGrupo("grupoHash");
    }

    @Test
    void listarMembros_grupoInexistente_retorna404() throws Exception {
        when(grupoService.listarMembrosDoGrupo(anyString()))
                .thenThrow(new RuntimeException("Grupo não encontrado"));

        ResponseEntity<?> response = grupoController.listarMembros("invalido");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Grupo não encontrado", response.getBody());
    }

    // ─── criarGrupo ──────────────────────────────────────────────────────────

    @Test
    void criarGrupo_comSucesso_retorna200() throws Exception {
        GrupoRequestDto dto = new GrupoRequestDto("Grupo de Pintura", List.of("membro1", "membro2"));

        doNothing().when(grupoService).criarGrupoPrivado(eq(ADMIN_HASH), eq(dto.nome()), anyList());

        ResponseEntity<String> response = grupoController.criarGrupo(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Grupo criado com sucesso!", response.getBody());
    }

    @Test
    void criarGrupo_erroNoService_retorna400() throws Exception {
        GrupoRequestDto dto = new GrupoRequestDto("", null);
        doThrow(new RuntimeException("Nome inválido")).when(grupoService).criarGrupoPrivado(any(), any(), any());

        ResponseEntity<String> response = grupoController.criarGrupo(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Nome inválido", response.getBody());
    }

    // ─── adicionarMembro ─────────────────────────────────────────────────────

    @Test
    void adicionarMembro_comSucesso_retorna200() throws Exception {
        doNothing().when(grupoService).adicionarMembro(ADMIN_HASH, "grupoHash", "membroHash");

        ResponseEntity<String> response = grupoController.adicionarMembro("grupoHash", "membroHash");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Membro adicionado com sucesso.", response.getBody());
    }

    @Test
    void adicionarMembro_semPermissao_retorna400() throws Exception {
        doThrow(new RuntimeException("Apenas o admin pode adicionar")).when(grupoService).adicionarMembro(any(), any(), any());

        ResponseEntity<String> response = grupoController.adicionarMembro("grupoHash", "membroHash");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ─── removerMembro ───────────────────────────────────────────────────────

    @Test
    void removerMembro_comSucesso_retorna200() throws Exception {
        doNothing().when(grupoService).removerMembro(ADMIN_HASH, "grupoHash", "membroHash");

        ResponseEntity<String> response = grupoController.removerMembro("grupoHash", "membroHash");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Membro removido com sucesso.", response.getBody());
    }

    @Test
    void removerMembro_erro_retorna400() throws Exception {
        doThrow(new RuntimeException("Membro não pertence ao grupo")).when(grupoService).removerMembro(any(), any(), any());

        ResponseEntity<String> response = grupoController.removerMembro("grupoHash", "membroHash");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Membro não pertence ao grupo"));
    }
}