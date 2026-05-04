package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Mensagens;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.MensagemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MensagensControllerTest {

    @Mock
    private MensagemService mensagemService;

    @InjectMocks
    private MensagensController mensagensController;

    private MensagenDto mensagemDto;
    private UtilizadoreResumoDto userResumo;
    private final String USER_EMAIL = "teste@ipca.pt";

    @BeforeEach
    void setUp() {
        userResumo = new UtilizadoreResumoDto("hashUser", "Utilizador Teste");
        mensagemDto = new MensagenDto(
                "msgHash123",
                userResumo, // remetente
                userResumo, // destinatario
                "Olá, tudo bem?",
                LocalDateTime.now()
        );
    }

    // ─── getPreviewMensagens ─────────────────────────────────────────────────

    @Test
    void getPreviewMensagens_comSucesso_retorna200ELista() {
        MensagenPreviewDto preview = mock(MensagenPreviewDto.class);
        when(mensagemService.buscarPreviewMensagens(USER_EMAIL)).thenReturn(List.of(preview));

        ResponseEntity<List<MensagenPreviewDto>> response = mensagensController.getPreviewMensagens(USER_EMAIL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getPreviewMensagens_utilizadorNaoEncontrado_retorna404() {
        when(mensagemService.buscarPreviewMensagens(anyString())).thenThrow(new NoSuchElementException());

        ResponseEntity<List<MensagenPreviewDto>> response = mensagensController.getPreviewMensagens("outro@ipca.pt");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── getMensagensConversa ────────────────────────────────────────────────

    @Test
    void getMensagensConversa_comSucesso_retorna200ELista() throws Exception {
        when(mensagemService.mensagensConversa(USER_EMAIL, "conversaHash")).thenReturn(List.of(mensagemDto));

        ResponseEntity<List<MensagenDto>> response = mensagensController.getMensagensConversa(USER_EMAIL, "conversaHash");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getMensagensConversa_idInvalido_retorna400() throws Exception {
        when(mensagemService.mensagensConversa(anyString(), eq("!!!")))
                .thenThrow(new IllegalArgumentException());

        ResponseEntity<List<MensagenDto>> response = mensagensController.getMensagensConversa(USER_EMAIL, "!!!");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ─── criarMensagem (Individual) ──────────────────────────────────────────

    @Test
    void criarMensagem_comDadosValidos_retorna200EMensagem() throws Exception {
        MensagemCriarDto request = new MensagemCriarDto("destHash", "Conteúdo");
        when(mensagemService.criar(eq(USER_EMAIL), any(MensagemCriarDto.class))).thenReturn(mensagemDto);

        ResponseEntity<MensagenDto> response = mensagensController.criarMensagem(USER_EMAIL, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void criarMensagem_destinatarioNaoExiste_retorna404() throws Exception {
        MensagemCriarDto request = new MensagemCriarDto("naoExiste", "...");
        // Adicionado throws Exception para satisfazer o compilador no when(...)
        when(mensagemService.criar(anyString(), any())).thenThrow(new NoSuchElementException());

        ResponseEntity<MensagenDto> response = mensagensController.criarMensagem(USER_EMAIL, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── eliminarMensagem ────────────────────────────────────────────────────

    @Test
    void eliminarMensagem_comSucesso_retorna204() {
        doNothing().when(mensagemService).eliminar("msgHash123");

        ResponseEntity<Void> response = mensagensController.eliminarMensagem("msgHash123");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(mensagemService).eliminar("msgHash123");
    }

    @Test
    void eliminarMensagem_idInexistente_retorna404() {
        doThrow(new NoSuchElementException()).when(mensagemService).eliminar("hashInexistente");

        ResponseEntity<Void> response = mensagensController.eliminarMensagem("hashInexistente");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── Teste de Erro Genérico (Exemplo para 500) ───────────────────────────

    @Test
    void getPreviewMensagens_erroServidor_retorna500() {
        when(mensagemService.buscarPreviewMensagens(anyString())).thenThrow(new RuntimeException("Crash"));

        ResponseEntity<List<MensagenPreviewDto>> response = mensagensController.getPreviewMensagens(USER_EMAIL);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}