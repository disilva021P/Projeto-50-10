package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.InventarioAdicionarRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.InventarioDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.InventarioEditarRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EstadoUnidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.InventarioUnidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock private InventarioUnidadeRepository inventarioUnidadeRepository;
    @Mock private ImagensUnidadeRepository imagensUnidadeRepository;
    @Mock private EstadoUnidadeRepository estadoUnidadeRepository;
    @Mock private UtilizadoreRepository utilizadoreRepository;
    @Mock private IdHasher idHasher;

    private InventarioService inventarioService;

    @BeforeEach
    void setUp() {
        inventarioService = new InventarioService(
                inventarioUnidadeRepository,
                imagensUnidadeRepository,
                estadoUnidadeRepository,
                utilizadoreRepository,
                idHasher
        );
    }

    // --- TESTES DE ADIÇÃO ---

    @Test
    @DisplayName("Deve adicionar item ao inventário com estado padrão (9)")
    void adicionarAoInventario_DeveUsarEstadoPadrao9() {
        // GIVEN
        // Mudamos o estadoId para null para testar a lógica do default no Service
        InventarioAdicionarRequest request = new InventarioAdicionarRequest(
                "Projetor Epson", "Modelo X12", null, null, true, "Sala 10", "Verificado"
        );

        EstadoUnidade estadoPadrao = new EstadoUnidade();
        estadoPadrao.setId(9);
        estadoPadrao.setEstado("Inventariado");

        // Agora o GIVEN bate certo com o que o Service vai pedir (findById(9))
        when(estadoUnidadeRepository.findById(9)).thenReturn(Optional.of(estadoPadrao));

        when(inventarioUnidadeRepository.save(any(InventarioUnidade.class))).thenAnswer(i -> {
            InventarioUnidade u = i.getArgument(0);
            u.setId(1);
            u.setEstado(estadoPadrao); // Importante para o toDto não dar erro
            return u;
        });

        // WHEN
        InventarioDto resultado = inventarioService.adicionarAoInventario(request);

        // THEN
        assertNotNull(resultado);
        assertEquals(9, resultado.estadoId()); // Verifica se realmente assumiu o 9
        verify(estadoUnidadeRepository).findById(9);
    }

    @Test
    @DisplayName("Não deve adicionar se o estado for inválido")
    void adicionarAoInventario_DeveLancarExcecao_QuandoEstadoNaoExiste() {
        // GIVEN
        InventarioAdicionarRequest request = new InventarioAdicionarRequest(
                "Item", "Desc", 99, 1, true, "Sala 11", "Verificado"
        );
        when(estadoUnidadeRepository.findById(99)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> inventarioService.adicionarAoInventario(request));
    }

    // --- TESTES DE EDIÇÃO ---

    @Test
    @DisplayName("Deve editar apenas os campos fornecidos na unidade")
    void editarUnidade_DeveAtualizarApenasCamposPreenchidos() {
        // GIVEN
        String hash = "item123";
        Integer idReal = 123;
        InventarioUnidade unidadeExistente = new InventarioUnidade();
        unidadeExistente.setId(idReal);
        unidadeExistente.setNome("Nome Antigo");
        unidadeExistente.setLocalizacao("Armazém A");

        EstadoUnidade estado = new EstadoUnidade(); estado.setId(9);
        unidadeExistente.setEstado(estado);

        // Request apenas com novo nome (outros campos null)
        InventarioEditarRequest request = new InventarioEditarRequest(
                "Nome Novo", null, null, null, null
        );

        when(idHasher.decode(hash)).thenReturn(idReal);
        when(inventarioUnidadeRepository.findById(idReal)).thenReturn(Optional.of(unidadeExistente));
        when(inventarioUnidadeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // WHEN
        InventarioDto resultado = inventarioService.editarUnidade(hash, request);

        // THEN
        assertEquals("Nome Novo", resultado.nomeArtigo());
        // A localização não deve ter mudado para null, deve manter a antiga
        verify(inventarioUnidadeRepository).save(argThat(u -> u.getLocalizacao().equals("Armazém A")));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o hash do ID é inválido")
    void editarUnidade_DeveLancarExcecao_QuandoHashInvalido() {
        // GIVEN
        String hashInvalido = "hash_corrompido";
        // Simulamos que o hasher lança uma exceção ao tentar descodificar algo mal formatado
        when(idHasher.decode(hashInvalido)).thenThrow(new RuntimeException("Invalid hash"));

        // WHEN & THEN
        assertThrows(RuntimeException.class, () ->
                inventarioService.editarUnidade(hashInvalido, new InventarioEditarRequest("Novo", null, null, null, null))
        );
        verifyNoInteractions(inventarioUnidadeRepository);
    }

    // --- TESTES DE REMOÇÃO ---

    @Test
    @DisplayName("Deve remover unidade se ela existir")
    void removerDoInventario_Sucesso() {
        // GIVEN
        String hash = "hash1";
        Integer idReal = 1;
        when(idHasher.decode(hash)).thenReturn(idReal);
        when(inventarioUnidadeRepository.existsById(idReal)).thenReturn(true);

        // WHEN
        inventarioService.removerDoInventario(hash);

        // THEN
        verify(inventarioUnidadeRepository).deleteById(idReal);
    }

    @Test
    @DisplayName("Não deve remover se a unidade não existir")
    void removerDoInventario_DeveLancarExcecao_QuandoInexistente() {
        // GIVEN
        String hash = "nao_existe";
        when(idHasher.decode(hash)).thenReturn(404);
        when(inventarioUnidadeRepository.existsById(404)).thenReturn(false);

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class, () -> inventarioService.removerDoInventario(hash));
        assertEquals("Unidade não encontrada", ex.getMessage());
    }

    // --- TESTES DE FILTRAGEM ---

    @Test
    @DisplayName("Deve retornar página de DTOs ao filtrar")
    void filtrarInventario_Sucesso() {
        // GIVEN
        org.springframework.data.domain.Pageable pageable = mock(org.springframework.data.domain.Pageable.class);
        when(inventarioUnidadeRepository.filtrarInventario(any(), any(), any(), any(), any(), eq(pageable)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        // WHEN
        inventarioService.filtrarInventario("Mesa", null, null, null, null, pageable);

        // THEN
        verify(inventarioUnidadeRepository).filtrarInventario(eq("Mesa"), any(), any(), any(), any(), eq(pageable));
    }

    // --- OUTROS TESTES ---

    @Test
    @DisplayName("Deve garantir que o toDto anula campos de vestuário e imagens")
    void toDto_DeveRetornarCamposNulosParaCompatibilidade() {
        // GIVEN
        String hashValido = "hash500";
        Integer idReal = 500;

        InventarioUnidade unidade = new InventarioUnidade();
        unidade.setId(idReal);
        unidade.setNome("Mesa");

        EstadoUnidade estado = new EstadoUnidade();
        estado.setId(9);
        estado.setEstado("Inventariado");
        unidade.setEstado(estado);

        // CONFIGURAÇÃO DOS MOCKS (Ordem importa)
        when(idHasher.decode(hashValido)).thenReturn(idReal); // Faltava isto!
        when(idHasher.encode(idReal)).thenReturn(hashValido);
        when(inventarioUnidadeRepository.findById(idReal)).thenReturn(Optional.of(unidade));

        // Simular o save para não dar erro no service
        when(inventarioUnidadeRepository.save(any())).thenReturn(unidade);

        // WHEN
        // Passamos um request vazio apenas para disparar a lógica do editar que chama o toDto
        InventarioDto dto = inventarioService.editarUnidade(hashValido, new InventarioEditarRequest(null, null, null, null, null));

        // THEN
        assertEquals(hashValido, dto.id());
        assertNull(dto.tamanho(), "Tamanho deve ser null no inventário");
        assertNull(dto.cor(), "Cor deve ser null no inventário");
        assertTrue(dto.imagemIds().isEmpty(), "Lista de imagens deve vir vazia");
    }
}