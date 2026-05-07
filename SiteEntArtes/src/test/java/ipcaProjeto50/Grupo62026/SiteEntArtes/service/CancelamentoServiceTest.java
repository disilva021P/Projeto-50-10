package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelamentoServiceTest {

    @Mock private CancelamentoRepository cancelamentoRepository;
    @Mock private AulaRepository aulaRepository;
    @Mock private UtilizadoreRepository utilizadoreRepository;
    @Mock private IdHasher idHasher;
    @Mock private EncarregadoAlunoRepository encarregadoAlunoRepository;
    @Mock private NotificacoesService notificacoesService;

    @InjectMocks
    private CancelamentoService cancelamentoService;

    private Aula aula;
    private Utilizadore aluno;
    private Utilizadore professor;
    private Cancelamento cancelamento;
    private FaltaDto faltaDto;

    @BeforeEach
    void setUp() {
        aula = new Aula();
        aula.setId(1);
        aula.setDataAula(java.time.LocalDate.now());
        aula.setHoraInicio(java.time.LocalTime.of(9, 0));
        aula.setHoraFim(java.time.LocalTime.of(10, 0));

        aluno = new Utilizadore();
        aluno.setId(10);
        aluno.setNome("Aluno Teste");
        aluno.setAtivo(true);

        professor = new Utilizadore();
        professor.setId(20);
        professor.setNome("Prof Teste");
        professor.setAtivo(true);

        cancelamento = new Cancelamento();
        cancelamento.setId(1);
        cancelamento.setAula(aula);
        cancelamento.setUtilizador(aluno);
        cancelamento.setMarcardo_por(professor);
        cancelamento.setJustificado(false);
        cancelamento.setMotivo(null);
        cancelamento.setCriadoEm(LocalDateTime.now());

        faltaDto = new FaltaDto("hashFalta", "hashAula", "hashAluno", false, null, "PENDENTE");
    }

    // ─── marcarFalta ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve marcar falta com sucesso e retornar DTO com hashes")
    void marcarFalta_comDadosValidos_deveRetornarDto() throws Exception {
        // GIVEN
        when(idHasher.decode("hashAula")).thenReturn(1);
        when(idHasher.decode("hashAluno")).thenReturn(10);
        when(idHasher.decode("hashProf")).thenReturn(20);
        when(aulaRepository.findById(1)).thenReturn(Optional.of(aula));
        when(utilizadoreRepository.findById(10)).thenReturn(Optional.of(aluno));
        when(utilizadoreRepository.findById(20)).thenReturn(Optional.of(professor));
        when(cancelamentoRepository.save(any(Cancelamento.class))).thenReturn(cancelamento);
        when(idHasher.encode(1)).thenReturn("hashFalta");
        when(idHasher.encode(10)).thenReturn("hashAluno");

        // WHEN
        FaltaDto resultado = cancelamentoService.marcarFalta(faltaDto, "hashProf");

        // THEN
        assertNotNull(resultado);
        assertEquals("hashFalta", resultado.id());
        assertFalse(resultado.justificado());
        assertEquals("PENDENTE", resultado.estado());
        verify(cancelamentoRepository).save(any(Cancelamento.class));
        verify(notificacoesService).criarNotificacao(
                eq(10), eq(20), anyString(), anyString(), eq("FALTA"), anyString()
        );
    }

    @Test
    @DisplayName("Deve lançar excepção se aula não existir ao marcar falta")
    void marcarFalta_aulaInexistente_deveLancarExcecao() {
        // GIVEN
        when(idHasher.decode("hashAula")).thenReturn(1);
        when(idHasher.decode("hashAluno")).thenReturn(10);
        when(aulaRepository.findById(1)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(Exception.class,
                () -> cancelamentoService.marcarFalta(faltaDto, "hashProf"));
        verify(cancelamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar excepção se utilizador não existir ao marcar falta")
    void marcarFalta_utilizadorInexistente_deveLancarExcecao() {
        // GIVEN
        when(idHasher.decode("hashAula")).thenReturn(1);
        when(idHasher.decode("hashAluno")).thenReturn(10);
        when(aulaRepository.findById(1)).thenReturn(Optional.of(aula));
        when(utilizadoreRepository.findById(10)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(Exception.class,
                () -> cancelamentoService.marcarFalta(faltaDto, "hashProf"));
        verify(cancelamentoRepository, never()).save(any());
    }

    // ─── removerFalta ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve remover falta com sucesso quando existe")
    void removerFalta_existente_deveRemover() throws Exception {
        // GIVEN
        when(idHasher.decode("hashFalta")).thenReturn(1);
        when(cancelamentoRepository.existsById(1)).thenReturn(true);

        // WHEN
        cancelamentoService.removerFalta("hashFalta");

        // THEN
        verify(cancelamentoRepository).deleteById(1);
    }

    @Test
    @DisplayName("Deve lançar excepção ao remover falta inexistente")
    void removerFalta_inexistente_deveLancarExcecao() {
        // GIVEN
        when(idHasher.decode("hashFalta")).thenReturn(1);
        when(cancelamentoRepository.existsById(1)).thenReturn(false);

        // WHEN & THEN
        assertThrows(Exception.class,
                () -> cancelamentoService.removerFalta("hashFalta"));
        verify(cancelamentoRepository, never()).deleteById(any());
    }

    // ─── listarTodas ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve listar todas as faltas convertendo para DTO")
    void listarTodas_deveRetornarListaDeDtos() {
        // GIVEN
        when(cancelamentoRepository.findAll()).thenReturn(List.of(cancelamento));
        when(idHasher.encode(1)).thenReturn("hashFalta");
        when(idHasher.encode(10)).thenReturn("hashAluno");

        // WHEN
        List<FaltaDto> resultado = cancelamentoService.listarTodas();

        // THEN
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals("PENDENTE", resultado.get(0).estado());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há faltas")
    void listarTodas_semFaltas_deveRetornarListaVazia() {
        // GIVEN
        when(cancelamentoRepository.findAll()).thenReturn(List.of());

        // WHEN
        List<FaltaDto> resultado = cancelamentoService.listarTodas();

        // THEN
        assertTrue(resultado.isEmpty());
    }

    // ─── listarPendentes ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve listar apenas faltas pendentes")
    void listarPendentes_deveRetornarApenasNaoJustificadas() {
        // GIVEN
        when(cancelamentoRepository.findByJustificadoFalseAndJustificadoEmNull())
                .thenReturn(List.of(cancelamento));
        when(idHasher.encode(1)).thenReturn("hashFalta");
        when(idHasher.encode(10)).thenReturn("hashAluno");

        // WHEN
        List<FaltaDto> resultado = cancelamentoService.listarPendentes();

        // THEN
        assertEquals(1, resultado.size());
        assertEquals("PENDENTE", resultado.get(0).estado());
    }

    // ─── listarFaltasPorUtilizador ────────────────────────────────────────────

    @Test
    @DisplayName("Deve listar faltas de utilizador específico com estado PENDENTE")
    void listarFaltasPorUtilizador_pendente_deveRetornarEstadoCorreto() {
        // GIVEN — justificado=false, justificadoEm=null → PENDENTE
        when(idHasher.decode("hashAluno")).thenReturn(10);
        when(cancelamentoRepository.findAllByUtilizador_Id(10)).thenReturn(List.of(cancelamento));
        when(idHasher.encode(1)).thenReturn("hashFalta");
        when(idHasher.encode(10)).thenReturn("hashAluno");

        // WHEN
        List<FaltaDto> resultado = cancelamentoService.listarFaltasPorUtilizador("hashAluno");

        // THEN
        assertEquals(1, resultado.size());
        assertEquals("PENDENTE", resultado.get(0).estado());
    }

    @Test
    @DisplayName("Deve listar faltas de utilizador com estado APROVADA")
    void listarFaltasPorUtilizador_aprovada_deveRetornarEstadoCorreto() {
        // GIVEN — justificado=true → APROVADA
        cancelamento.setJustificado(true);
        when(idHasher.decode("hashAluno")).thenReturn(10);
        when(cancelamentoRepository.findAllByUtilizador_Id(10)).thenReturn(List.of(cancelamento));
        when(idHasher.encode(1)).thenReturn("hashFalta");
        when(idHasher.encode(10)).thenReturn("hashAluno");

        // WHEN
        List<FaltaDto> resultado = cancelamentoService.listarFaltasPorUtilizador("hashAluno");

        // THEN
        assertEquals("APROVADA", resultado.get(0).estado());
    }

    @Test
    @DisplayName("Deve listar faltas de utilizador com estado INJUSTIFICADA")
    void listarFaltasPorUtilizador_injustificada_deveRetornarEstadoCorreto() {
        // GIVEN — justificado=false, justificadoEm=data → INJUSTIFICADA
        cancelamento.setJustificado(false);
        cancelamento.setJustificadoEm(Instant.now());
        when(idHasher.decode("hashAluno")).thenReturn(10);
        when(cancelamentoRepository.findAllByUtilizador_Id(10)).thenReturn(List.of(cancelamento));
        when(idHasher.encode(1)).thenReturn("hashFalta");
        when(idHasher.encode(10)).thenReturn("hashAluno");

        // WHEN
        List<FaltaDto> resultado = cancelamentoService.listarFaltasPorUtilizador("hashAluno");

        // THEN
        assertEquals("INJUSTIFICADA", resultado.get(0).estado());
    }

    // ─── obterResumoEstatisticas ──────────────────────────────────────────────

    @Test
    @DisplayName("Deve retornar resumo de estatísticas correcto para um aluno")
    void obterResumoEstatisticas_deveRetornarContagens() {
        // GIVEN
        when(idHasher.decode("hashAluno")).thenReturn(10);
        when(cancelamentoRepository.countTotalFaltas(10)).thenReturn(10L);
        when(cancelamentoRepository.countJustificadas(10)).thenReturn(3L);
        when(cancelamentoRepository.countPendentes(10)).thenReturn(4L);
        when(cancelamentoRepository.countNaoJustificadas(10)).thenReturn(3L);

        // WHEN
        FaltaResumoDto resultado = cancelamentoService.obterResumoEstatisticas("hashAluno");

        // THEN
        assertEquals(10L, resultado.total());
        assertEquals(3L, resultado.justificadas());
        assertEquals(4L, resultado.pendentes());
        assertEquals(3L, resultado.injustificadas());
    }

    // ─── atualizarFalta ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve atualizar falta com novos dados de aula e utilizador")
    void atualizarFalta_comDadosValidos_deveAtualizar() throws Exception {
        // GIVEN
        FaltaDto novosDados = new FaltaDto(null, "hashAula2", "hashAluno2", true, "Doença", null);
        Aula novaAula = new Aula(); novaAula.setId(2);
        Utilizadore novoAluno = new Utilizadore(); novoAluno.setId(11);

        when(idHasher.decode("hashFalta")).thenReturn(1);
        when(idHasher.decode("hashAula2")).thenReturn(2);
        when(idHasher.decode("hashAluno2")).thenReturn(11);
        when(cancelamentoRepository.findById(1)).thenReturn(Optional.of(cancelamento));
        when(aulaRepository.findById(2)).thenReturn(Optional.of(novaAula));
        when(utilizadoreRepository.findById(11)).thenReturn(Optional.of(novoAluno));
        when(cancelamentoRepository.save(any())).thenReturn(cancelamento);
        when(idHasher.encode(anyInt())).thenReturn("hash");

        // WHEN
        FaltaDto resultado = cancelamentoService.atualizarFalta("hashFalta", novosDados);

        // THEN
        assertNotNull(resultado);
        verify(cancelamentoRepository).save(argThat(c ->
                c.getMotivo().equals("Doença") && c.getJustificadoEm() != null
        ));
    }

    @Test
    @DisplayName("Deve lançar excepção ao atualizar falta inexistente")
    void atualizarFalta_inexistente_deveLancarExcecao() {
        // GIVEN
        when(idHasher.decode("hashFalta")).thenReturn(1);
        when(cancelamentoRepository.findById(1)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(Exception.class,
                () -> cancelamentoService.atualizarFalta("hashFalta", faltaDto));
        verify(cancelamentoRepository, never()).save(any());
    }

    // ─── listarFaltasDosEducandos ─────────────────────────────────────────────

    @Test
    @DisplayName("Deve retornar lista vazia quando encarregado não tem educandos")
    void listarFaltasDosEducandos_semEducandos_deveRetornarListaVazia() {
        // GIVEN
        when(idHasher.decode("hashEnc")).thenReturn(30);
        when(encarregadoAlunoRepository.findAllByEncarregado_Id(30)).thenReturn(List.of());

        // WHEN
        List<FaltaDto> resultado = cancelamentoService.listarFaltasDosEducandos("hashEnc");

        // THEN
        assertTrue(resultado.isEmpty());
        verify(cancelamentoRepository, never()).findByUtilizadorIdIn(any());
    }
}