package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Inventario;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.InventarioAdicionarRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.InventarioDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.InventarioEditarRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.InventarioUnidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.InventarioUnidadeRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.InventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioControllerTest {

    @Mock
    private InventarioService inventarioService;

    @Mock
    private InventarioUnidadeRepository unidadeRepository;

    @Mock
    private IdHasher idHasher;

    @InjectMocks
    private InventarioController inventarioController;

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

    // ─── listar ───────────────────────────────────────────────────────────────

    @Test
    void listar_comParametrosPadrao_retorna200ComPagina() {
        Page<InventarioDto> page = new PageImpl<>(List.of(inventarioDto));
        when(inventarioService.filtrarInventario(any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        ResponseEntity<Page<InventarioDto>> response = inventarioController.listar(
                0, 12, "criadoEm", "desc",
                null, null, null, null, null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void listar_comFiltrosAplicados_retorna200() {
        Page<InventarioDto> page = new PageImpl<>(List.of(inventarioDto));
        when(inventarioService.filtrarInventario(eq("Cadeira"), eq(1), eq("M"), eq("Castanho"), eq("Bom"), any()))
                .thenReturn(page);

        ResponseEntity<Page<InventarioDto>> response = inventarioController.listar(
                0, 12, "criadoEm", "asc",
                "Cadeira", 1, "M", "Castanho", "Bom"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Cadeira Antiga", response.getBody().getContent().get(0).nomeArtigo());
    }

    @Test
    void listar_comOrdenacaoAscendente_retorna200() {
        Page<InventarioDto> page = new PageImpl<>(List.of(inventarioDto));
        when(inventarioService.filtrarInventario(any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        ResponseEntity<Page<InventarioDto>> response = inventarioController.listar(
                0, 12, "nomeArtigo", "asc",
                null, null, null, null, null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void listar_paginaVazia_retorna200ComPaginaVazia() {
        Page<InventarioDto> page = new PageImpl<>(List.of());
        when(inventarioService.filtrarInventario(any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        ResponseEntity<Page<InventarioDto>> response = inventarioController.listar(
                0, 12, "criadoEm", "desc",
                null, null, null, null, null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getTotalElements());
    }

    // ─── remover ──────────────────────────────────────────────────────────────

    @Test
    void remover_comIdValido_retorna204() {
        doNothing().when(inventarioService).removerDoInventario("hash123");

        ResponseEntity<Void> response = inventarioController.remover("hash123");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(inventarioService).removerDoInventario("hash123");
    }

    @Test
    void remover_comIdInexistente_propagaExcecao() {
        doThrow(new NoSuchElementException("Não encontrado"))
                .when(inventarioService).removerDoInventario("naoExiste");

        assertThrows(NoSuchElementException.class,
                () -> inventarioController.remover("naoExiste"));
    }

    @Test
    void remover_comIdInvalido_propagaExcecao() {
        doThrow(new IllegalArgumentException("ID inválido"))
                .when(inventarioService).removerDoInventario("!!!");

        assertThrows(IllegalArgumentException.class,
                () -> inventarioController.remover("!!!"));
    }

    // ─── editar ───────────────────────────────────────────────────────────────

    @Test
    void editar_comDadosValidos_retorna200ComDtoAtualizado() {
        InventarioEditarRequest request = mock(InventarioEditarRequest.class);
        when(inventarioService.editarUnidade("hash123", request)).thenReturn(inventarioDto);

        ResponseEntity<InventarioDto> response = inventarioController.editar("hash123", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cadeira Antiga", response.getBody().nomeArtigo());
    }

    @Test
    void editar_comIdInexistente_propagaExcecao() {
        InventarioEditarRequest request = mock(InventarioEditarRequest.class);
        when(inventarioService.editarUnidade("naoExiste", request))
                .thenThrow(new NoSuchElementException("Não encontrado"));

        assertThrows(NoSuchElementException.class,
                () -> inventarioController.editar("naoExiste", request));
    }

    @Test
    void editar_comDadosInvalidos_propagaExcecao() {
        InventarioEditarRequest request = mock(InventarioEditarRequest.class);
        when(inventarioService.editarUnidade(eq("hash123"), any()))
                .thenThrow(new IllegalArgumentException("Dados inválidos"));

        assertThrows(IllegalArgumentException.class,
                () -> inventarioController.editar("hash123", request));
    }

    // ─── adicionar ────────────────────────────────────────────────────────────

    @Test
    void adicionar_comDadosValidos_retorna201ComDto() {
        InventarioAdicionarRequest request = mock(InventarioAdicionarRequest.class);
        when(inventarioService.adicionarAoInventario(request)).thenReturn(inventarioDto);

        ResponseEntity<InventarioDto> response = inventarioController.adicionar(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("hash123", response.getBody().id());
    }

    @Test
    void adicionar_comDadosInvalidos_propagaExcecao() {
        InventarioAdicionarRequest request = mock(InventarioAdicionarRequest.class);
        when(inventarioService.adicionarAoInventario(request))
                .thenThrow(new IllegalArgumentException("Dados inválidos"));

        assertThrows(IllegalArgumentException.class,
                () -> inventarioController.adicionar(request));
    }

    @Test
    void adicionar_quandoErroGenerico_propagaExcecao() {
        InventarioAdicionarRequest request = mock(InventarioAdicionarRequest.class);
        when(inventarioService.adicionarAoInventario(request))
                .thenThrow(new RuntimeException("Erro interno"));

        assertThrows(RuntimeException.class,
                () -> inventarioController.adicionar(request));
    }

    // ─── getUnidadesDisponiveis ───────────────────────────────────────────────

    @Test
    void getUnidadesDisponiveis_retorna200ComLista() {
        InventarioUnidade unidade = new InventarioUnidade();
        unidade.setId(1);
        unidade.setNome("Cadeira");
        unidade.setDisponivel(true);
        unidade.setCriadoEm(Instant.now());

        when(unidadeRepository.findAll()).thenReturn(List.of(unidade));

        ResponseEntity<List<InventarioUnidade>> response = inventarioController.getUnidadesDisponiveis();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Cadeira", response.getBody().get(0).getNome());
    }

    @Test
    void getUnidadesDisponiveis_listaVazia_retorna200() {
        when(unidadeRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<InventarioUnidade>> response = inventarioController.getUnidadesDisponiveis();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getUnidadesDisponiveis_quandoErroGenerico_propagaExcecao() {
        when(unidadeRepository.findAll()).thenThrow(new RuntimeException("Erro BD"));

        assertThrows(RuntimeException.class,
                () -> inventarioController.getUnidadesDisponiveis());
    }
}