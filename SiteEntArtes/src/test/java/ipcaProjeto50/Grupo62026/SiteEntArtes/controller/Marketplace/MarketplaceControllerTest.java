package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Marketplace;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ConversaoInventarioRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.ImagensUnidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ImagensUnidadeRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.MarketplaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.Utils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketplaceControllerTest {

    @Mock
    private MarketplaceService marketplaceService;

    @Mock
    private ImagensUnidadeRepository imagensUnidadeRepository;

    @Mock
    private IdHasher idHasher;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MarketplaceController marketplaceController;

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

    // ─── listarArtigos ────────────────────────────────────────────────────────

    @Test
    void listarArtigos_comParametrosPadrao_retorna200ComPagina() {
        Page<ArtigoDto> page = new PageImpl<>(List.of(artigoDto));
        when(marketplaceService.filtrarArtigos(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        ResponseEntity<Page<ArtigoDto>> response = marketplaceController.listarArtigos(
                0, 12, "criadoEm", "desc",
                null, null, null, null, null, null, null, null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void listarArtigos_comFiltrosAplicados_retorna200() {
        Page<ArtigoDto> page = new PageImpl<>(List.of(artigoDto));
        when(marketplaceService.filtrarArtigos(eq("Vestido"), eq(1), eq("M"), eq("Azul"), eq("Bom"),
                eq(10.0), eq(20.0), eq("user1"), any()))
                .thenReturn(page);

        ResponseEntity<Page<ArtigoDto>> response = marketplaceController.listarArtigos(
                0, 12, "criadoEm", "asc",
                "Vestido", 1, "M", "Azul", "Bom", 10.0, 20.0, "user1"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getContent().size());
    }

    @Test
    void listarArtigos_quandoServiceLancaIllegalArgument_retorna400() {
        when(marketplaceService.filtrarArtigos(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Parâmetro inválido"));

        ResponseEntity<Page<ArtigoDto>> response = marketplaceController.listarArtigos(
                0, 12, "criadoEm", "desc",
                null, null, null, null, null, null, null, null
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void listarArtigos_quandoServiceLancaExcecaoGenerica_retorna500() {
        when(marketplaceService.filtrarArtigos(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Erro inesperado"));

        ResponseEntity<Page<ArtigoDto>> response = marketplaceController.listarArtigos(
                0, 12, "criadoEm", "desc",
                null, null, null, null, null, null, null, null
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ─── inserirArtigo ────────────────────────────────────────────────────────

    @Test
    void inserirArtigo_comDadosValidos_retorna201ComArtigo() throws Exception {
        when(authentication.getName()).thenReturn("user@email.com");
        when(marketplaceService.inserirArtigo(any(ArtigoRequest.class), anyList(), eq("user@email.com")))
                .thenReturn(artigoDto);

        MultipartFile imagem = new MockMultipartFile("imagens", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3});

        ResponseEntity<ArtigoDto> response = marketplaceController.inserirArtigo(
                "Vestido Azul", "Vestido em bom estado", "M", "Azul", "Bom",
                true, false, false,
                new BigDecimal("15.00"), null,
                List.of(imagem), authentication
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Vestido Azul", response.getBody().nome());
    }

    @Test
    void inserirArtigo_comDadosInvalidos_retorna400() throws Exception {
        when(authentication.getName()).thenReturn("user@email.com");
        when(marketplaceService.inserirArtigo(any(), anyList(), any()))
                .thenThrow(new IllegalArgumentException("Dados inválidos"));

        MultipartFile imagem = new MockMultipartFile("imagens", "foto.jpg", "image/jpeg", new byte[]{1});

        ResponseEntity<ArtigoDto> response = marketplaceController.inserirArtigo(
                "", "", null, null, null,
                false, false, false, null, null,
                List.of(imagem), authentication
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void inserirArtigo_quandoServiceLancaExcecaoGenerica_retorna500() throws Exception {
        when(authentication.getName()).thenReturn("user@email.com");
        when(marketplaceService.inserirArtigo(any(), anyList(), any()))
                .thenThrow(new RuntimeException("Erro interno"));

        MultipartFile imagem = new MockMultipartFile("imagens", "foto.jpg", "image/jpeg", new byte[]{1});

        ResponseEntity<ArtigoDto> response = marketplaceController.inserirArtigo(
                "Nome", "Desc", null, null, null,
                true, false, false, null, null,
                List.of(imagem), authentication
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ─── getImagem ────────────────────────────────────────────────────────────

    @Test
    void getImagem_comIdValido_retorna200ComBytes() {
        ImagensUnidade imagem = new ImagensUnidade();
        imagem.setId(1);
        imagem.setArtigoId(10);
        imagem.setUrlImagem(new byte[]{10, 20, 30});

        when(idHasher.decode("abc")).thenReturn(1);
        when(imagensUnidadeRepository.findById(1)).thenReturn(Optional.of(imagem));

        ResponseEntity<byte[]> response = marketplaceController.getImagem("abc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(new byte[]{10, 20, 30}, response.getBody());
    }

    @Test
    void getImagem_comIdInexistente_retorna404() {
        when(idHasher.decode("xyz")).thenReturn(99);
        when(imagensUnidadeRepository.findById(99)).thenReturn(Optional.empty());

        ResponseEntity<byte[]> response = marketplaceController.getImagem("xyz");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getImagem_comIdHashInvalido_retorna400() {
        when(idHasher.decode("!!!")).thenThrow(new IllegalArgumentException("Hash inválido"));

        ResponseEntity<byte[]> response = marketplaceController.getImagem("!!!");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getImagem_quandoErroGenerico_retorna500() {
        when(idHasher.decode("abc")).thenReturn(1);
        when(imagensUnidadeRepository.findById(1)).thenThrow(new RuntimeException("Erro BD"));

        ResponseEntity<byte[]> response = marketplaceController.getImagem("abc");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ─── arquivar ─────────────────────────────────────────────────────────────

    @Test
    void arquivar_comIdValido_retorna204() {
        doNothing().when(marketplaceService).arquivarArtigo("abc123");

        ResponseEntity<Void> response = marketplaceController.arquivar("abc123");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(marketplaceService).arquivarArtigo("abc123");
    }

    @Test
    void arquivar_comIdInexistente_retorna404() {
        doThrow(new NoSuchElementException()).when(marketplaceService).arquivarArtigo("naoExiste");

        ResponseEntity<Void> response = marketplaceController.arquivar("naoExiste");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void arquivar_comIdInvalido_retorna400() {
        doThrow(new IllegalArgumentException("ID inválido")).when(marketplaceService).arquivarArtigo("!!!");

        ResponseEntity<Void> response = marketplaceController.arquivar("!!!");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void arquivar_quandoErroGenerico_retorna500() {
        doThrow(new RuntimeException("Erro inesperado")).when(marketplaceService).arquivarArtigo("abc");

        ResponseEntity<Void> response = marketplaceController.arquivar("abc");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ─── apagarImagem ─────────────────────────────────────────────────────────

    @Test
    void apagarImagem_comIdValido_retorna204() {
        doNothing().when(marketplaceService).removerImagem("img1");

        ResponseEntity<Void> response = marketplaceController.apagarImagem("img1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(marketplaceService).removerImagem("img1");
    }

    @Test
    void apagarImagem_comIdInexistente_retorna404() {
        doThrow(new NoSuchElementException()).when(marketplaceService).removerImagem("naoExiste");

        ResponseEntity<Void> response = marketplaceController.apagarImagem("naoExiste");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void apagarImagem_comIdInvalido_retorna400() {
        doThrow(new IllegalArgumentException("ID inválido")).when(marketplaceService).removerImagem("!!!");

        ResponseEntity<Void> response = marketplaceController.apagarImagem("!!!");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void apagarImagem_quandoErroGenerico_retorna500() {
        doThrow(new RuntimeException("Erro interno")).when(marketplaceService).removerImagem("img1");

        ResponseEntity<Void> response = marketplaceController.apagarImagem("img1");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ─── editar ───────────────────────────────────────────────────────────────

    @Test
    void editar_comDadosValidos_retorna200ComArtigoAtualizado() {
        ArtigoRequest request = mock(ArtigoRequest.class);
        when(marketplaceService.editarArtigo("abc123", request)).thenReturn(artigoDto);

        ResponseEntity<ArtigoDto> response = marketplaceController.editar("abc123", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("abc123", response.getBody().id());
    }

    @Test
    void editar_comIdInexistente_retorna404() {
        ArtigoRequest request = mock(ArtigoRequest.class);
        when(marketplaceService.editarArtigo("naoExiste", request))
                .thenThrow(new NoSuchElementException());

        ResponseEntity<ArtigoDto> response = marketplaceController.editar("naoExiste", request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void editar_comDadosInvalidos_retorna400() {
        ArtigoRequest request = mock(ArtigoRequest.class);
        when(marketplaceService.editarArtigo(eq("abc123"), any()))
                .thenThrow(new IllegalArgumentException("Dados inválidos"));

        ResponseEntity<ArtigoDto> response = marketplaceController.editar("abc123", request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void editar_quandoErroGenerico_retorna500() {
        ArtigoRequest request = mock(ArtigoRequest.class);
        when(marketplaceService.editarArtigo(eq("abc123"), any()))
                .thenThrow(new RuntimeException("Erro interno"));

        ResponseEntity<ArtigoDto> response = marketplaceController.editar("abc123", request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ─── alterarEstado ────────────────────────────────────────────────────────

    @Test
    void alterarEstado_comDadosValidos_retorna200() throws Exception {
        doNothing().when(marketplaceService).alterarEstadoArtigo("abc123", 2);

        ResponseEntity<Void> response = marketplaceController.alterarEstado("abc123", 2);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(marketplaceService).alterarEstadoArtigo("abc123", 2);
    }

    @Test
    void alterarEstado_comIdInexistente_retorna404() throws Exception {
        doThrow(new NoSuchElementException()).when(marketplaceService).alterarEstadoArtigo("naoExiste", 2);

        ResponseEntity<Void> response = marketplaceController.alterarEstado("naoExiste", 2);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void alterarEstado_comEstadoInvalido_retorna400() throws Exception {
        doThrow(new IllegalArgumentException("Estado inválido"))
                .when(marketplaceService).alterarEstadoArtigo("abc123", 99);

        ResponseEntity<Void> response = marketplaceController.alterarEstado("abc123", 99);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void alterarEstado_quandoErroGenerico_retorna500() throws Exception {
        doThrow(new RuntimeException("Erro interno")).when(marketplaceService).alterarEstadoArtigo("abc123", 2);

        ResponseEntity<Void> response = marketplaceController.alterarEstado("abc123", 2);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ─── importarDoInventario ─────────────────────────────────────────────────

    @Test
    void importarDoInventario_comDadosValidos_retorna200() {
        ConversaoInventarioRequest request = mock(ConversaoInventarioRequest.class);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("coordenador@email.com");
            doNothing().when(marketplaceService).converterUnidadeParaMarketplace(eq(request), eq("coordenador@email.com"));

            ResponseEntity<String> response = marketplaceController.importarDoInventario(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Artigo importado com sucesso!", response.getBody());
        }
    }

    @Test
    void importarDoInventario_comUnidadeInexistente_retorna404() {
        ConversaoInventarioRequest request = mock(ConversaoInventarioRequest.class);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("coordenador@email.com");
            doThrow(new NoSuchElementException("Não encontrada"))
                    .when(marketplaceService).converterUnidadeParaMarketplace(any(), any());

            ResponseEntity<String> response = marketplaceController.importarDoInventario(request);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertTrue(response.getBody().contains("não encontrada"));
        }
    }

    @Test
    void importarDoInventario_comDadosInvalidos_retorna400() {
        ConversaoInventarioRequest request = mock(ConversaoInventarioRequest.class);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("coordenador@email.com");
            doThrow(new IllegalArgumentException("Campo obrigatório em falta"))
                    .when(marketplaceService).converterUnidadeParaMarketplace(any(), any());

            ResponseEntity<String> response = marketplaceController.importarDoInventario(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().contains("Pedido inválido"));
        }
    }

    @Test
    void importarDoInventario_quandoErroGenerico_retorna500() {
        ConversaoInventarioRequest request = mock(ConversaoInventarioRequest.class);

        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("coordenador@email.com");
            doThrow(new RuntimeException("Erro interno"))
                    .when(marketplaceService).converterUnidadeParaMarketplace(any(), any());

            ResponseEntity<String> response = marketplaceController.importarDoInventario(request);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertTrue(response.getBody().contains("Erro interno"));
        }
    }
}