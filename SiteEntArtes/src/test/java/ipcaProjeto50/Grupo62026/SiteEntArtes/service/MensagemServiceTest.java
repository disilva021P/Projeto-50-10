package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Mensagen;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MensagemServiceTest {

    @Mock private MensagensGrupoService mensagensTurmaService;
    @Mock private UtilizadoreRepository utilizadoreRepository;
    @Mock private MensagensGrupoRepository mensagensGrupoRepository;
    @Mock private GrupoRepository grupoRepository;
    @Mock private MensagenRepository mensagenRepository;
    @Mock private IdHasher idHasher;
    @Mock private UtilizadorLogRepository utilizadorLogRepository;
    @Mock private NotificacoesService notificacoesService;

    private MensagemService mensagemService;

    @BeforeEach
    void setUp() {
        mensagemService = new MensagemService(
                mensagensTurmaService,
                utilizadoreRepository,
                mensagensGrupoRepository,
                grupoRepository,
                mensagenRepository,
                idHasher,
                utilizadorLogRepository,
                notificacoesService
        );
    }

    // --- TESTES DE MENSAGENS PRIVADAS ---

    @Test
    @DisplayName("Deve criar mensagem privada e disparar notificação")
    void criarMensagem_Sucesso() throws Exception {
        // GIVEN
        String idRemetenteHash = "rem_hash";
        String idDestinatarioHash = "dest_hash";
        Integer idRemReal = 1;
        Integer idDestReal = 2;

        Utilizadore remetente = new Utilizadore(); remetente.setId(idRemReal); remetente.setNome("Rem");
        Utilizadore destinatario = new Utilizadore(); destinatario.setId(idDestReal); destinatario.setNome("Dest");

        MensagemCriarDto dto = new MensagemCriarDto(idDestinatarioHash, "Olá!");

        when(idHasher.decode(idRemetenteHash)).thenReturn(idRemReal);
        when(idHasher.decode(idDestinatarioHash)).thenReturn(idDestReal);
        when(utilizadoreRepository.findById(idRemReal)).thenReturn(Optional.of(remetente));
        when(utilizadoreRepository.findById(idDestReal)).thenReturn(Optional.of(destinatario));

        when(mensagenRepository.save(any(Mensagen.class))).thenAnswer(i -> {
            Mensagen m = i.getArgument(0);
            m.setId(100);
            return m;
        });
        when(idHasher.encode(anyInt())).thenReturn("encoded");

        // WHEN
        MensagenDto resultado = mensagemService.criar(idRemetenteHash, dto);

        // THEN
        assertNotNull(resultado);
        assertEquals("Olá!", resultado.conteudo());
        // Verifica se o repositório guardou
        verify(mensagenRepository).save(any(Mensagen.class));
        // Verifica se a notificação foi enviada para o destinatário
        verify(notificacoesService).criarNotificacao(eq(idDestReal), eq(idRemReal), anyString(), eq("Olá!"), eq("MENSAGEM"), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar mensagem com destinatário inexistente")
    void criarMensagem_DestinatarioNaoExiste_Erro() {
        // GIVEN
        when(idHasher.decode(anyString())).thenReturn(1);
        when(utilizadoreRepository.findById(1)).thenReturn(Optional.of(new Utilizadore()));
        when(idHasher.decode("invalido")).thenReturn(999);
        when(utilizadoreRepository.findById(999)).thenReturn(Optional.empty());

        MensagemCriarDto dto = new MensagemCriarDto("invalido", "Oi");

        // WHEN & THEN
        assertThrows(EntityNotFoundException.class, () -> mensagemService.criar("rem_hash", dto));
    }

    @Test
    @DisplayName("Deve buscar histórico de conversa entre dois utilizadores")
    void mensagensConversa_Sucesso() throws Exception {
        // GIVEN
        String userHash = "u1";
        String conversaHash = "u2";
        Integer id1 = 1; Integer id2 = 2;

        Utilizadore u1 = new Utilizadore(); u1.setId(id1);
        Mensagen m = new Mensagen(10, u1, new Utilizadore(), "Teste", LocalDateTime.now());
        m.getDestinatario().setId(id2);
        m.getDestinatario().setNome("Outro");

        when(idHasher.decode(userHash)).thenReturn(id1);
        when(idHasher.decode(conversaHash)).thenReturn(id2);
        when(utilizadoreRepository.findById(id1)).thenReturn(Optional.of(u1));
        when(mensagenRepository.findChatHistory(id1, id2)).thenReturn(List.of(m));
        when(idHasher.encode(anyInt())).thenReturn("hash");

        // WHEN
        List<MensagenDto> resultado = mensagemService.mensagensConversa(userHash, conversaHash);

        // THEN
        assertEquals(1, resultado.size());
        assertEquals("Teste", resultado.get(0).conteudo());
        verify(mensagenRepository).findChatHistory(id1, id2);
    }

    @Test
    @DisplayName("Deve remover uma mensagem pelo ID")
    void eliminar_Sucesso() {
        // GIVEN
        String msgHash = "msg123";
        Integer idReal = 123;
        when(idHasher.decode(msgHash)).thenReturn(idReal);

        // WHEN
        mensagemService.eliminar(msgHash);

        // THEN
        verify(mensagenRepository).deleteById(idReal);
    }

    @Test
    @DisplayName("Deve gerar preview de conversa corretamente (identificar o outro utilizador)")
    void converterParaPreviewDto_Sucesso() {
        // GIVEN
        Integer currentUserId = 1;
        Utilizadore eu = new Utilizadore(); eu.setId(currentUserId);
        Utilizadore outro = new Utilizadore(); outro.setId(2); outro.setNome("Maria");

        Mensagen m = new Mensagen(100, eu, outro, "Última msg", LocalDateTime.now());
        when(idHasher.encode(2)).thenReturn("maria_hash");

        // WHEN
        MensagenPreviewDto preview = mensagemService.converterParaPreviewDto(m, currentUserId);

        // THEN
        assertEquals("Maria", preview.nome());
        assertEquals("maria_hash", preview.id());
        assertFalse(preview.isTurma());
    }
}