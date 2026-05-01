package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.NotificacoeDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Notificacoe;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.NotificacoeRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacoesServiceTest {

    @Mock private NotificacoeRepository notificacoeRepository;
    @Mock private UtilizadoreRepository utilizadoreRepository;
    @Mock private IdHasher idHasher;

    private NotificacoesService notificacoesService;

    @BeforeEach
    void setUp() {
        notificacoesService = new NotificacoesService(
                notificacoeRepository,
                utilizadoreRepository,
                idHasher
        );
    }

    // --- TESTES DE CRIAÇÃO ---

    @Test
    @DisplayName("Deve criar notificação com sucesso quando remetente existe")
    void criarNotificacao_ComRemetente_Sucesso() throws Exception {
        // GIVEN
        Utilizadore dest = new Utilizadore(); dest.setId(1);
        Utilizadore rem = new Utilizadore(); rem.setId(2);

        when(utilizadoreRepository.findById(1)).thenReturn(Optional.of(dest));
        when(utilizadoreRepository.findById(2)).thenReturn(Optional.of(rem));

        // WHEN
        notificacoesService.criarNotificacao(1, 2, "Título", "Msg", "TIPO", "ref123");

        // THEN
        verify(notificacoeRepository).save(argThat(n ->
                n.getDestinatario().getId().equals(1) &&
                        n.getRemetente().getId().equals(2) &&
                        n.getTitulo().equals("Título")
        ));
    }

    @Test
    @DisplayName("Deve criar notificação de sistema (sem remetente)")
    void criarNotificacao_SemRemetente_Sucesso() throws Exception {
        // GIVEN
        Utilizadore dest = new Utilizadore(); dest.setId(1);
        when(utilizadoreRepository.findById(1)).thenReturn(Optional.of(dest));

        // WHEN
        notificacoesService.criarNotificacao(1, null, "Sistema", "Bem-vindo", "INFO", null);

        // THEN
        verify(notificacoeRepository).save(argThat(n ->
                n.getRemetente() == null && n.getTitulo().equals("Sistema")
        ));
    }

    @Test
    @DisplayName("Deve lançar exceção se destinatário não existir")
    void criarNotificacao_DestinatarioInexistente_Erro() {
        when(utilizadoreRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(Exception.class, () ->
                notificacoesService.criarNotificacao(99, null, "T", "M", "T", "R")
        );
    }

    @Test
    @DisplayName("Deve criar notificação mesmo que o remetenteId seja inválido (fica null)")
    void criarNotificacao_RemetenteInvalido_FicaNull() throws Exception {
        // GIVEN
        Utilizadore dest = new Utilizadore(); dest.setId(1);
        when(utilizadoreRepository.findById(1)).thenReturn(Optional.of(dest));
        // Simulamos que o remetente ID 999 não existe
        when(utilizadoreRepository.findById(999)).thenReturn(Optional.empty());

        // WHEN
        notificacoesService.criarNotificacao(1, 999, "Título", "Msg", "TIPO", null);

        // THEN
        verify(notificacoeRepository).save(argThat(n -> n.getRemetente() == null));
    }

    // --- TESTES DE LEITURA E LISTAGEM ---

    @Test
    @DisplayName("Deve marcar notificação como lida")
    void marcarComoLida_Sucesso() {
        // GIVEN
        String hash = "notif_hash";
        Integer idReal = 10;
        Notificacoe n = new Notificacoe();
        n.setId(idReal);
        n.setLida(false);

        when(idHasher.decode(hash)).thenReturn(idReal);
        when(notificacoeRepository.findById(idReal)).thenReturn(Optional.of(n));

        // WHEN
        notificacoesService.marcarComoLida(hash);

        // THEN
        assertTrue(n.getLida());
        verify(notificacoeRepository).save(n);
    }

    @Test
    @DisplayName("Deve listar apenas notificações não lidas do utilizador")
    void findNotificacoesUtilizador_Sucesso() {
        // GIVEN
        String userHash = "user_hash";
        Integer userId = 5;
        when(idHasher.decode(userHash)).thenReturn(userId);

        Notificacoe n = new Notificacoe();
        n.setId(100);
        n.setDestinatario(new Utilizadore()); n.getDestinatario().setId(userId);
        n.setTitulo("Teste");

        Page<Notificacoe> page = new PageImpl<>(List.of(n));
        when(notificacoeRepository.findAllByDestinatarioIdAndLidaFalse(eq(userId), any())).thenReturn(page);
        when(idHasher.encode(any())).thenReturn("hash_enc");

        // WHEN
        Page<NotificacoeDto> resultado = notificacoesService.findNotificacoesUtilizador(userHash, PageRequest.of(0, 10));

        // THEN
        assertFalse(resultado.isEmpty());
        assertEquals("Teste", resultado.getContent().get(0).titulo());
        verify(notificacoeRepository).findAllByDestinatarioIdAndLidaFalse(eq(userId), any());
    }

    @Test
    @DisplayName("Deve converter entidade para DTO corretamente tratando remetente nulo")
    void converterParaNotificacaoDto_TrataRemetenteNulo() {
        // GIVEN
        Notificacoe n = new Notificacoe();
        n.setId(1);
        n.setDestinatario(new Utilizadore()); n.getDestinatario().setId(10);
        n.setRemetente(null); // Cenário de notificação de sistema
        n.setTitulo("Aviso");

        when(idHasher.encode(1)).thenReturn("n1");
        when(idHasher.encode(10)).thenReturn("u10");

        // WHEN
        NotificacoeDto dto = notificacoesService.converterParaNotificacaoDto(n);

        // THEN
        assertNotNull(dto);
        assertNull(dto.remetente());
        assertEquals("n1", dto.id());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao marcar notificação inexistente como lida")
    void marcarComoLida_Inexistente_Erro() {
        // GIVEN
        String hash = "notif_404";
        when(idHasher.decode(hash)).thenReturn(404);
        when(notificacoeRepository.findById(404)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () ->
                notificacoesService.marcarComoLida(hash)
        );
    }
}