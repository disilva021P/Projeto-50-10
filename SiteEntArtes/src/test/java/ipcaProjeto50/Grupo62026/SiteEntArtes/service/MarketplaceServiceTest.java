package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ConversaoInventarioRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Artigo;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ArtigoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.InventarioUnidadeRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketplaceServiceTest {

    @Mock
    private UtilizadoreRepository utilizadoreRepository;

    @Mock
    private InventarioUnidadeRepository inventarioUnidadeRepository;

    @Mock
    private ArtigoRepository artigoRepository;

    @Mock
    private IdHasher idHasher;

    @InjectMocks
    private MarketplaceService marketplaceService;

    @Test
    @DisplayName("Não deve alterar estado se o artigo não existir")
    void alterarEstadoArtigo_DeveLancarExcecao_QuandoArtigoInexistente() {
        // GIVEN
        String idHash = "hash_invalido";
        Integer idReal = 123;

        // Configuramos o mock para simular o comportamento
        when(idHasher.decode(idHash)).thenReturn(idReal);
        // Simulamos que o repositório retorna Vazio (Optional.empty())
        when(artigoRepository.findById(idReal)).thenReturn(Optional.empty());

        // WHEN & THEN (Quando tentamos executar, ele deve lançar a RuntimeException)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            marketplaceService.alterarEstadoArtigo(idHash, 5);
        });

        // Verificamos se a mensagem de erro é a que definiste no Service
        assertEquals("Artigo não encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Não deve inserir artigo se o utilizador não for encontrado")
    void inserirArtigo_DeveLancarExcecao_QuandoUtilizadorNaoExiste() {
        // GIVEN
        String emailInexistente = "naoexiste@escola.pt";
        ArtigoRequest request = new ArtigoRequest(
                "Cadeira", "Desc", "M", "Preto", "Novo",
                true, false, false, BigDecimal.valueOf(10.0), BigDecimal.valueOf(0.0), null
        );

        // Simulamos que o repositório de utilizadores retorna vazio
        when(utilizadoreRepository.findByEmail(emailInexistente)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            marketplaceService.inserirArtigo(request, null, emailInexistente);
        });

        assertEquals("Utilizador nao encontrado: " + emailInexistente, exception.getMessage());
    }

    @Test
    @DisplayName("Não deve editar artigo se houver erro ao processar a imagem")
    void editarArtigo_DeveLancarExcecao_QuandoErroNaImagem() throws IOException {
        // GIVEN
        String idHash = "artigo123";
        Integer idReal = 1;
        Artigo artigoExistente = new Artigo();
        artigoExistente.setId(idReal);

        // Criamos um mock de um ficheiro que lança erro ao ler os bytes
        MultipartFile imagemComErro = mock(MultipartFile.class);
        when(imagemComErro.isEmpty()).thenReturn(false);
        when(imagemComErro.getBytes()).thenThrow(new IOException("Erro de leitura"));

        ArtigoRequest request = new ArtigoRequest(
                "Nome", "Desc", "L", "Azul", "Usado",
                false, false, true, BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.0), List.of(imagemComErro)
        );

        when(idHasher.decode(idHash)).thenReturn(idReal);
        when(artigoRepository.findById(idReal)).thenReturn(Optional.of(artigoExistente));

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            marketplaceService.editarArtigo(idHash, request);
        });

        assertEquals("Erro ao processar imagem", exception.getMessage());
    }

    @Test
    @DisplayName("Não deve converter unidade se o item de inventário não existir")
    void converterUnidadeParaMarketplace_DeveLancarExcecao_QuandoUnidadeInexistente() {
        // GIVEN
        Integer unidadeIdInexistente = 999;
        ConversaoInventarioRequest request = new ConversaoInventarioRequest();
        request.setUnidadeId(unidadeIdInexistente);

        // Simulamos que o repositório de unidades retorna vazio
        when(inventarioUnidadeRepository.findById(unidadeIdInexistente)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            marketplaceService.converterUnidadeParaMarketplace(request, 1);
        });

        assertEquals("Item de inventário não encontrado.", exception.getMessage());
    }

}