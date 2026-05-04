package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Notificacoes;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.NotificacoeDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.NotificacoesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacoesControllerTest {

    @Mock
    private NotificacoesService notificacoesService;

    @InjectMocks
    private NotificacoesController notificacoesController;

    private NotificacoeDto notificacoeDto;

    @BeforeEach
    void setUp() {
        // Criar um resumo de utilizador fictício para o DTO
        UtilizadoreResumoDto userResumo = new UtilizadoreResumoDto("userHash123", "Nome Teste");

        notificacoeDto = new NotificacoeDto(
                "notifHash123",
                userResumo,      // destinatario
                userResumo,      // remetente
                "Nova Proposta",
                "Recebeu uma nova proposta no artigo X",
                "NEGOCIACAO",
                "ref456",
                false,           // lida
                Instant.now()
        );
    }

    // ─── getMinhasNotificacoes ───────────────────────────────────────────────

    @Test
    void getMinhasNotificacoes_comSucesso_retorna200EPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<NotificacoeDto> page = new PageImpl<>(List.of(notificacoeDto));

        when(notificacoesService.findNotificacoesUtilizador(eq("userHash123"), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<Page<NotificacoeDto>> response = notificacoesController.getMinhasNotificacoes("userHash123", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("Nova Proposta", response.getBody().getContent().get(0).titulo());
        verify(notificacoesService).findNotificacoesUtilizador("userHash123", pageable);
    }

    @Test
    void getMinhasNotificacoes_quandoVazio_retorna200EPaginaVazia() {
        Page<NotificacoeDto> emptyPage = new PageImpl<>(List.of());
        when(notificacoesService.findNotificacoesUtilizador(anyString(), any()))
                .thenReturn(emptyPage);

        ResponseEntity<Page<NotificacoeDto>> response = notificacoesController.getMinhasNotificacoes("userSemNotif", Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getContent().isEmpty());
    }

    // ─── marcarComoLida ──────────────────────────────────────────────────────

    @Test
    void marcarComoLida_comIdValido_retorna200() {
        // Como o método no controller retorna ResponseEntity.ok().build() (Void)
        doNothing().when(notificacoesService).marcarComoLida("notifHash123");

        ResponseEntity<Void> response = notificacoesController.marcarComoLida("notifHash123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificacoesService, times(1)).marcarComoLida("notifHash123");
    }

    @Test
    void marcarComoLida_comIdInexistente_propagaExcecao() {
        // Simular que o serviço lança exceção ao não encontrar a notificação
        doThrow(new NoSuchElementException("Notificação não encontrada"))
                .when(notificacoesService).marcarComoLida("idInvalido");

        assertThrows(NoSuchElementException.class,
                () -> notificacoesController.marcarComoLida("idInvalido"));
    }

    @Test
    void marcarComoLida_quandoErroServidor_propagaExcecao() {
        doThrow(new RuntimeException("Erro BD"))
                .when(notificacoesService).marcarComoLida(anyString());

        assertThrows(RuntimeException.class,
                () -> notificacoesController.marcarComoLida("notifHash123"));
    }
}