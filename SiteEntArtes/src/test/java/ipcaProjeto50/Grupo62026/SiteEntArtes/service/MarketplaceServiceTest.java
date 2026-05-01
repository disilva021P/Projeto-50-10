package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketplaceServiceTest {

    @Mock private ArtigoRepository artigoRepository;
    @Mock private InventarioUnidadeRepository unidadeRepository;
    @Mock private InventarioUnidadeRepository inventarioUnidadeRepository;
    @Mock private UtilizadoreRepository utilizadoreRepository;
    @Mock private ImagensUnidadeRepository imagensUnidadeRepository;
    @Mock private IdHasher idHasher;
    @Mock private EntityManager entityManager;
    @Mock private NotificacoesService notificacoesService;

    @InjectMocks
    private MarketplaceService marketplaceService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        // Instanciação manual para garantir que os Mocks vão para os lugares certos
        marketplaceService = new MarketplaceService(
                artigoRepository,
                unidadeRepository,
                utilizadoreRepository,
                inventarioUnidadeRepository, // O 4º parâmetro é o que falha
                imagensUnidadeRepository,
                idHasher,
                entityManager,
                notificacoesService
        );
    }

    // --- TESTES DE ALTERAR ESTADO (CENÁRIOS POSITIVOS) ---

    @Test
    @DisplayName("Deve aprovar artigo para o marketplace público (Estado 2)")
    void alterarEstadoArtigo_DeveAprovarParaMarketplace() throws Exception {
        // GIVEN
        String idHash = "art123";
        Integer idReal = 1;
        Artigo artigo = new Artigo();
        artigo.setId(idReal);
        artigo.setNome("Cadeira Antiga");
        Utilizadore dono = new Utilizadore();
        dono.setId(10);
        artigo.setDonoUtilizador(dono);

        when(idHasher.decode(idHash)).thenReturn(idReal);
        when(artigoRepository.findById(idReal)).thenReturn(Optional.of(artigo));

        // WHEN
        marketplaceService.alterarEstadoArtigo(idHash, 2);

        // THEN
        assertTrue(artigo.getAprovado());
        assertFalse(artigo.getArquivado());
        verify(artigoRepository).save(artigo);
        verify(notificacoesService).criarNotificacao(eq(10), any(), anyString(), contains("aprovado"), eq("MARKETPLACE_STATUS"), anyString());
    }

    @Test
    @DisplayName("Deve converter artigo para inventário interno da escola (Estado 9)")
    void alterarEstadoArtigo_DeveMoverParaInventarioInterno() throws Exception {
        // GIVEN
        String idHash = "art999";
        Integer idReal = 999;

        Artigo artigo = new Artigo();
        artigo.setId(idReal);
        artigo.setNome("Mesa");
        artigo.setDescricao("Desc");
        Utilizadore dono = new Utilizadore();
        dono.setId(10);
        artigo.setDonoUtilizador(dono);

        // Configuramos os mocks necessários para o fluxo não interromper
        when(idHasher.decode(idHash)).thenReturn(idReal);
        when(artigoRepository.findById(idReal)).thenReturn(Optional.of(artigo));

        // Mock do EntityManager para retornar um objeto EstadoUnidade e não dar NullPointer
        EstadoUnidade mockEstado = mock(EstadoUnidade.class);
        when(entityManager.getReference(eq(EstadoUnidade.class), anyInt())).thenReturn(mockEstado);

        // WHEN
        marketplaceService.alterarEstadoArtigo(idHash, 9);

        // THEN
        assertTrue(artigo.getArquivado(), "O artigo deve ser arquivado no marketplace");

        // VERIFICAÇÃO CRÍTICA: Garantimos que o save foi chamado no repositório certo
        verify(inventarioUnidadeRepository, times(1)).save(any(InventarioUnidade.class));

        // Opcional: verificar se o nome da unidade criada é o mesmo do artigo
        verify(inventarioUnidadeRepository).save(argThat(unidade -> unidade.getNome().equals("Mesa")));
    }

    @Test
    @DisplayName("Deve listar artigos pendentes convertendo-os para DTO")
    void listarArtigosPendentes_DeveRetornarListaDeDtos() {
        // GIVEN
        Artigo p1 = new Artigo(); p1.setId(1); p1.setNome("Pendente 1");
        Utilizadore dono = new Utilizadore(); dono.setId(10); p1.setDonoUtilizador(dono);

        when(artigoRepository.findPendentesParaCoordenacao()).thenReturn(List.of(p1));
        when(idHasher.encode(anyInt())).thenReturn("hash");

        // WHEN
        List<ArtigoDto> resultado = marketplaceService.listarArtigosPendentes();

        // THEN
        assertFalse(resultado.isEmpty());
        assertEquals("Pendente 1", resultado.get(0).nome());
        verify(artigoRepository).findPendentesParaCoordenacao();
    }

    // --- TESTES DE INSERÇÃO ---

    @Test
    @DisplayName("Deve inserir artigo de doação como não aprovado e notificar coordenadores")
    void inserirArtigo_Doacao_DeveFicarPendente() throws Exception {
        // GIVEN
        ArtigoRequest request = new ArtigoRequest(
                "Teste", "Desc", "M", "Branco", "Novo",
                false, false, true, BigDecimal.TEN, BigDecimal.ZERO, null
        );

        Utilizadore dono = new Utilizadore();
        dono.setId(5);
        dono.setNome("João");

        // Precisamos de pelo menos um coordenador para a notificação ser enviada!
        Utilizadore coord = new Utilizadore();
        coord.setId(1);
        when(utilizadoreRepository.findByTipo_TipoUtilizador("COORDENACAO")).thenReturn(List.of(coord));

        when(utilizadoreRepository.findByEmail(anyString())).thenReturn(Optional.of(dono));
        when(idHasher.encode(anyInt())).thenReturn("hash_artigo");

        when(artigoRepository.save(any(Artigo.class))).thenAnswer(i -> {
            Artigo a = i.getArgument(0);
            a.setId(100);
            a.setDonoUtilizador(dono);
            return a;
        });

        // WHEN
        marketplaceService.inserirArtigo(request, null, "joao@teste.com");

        // THEN
        // Verificamos se foi guardado com aprovado = false (regra das doações)
        verify(artigoRepository).save(argThat(art -> !art.getAprovado()));

        // Verificamos se a notificação foi enviada (agora a lista de coords não está vazia)
        verify(notificacoesService, atLeastOnce()).criarNotificacao(
                any(), any(), anyString(), anyString(), eq("MARKETPLACE_PENDENTE"), anyString()
        );
    }

    @Test
    @DisplayName("Deve inserir artigo de venda como aprovado automaticamente")
    void inserirArtigo_Venda_DeveFicarAprovado() throws Exception {
        // GIVEN
        ArtigoRequest request = createMockRequest(false); // isDoacao = false
        Utilizadore dono = new Utilizadore();
        dono.setId(5);

        when(utilizadoreRepository.findByEmail(anyString())).thenReturn(Optional.of(dono));
        when(artigoRepository.save(any(Artigo.class))).thenAnswer(i -> i.getArgument(0));

        // WHEN
        marketplaceService.inserirArtigo(request, null, "vendedor@teste.com");

        // THEN
        verify(artigoRepository).save(argThat(Artigo::getAprovado));
    }

    @Test
    @DisplayName("Deve guardar imagens associadas ao artigo ao inserir")
    void inserirArtigo_ComImagens_DeveGuardarCadaUma() throws Exception {
        // GIVEN
        ArtigoRequest request = createMockRequest(false);
        MultipartFile mockImg = mock(MultipartFile.class);
        when(mockImg.isEmpty()).thenReturn(false);
        when(mockImg.getBytes()).thenReturn(new byte[]{1, 2, 3});

        Utilizadore dono = new Utilizadore(); dono.setId(1);
        when(utilizadoreRepository.findByEmail(any())).thenReturn(Optional.of(dono));
        when(artigoRepository.save(any())).thenAnswer(i -> {
            Artigo a = i.getArgument(0);
            a.setId(1);
            return a;
        });

        // WHEN
        marketplaceService.inserirArtigo(request, List.of(mockImg, mockImg), "user@teste.com");

        // THEN
        // Verificamos se o repositório de imagens foi chamado 2 vezes
        verify(imagensUnidadeRepository, times(2)).save(any(ImagensUnidade.class));
    }

    // --- TESTES NEGATIVOS ---

    @Test
    @DisplayName("Não deve remover imagem se o ID for inválido")
    void removerImagem_DeveLancarExcecao_QuandoIdInexistente() {
        // GIVEN
        String hash = "img_errada";
        when(idHasher.decode(hash)).thenReturn(404);
        doThrow(new RuntimeException("Erro ao apagar")).when(imagensUnidadeRepository).deleteById(404);

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> marketplaceService.removerImagem(hash));
    }

    @Test
    @DisplayName("arquivarArtigo deve lançar exceção se artigo não existir")
    void arquivarArtigo_Negativo() {
        when(idHasher.decode(anyString())).thenReturn(1);
        when(artigoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> marketplaceService.arquivarArtigo("hash"));
    }

    // --- TESTES DE FILTRAGEM ---

    @Test
    @DisplayName("Deve filtrar artigos corretamente descodificando o donoId")
    void filtrarArtigos_DeveChamarRepositoryComDadosCorretos() {
        // GIVEN
        String donoHash = "user_hash";
        Integer donoIdReal = 50;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        when(idHasher.decode(donoHash)).thenReturn(donoIdReal);
        // Mock do retorno do repository (Page vazia para simplificar)
        when(artigoRepository.filtrarMarketplace(any(), any(), any(), any(), any(), any(), any(), eq(donoIdReal), eq(pageable)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        // WHEN
        marketplaceService.filtrarArtigos("Nome", 1, "M", "Azul", "Novo", 10.0, 100.0, donoHash, pageable);

        // THEN
        verify(idHasher).decode(donoHash);
        verify(artigoRepository).filtrarMarketplace(eq("Nome"), eq(1), eq("M"), eq("Azul"), eq("Novo"), eq(10.0), eq(100.0), eq(donoIdReal), eq(pageable));
    }

    // --- TESTES DE IMAGEM ---

    @Test
    @DisplayName("Deve remover imagem com sucesso descodificando o ID")
    void removerImagem_Sucesso() {
        // GIVEN
        String imgHash = "img_hash";
        Integer imgIdReal = 101;
        when(idHasher.decode(imgHash)).thenReturn(imgIdReal);

        // WHEN
        marketplaceService.removerImagem(imgHash);

        // THEN
        verify(idHasher).decode(imgHash);
        verify(imagensUnidadeRepository).deleteById(imgIdReal);
    }


    // --- HELPER METHODS ---

    private ArtigoRequest createMockRequest(boolean isDoacao) {
        return new ArtigoRequest(
                "Teste", "Desc", "M", "Branco", "Novo",
                !isDoacao, false, isDoacao,
                BigDecimal.TEN, BigDecimal.ZERO, null
        );
    }
}