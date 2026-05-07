package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.PagamentoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Pagamento;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TipoPagamento;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock private PagamentoRepository pagamentoRepository;
    @Mock private IdHasher idHasher;
    @Mock private UtilizadoreRepository utilizadoreRepository;
    @Mock private TipoPagamentoRepository tipoPagamentoRepository;
    @Mock private AulaRepository aulaRepository;

    @InjectMocks
    private PagamentoService pagamentoService;

    private Pagamento pagamentoEntity;
    private Utilizadore utilizador;
    private TipoPagamento tipoPagamento;

    @BeforeEach
    void setUp() {
        utilizador = new Utilizadore();
        utilizador.setId(1);
        utilizador.setNome("João Silva");

        tipoPagamento = new TipoPagamento();
        tipoPagamento.setId(1);
        tipoPagamento.setTipoPagamento("Mensalidade");

        pagamentoEntity = new Pagamento();
        pagamentoEntity.setId(100);
        pagamentoEntity.setValorPagamento(new BigDecimal("50.00"));
        pagamentoEntity.setIdutilizador(utilizador);
        pagamentoEntity.setIdTipoPagamento(tipoPagamento);
        pagamentoEntity.setPago(false);
    }

    @Test
    @DisplayName("Deve criar um pagamento com sucesso")
    void criarPagamentoSucesso() throws Exception {
        // Arrange
        UtilizadoreResumoDto resumoDto = new UtilizadoreResumoDto("userHashed", "João Silva");
        PagamentoDto inputDto = new PagamentoDto(
                null, new BigDecimal("50.00"), false, "Mensalidade Jan",
                "tipoHashed", "Mensalidade", null, LocalDate.now().plusDays(1), null, resumoDto
        );

        when(idHasher.decode("userHashed")).thenReturn(1);
        when(idHasher.decode("tipoHashed")).thenReturn(1);
        when(utilizadoreRepository.findById(1)).thenReturn(Optional.of(utilizador));
        when(tipoPagamentoRepository.findById(1)).thenReturn(Optional.of(tipoPagamento));
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(pagamentoEntity);
        when(idHasher.encode(anyInt())).thenReturn("hashedID");

        // Act
        PagamentoDto resultado = pagamentoService.criar(inputDto);

        // Assert
        assertNotNull(resultado);
        assertEquals(new BigDecimal("50.00"), resultado.valorPagamento());
        verify(pagamentoRepository, times(1)).save(any(Pagamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar pagamento com valor negativo")
    void criarPagamentoValorNegativo() {
        PagamentoDto inputDto = new PagamentoDto(
                null, new BigDecimal("-10.00"), false, "Erro",
                "tipo", "tipo", null, LocalDate.now(), null, null
        );

        Exception exception = assertThrows(Exception.class, () -> pagamentoService.criar(inputDto));
        assertEquals("Valor não pode ser 0 ou menor que 0", exception.getMessage());
    }

    @Test
    @DisplayName("Deve confirmar um pagamento alterando o estado para pago")
    void confirmarPagamentoSucesso() throws Exception {
        // Arrange
        when(idHasher.decode("pagHashed")).thenReturn(100);
        when(pagamentoRepository.findById(100)).thenReturn(Optional.of(pagamentoEntity));
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(pagamentoEntity);
        when(idHasher.encode(anyInt())).thenReturn("pagHashed");

        // Act
        PagamentoDto resultado = pagamentoService.confirmar("pagHashed");

        // Assert
        assertTrue(pagamentoEntity.getPago());
        assertNotNull(pagamentoEntity.getDataConfirmado());
        verify(pagamentoRepository).save(pagamentoEntity);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar confirmar pagamento inexistente")
    void confirmarPagamentoInexistente() {
        when(idHasher.decode("erro")).thenReturn(999);
        when(pagamentoRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> pagamentoService.confirmar("erro"));
    }

    @Test
    @DisplayName("Deve gerar CSV corretamente formatado")
    void escreverPagamentosCsvSucesso() {
        // Arrange
        UtilizadoreResumoDto resumo = new UtilizadoreResumoDto("id", "Aluno Teste");
        PagamentoDto dto = new PagamentoDto(
                "id", new BigDecimal("30.00"), true, "Teste CSV",
                "tipo", "tipo", null, LocalDate.of(2024, 1, 1), null, resumo
        );

        // Act
        String csv = pagamentoService.escreverPagamentosCsv(java.util.List.of(dto));

        // Assert
        assertTrue(csv.contains("sep=;"));
        assertTrue(csv.contains("Aluno Teste"));
        assertTrue(csv.contains("30.00"));
        assertTrue(csv.contains("Pago"));
    }

    @Test
    @DisplayName("Deve eliminar pagamento chamando o repositório")
    void eliminarPagamentoSucesso() {
        // Arrange
        when(idHasher.decode("idHashed")).thenReturn(100);

        // Act
        pagamentoService.eliminar("idHashed");

        // Assert
        verify(pagamentoRepository, times(1)).deleteById(100);
    }
}