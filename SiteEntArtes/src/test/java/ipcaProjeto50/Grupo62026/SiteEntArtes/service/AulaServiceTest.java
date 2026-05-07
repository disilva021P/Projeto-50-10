package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AulaServiceTest {

    @Mock private EstudioService estudioService;
    @Mock private EstudioModalidadeRepository estudioModalidadeRepository;
    @Mock private UtilizadoreRepository utilizadoreRepository;
    @Mock private AulaRepository aulaRepository;
    @Mock private IdHasher idHasher;
    @Mock private EstadoAuloService estadoAuloService;
    @Mock private ModalidadeService modalidadeService;
    @Mock private AulaFixaService aulaFixaService;
    @Mock private HorarioFixoRepository horarioFixoRepository;
    @Mock private DisponibilidadeService disponibilidadeService;
    @Mock private EncarregadoAlunoRepository encarregadoAlunoRepository;
    @Mock private UtilizadorService utilizadorService;
    @Mock private TurmaService turmaService;
    @Mock private AulaCoachingRepository aulaCoachingRepository;
    @Mock private AulaProfessorService aulaProfessorService;
    @Mock private ProfessorService professorService;
    @Mock private AulaAlunoRepository aulaAlunoRepository;
    @Mock private AlunoRepository alunoRepository;
    @Mock private NotificacoesService notificacoesService;
    @Mock private ProfessorModalidadeRepository professorModalidadeRepository;
    @Mock private PagamentoRepository pagamentoRepository;
    @Mock private CancelamentoService cancelamentoService;
    @Mock private TipoPagamentoRepository tipoPagamentoRepository;
    @Mock private FeriadosService feriadosService;
    @Mock private AulaProfessoreRepository aulaProfessoreRepository;

    private AulaService aulaService;

    // Entidades reutilizáveis
    private EstadoAula estadoPendente;
    private EstadoAula estadoAgendada;
    private EstadoAula estadoRealizada;
    private EstadoAula estadoCancelada;
    private EstadoAula estadoValidada;
    private EstadoAula estadoAutomatico;

    @BeforeEach
    void setUp() {
        aulaService = new AulaService(
                estudioService,
                estudioModalidadeRepository,
                utilizadoreRepository,
                aulaRepository,
                idHasher,
                estadoAuloService,
                modalidadeService,
                aulaFixaService,
                horarioFixoRepository,
                disponibilidadeService,
                encarregadoAlunoRepository,
                utilizadorService,
                turmaService,
                aulaCoachingRepository,
                aulaProfessorService,
                professorService,
                aulaAlunoRepository,
                alunoRepository,
                notificacoesService,
                professorModalidadeRepository,
                pagamentoRepository,
                cancelamentoService,
                tipoPagamentoRepository,
                feriadosService,
                aulaProfessoreRepository
        );

        estadoPendente  = criarEstado(AulaService.ID_ESTADO_PENDENTE,  "PENDENTE");
        estadoAgendada  = criarEstado(AulaService.ID_ESTADO_AGENDADA,  "AGENDADA");
        estadoRealizada = criarEstado(AulaService.ID_ESTADO_REALIZADA, "REALIZADA");
        estadoCancelada = criarEstado(AulaService.ID_ESTADO_CANCELADA, "CANCELADA");
        estadoValidada  = criarEstado(AulaService.ID_ESTADO_VALIDADA,  "VALIDADA");
        estadoAutomatico = criarEstado(AulaService.ID_ESTADO_AUTOMATICO, "AUTOMATICO");
    }

    // =========================================================================
    // Auxiliares de construção
    // =========================================================================

    private EstadoAula criarEstado(int id, String nome) {
        EstadoAula e = new EstadoAula();
        e.setId(id);
        e.setEstado(nome);
        return e;
    }

    private Aula criarAula(int id, EstadoAula estado) {
        Estudio estudio = new Estudio();
        estudio.setId(1);
        estudio.setNome("Estudio A");

        Utilizadore criador = new Utilizadore();
        criador.setId(99);

        Aula aula = new Aula();
        aula.setId(id);
        aula.setEstado(estado);
        aula.setEstudio(estudio);
        aula.setDataAula(LocalDate.now().plusDays(3));
        aula.setHoraInicio(LocalTime.of(10, 0));
        aula.setHoraFim(LocalTime.of(11, 0));
        aula.setDuracaoMinutos(60);
        aula.setCriadoPor(criador);
        return aula;
    }

    private AulaDto criarAulaDto(int id) {
        EstudioDto estudioDto = mock(EstudioDto.class);
        when(estudioDto.id()).thenReturn("estHash");
        EstadoAulaDto estadoDto = mock(EstadoAulaDto.class);
        return new AulaDto(
                "hash" + id,
                estudioDto,
                60,
                LocalDate.now().plusDays(3),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                "criadoPorHash",
                null,
                estadoDto
        );
    }

    // =========================================================================
    // findAll
    // =========================================================================

    @Test
    @DisplayName("findAll – deve devolver lista de DTOs")
    void findAll_Sucesso() {
        Aula aula = criarAula(1, estadoAgendada);
        when(aulaRepository.findAll()).thenReturn(List.of(aula));
        when(idHasher.encode(anyInt())).thenReturn("hash1");
        when(estudioService.converterParaDto(any())).thenReturn(mock(EstudioDto.class));
        when(estadoAuloService.converterParaDto(any())).thenReturn(mock(EstadoAulaDto.class));
        when(aulaFixaService.convertToDto(any())).thenReturn(null);

        List<AulaDto> resultado = aulaService.findAll();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(aulaRepository).findAll();
    }

    @Test
    @DisplayName("findAll – deve devolver lista vazia quando não há aulas")
    void findAll_ListaVazia() {
        when(aulaRepository.findAll()).thenReturn(List.of());

        List<AulaDto> resultado = aulaService.findAll();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // =========================================================================
    // buscarPorId
    // =========================================================================

    @Test
    @DisplayName("buscarPorId (Integer) – deve devolver Optional com aula quando existe")
    void buscarPorId_Integer_Sucesso() {
        Aula aula = criarAula(1, estadoAgendada);
        when(aulaRepository.findById(1)).thenReturn(Optional.of(aula));

        Optional<Aula> resultado = aulaService.buscarPorId(1);

        assertTrue(resultado.isPresent());
        assertEquals(1, resultado.get().getId());
    }

    @Test
    @DisplayName("buscarPorId (String) – deve devolver Optional com aula quando existe")
    void buscarPorId_String_Sucesso() {
        Aula aula = criarAula(1, estadoAgendada);
        when(idHasher.decode("hash1")).thenReturn(1);
        when(aulaRepository.findById(1)).thenReturn(Optional.of(aula));

        Optional<Aula> resultado = aulaService.buscarPorId("hash1");

        assertTrue(resultado.isPresent());
    }

    @Test
    @DisplayName("buscarPorId – deve devolver Optional vazio quando aula não existe")
    void buscarPorId_NaoEncontrado() {
        when(aulaRepository.findById(99)).thenReturn(Optional.empty());

        Optional<Aula> resultado = aulaService.buscarPorId(99);

        assertFalse(resultado.isPresent());
    }

    // =========================================================================
    // bucarPorId (com lançamento de exceção)
    // =========================================================================

    @Test
    @DisplayName("bucarPorId (Integer) – deve lançar exceção quando aula não existe")
    void bucarPorId_Integer_NaoEncontrado_Erro() {
        when(aulaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> aulaService.bucarPorId(99));
    }

    @Test
    @DisplayName("bucarPorId (String) – deve lançar exceção quando aula não existe")
    void bucarPorId_String_NaoEncontrado_Erro() {
        when(idHasher.decode("invalido")).thenReturn(99);
        when(aulaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> aulaService.bucarPorId("invalido"));
    }

    @Test
    @DisplayName("bucarPorIdDto – deve devolver DTO quando aula existe")
    void bucarPorIdDto_Sucesso() throws Exception {
        Aula aula = criarAula(1, estadoAgendada);
        when(aulaRepository.findById(1)).thenReturn(Optional.of(aula));
        when(idHasher.encode(anyInt())).thenReturn("hash1");
        when(estudioService.converterParaDto(any())).thenReturn(mock(EstudioDto.class));
        when(estadoAuloService.converterParaDto(any())).thenReturn(mock(EstadoAulaDto.class));
        when(aulaFixaService.convertToDto(any())).thenReturn(null);

        AulaDto resultado = aulaService.bucarPorIdDto(1);

        assertNotNull(resultado);
    }

    @Test
    @DisplayName("bucarPorIdDto – deve lançar exceção quando aula não existe")
    void bucarPorIdDto_NaoEncontrado_Erro() {
        when(aulaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> aulaService.bucarPorIdDto(99));
    }

    // =========================================================================
    // salvar e eliminar
    // =========================================================================

    @Test
    @DisplayName("salvar – deve guardar e devolver a aula")
    void salvar_Sucesso() {
        Aula aula = criarAula(1, estadoAgendada);
        when(aulaRepository.save(aula)).thenReturn(aula);

        Aula resultado = aulaService.salvar(aula);

        assertNotNull(resultado);
        verify(aulaRepository).save(aula);
    }

    @Test
    @DisplayName("eliminar (Integer) – deve chamar deleteById com id correto")
    void eliminar_Integer_Sucesso() {
        aulaService.eliminar(1);

        verify(aulaRepository).deleteById(1);
    }

    @Test
    @DisplayName("eliminar (String) – deve descodificar e chamar deleteById")
    void eliminar_String_Sucesso() {
        when(idHasher.decode("hash1")).thenReturn(1);

        aulaService.eliminar("hash1");

        verify(aulaRepository).deleteById(1);
    }

    // =========================================================================
    // findByDataAula
    // =========================================================================

    @Test
    @DisplayName("findByDataAula – deve devolver aulas de uma data específica")
    void findByDataAula_Sucesso() {
        LocalDate data = LocalDate.now();
        Aula aula = criarAula(1, estadoAgendada);
        when(aulaRepository.findByDataAula(data)).thenReturn(List.of(aula));
        when(idHasher.encode(anyInt())).thenReturn("hash1");
        when(estudioService.converterParaDto(any())).thenReturn(mock(EstudioDto.class));
        when(estadoAuloService.converterParaDto(any())).thenReturn(mock(EstadoAulaDto.class));
        when(aulaFixaService.convertToDto(any())).thenReturn(null);

        List<AulaDto> resultado = aulaService.findByDataAula(data);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("findByDataAula – deve usar data de hoje quando data é null")
    void findByDataAula_DataNula_UsaHoje() {
        LocalDate hoje = LocalDate.now();
        when(aulaRepository.findByDataAula(hoje)).thenReturn(List.of());

        List<AulaDto> resultado = aulaService.findByDataAula(null);

        assertNotNull(resultado);
        verify(aulaRepository).findByDataAula(hoje);
    }

    // =========================================================================
    // buscarAulaporId_Data
    // =========================================================================

    @Test
    @DisplayName("buscarAulaporId_Data – deve devolver aulas do aluno na data")
    void buscarAulaporId_Data_Sucesso() throws Exception {
        Utilizadore user = new Utilizadore();
        user.setId(5);
        LocalDate data = LocalDate.now();

        when(idHasher.decode("userHash")).thenReturn(5);
        when(utilizadoreRepository.findById(5)).thenReturn(Optional.of(user));
        when(aulaRepository.findByDataEAluno(data, 5)).thenReturn(List.of());

        List<AulaDto> resultado = aulaService.buscarAulaporId_Data(data, "userHash");

        assertNotNull(resultado);
        verify(aulaRepository).findByDataEAluno(data, 5);
    }

    @Test
    @DisplayName("buscarAulaporId_Data – deve lançar exceção quando utilizador não existe")
    void buscarAulaporId_Data_UtilizadorNaoExiste_Erro() {
        when(idHasher.decode("invalido")).thenReturn(99);
        when(utilizadoreRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class,
                () -> aulaService.buscarAulaporId_Data(LocalDate.now(), "invalido"));
    }

    // =========================================================================
    // buscarAulaAluno
    // =========================================================================

    @Test
    @DisplayName("buscarAulaAluno – deve devolver DTO quando aluno está inscrito na aula")
    void buscarAulaAluno_Sucesso() throws Exception {
        Utilizadore user = new Utilizadore();
        user.setId(5);
        Aula aula = criarAula(10, estadoAgendada);

        when(idHasher.decode("userHash")).thenReturn(5);
        when(idHasher.decode("aulaHash")).thenReturn(10);
        when(utilizadoreRepository.findById(5)).thenReturn(Optional.of(user));
        when(aulaRepository.findAulaByIdAndAlunoId(10, 5)).thenReturn(Optional.of(aula));
        when(idHasher.encode(anyInt())).thenReturn("hash10");
        when(estudioService.converterParaDto(any())).thenReturn(mock(EstudioDto.class));
        when(estadoAuloService.converterParaDto(any())).thenReturn(mock(EstadoAulaDto.class));
        when(aulaFixaService.convertToDto(any())).thenReturn(null);

        AulaDto resultado = aulaService.buscarAulaAluno("userHash", "aulaHash");

        assertNotNull(resultado);
    }

    @Test
    @DisplayName("buscarAulaAluno – deve lançar exceção quando aluno não está inscrito na aula")
    void buscarAulaAluno_AlunoNaoInscrito_Erro() {
        Utilizadore user = new Utilizadore();
        user.setId(5);

        when(idHasher.decode("userHash")).thenReturn(5);
        when(idHasher.decode("aulaHash")).thenReturn(10);
        when(utilizadoreRepository.findById(5)).thenReturn(Optional.of(user));
        when(aulaRepository.findAulaByIdAndAlunoId(10, 5)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> aulaService.buscarAulaAluno("userHash", "aulaHash"));
    }

    // =========================================================================
    // contarInscritos
    // =========================================================================

    @Test
    @DisplayName("contarInscritos – deve devolver o número correto de inscritos")
    void contarInscritos_Sucesso() {
        when(idHasher.decode("aulaHash")).thenReturn(10);
        when(aulaAlunoRepository.countByAulaId(10)).thenReturn(3L);

        long resultado = aulaService.contarInscritos("aulaHash");

        assertEquals(3L, resultado);
    }

    // =========================================================================
    // inscreverAluno
    // =========================================================================

    @Test
    @DisplayName("inscreverAluno – deve inscrever aluno com sucesso")
    void inscreverAluno_Sucesso() throws Exception {
        Aula aula = criarAula(10, estadoAgendada);
        Aluno aluno = new Aluno();
        aluno.setId(5);

        when(idHasher.decode("aulaHash")).thenReturn(10);
        when(idHasher.decode("alunoHash")).thenReturn(5);
        when(aulaAlunoRepository.existsById(any())).thenReturn(false);
        when(aulaRepository.findById(10)).thenReturn(Optional.of(aula));
        when(alunoRepository.findById(5)).thenReturn(Optional.of(aluno));
        when(aulaProfessorService.findAllByAulaId("aulaHash")).thenReturn(List.of());

        aulaService.inscreverAluno("alunoHash", "aulaHash");

        verify(aulaAlunoRepository).save(any(AulaAluno.class));
    }

    @Test
    @DisplayName("inscreverAluno – deve lançar exceção quando aluno já está inscrito")
    void inscreverAluno_JaInscrito_Erro() {
        when(idHasher.decode("aulaHash")).thenReturn(10);
        when(idHasher.decode("alunoHash")).thenReturn(5);
        when(aulaAlunoRepository.existsById(any())).thenReturn(true);

        assertThrows(Exception.class, () -> aulaService.inscreverAluno("alunoHash", "aulaHash"));
        verify(aulaAlunoRepository, never()).save(any());
    }

    @Test
    @DisplayName("inscreverAluno – deve lançar exceção quando aula não existe")
    void inscreverAluno_AulaNaoExiste_Erro() {
        when(idHasher.decode("aulaHash")).thenReturn(10);
        when(idHasher.decode("alunoHash")).thenReturn(5);
        when(aulaAlunoRepository.existsById(any())).thenReturn(false);
        when(aulaRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> aulaService.inscreverAluno("alunoHash", "aulaHash"));
    }

    @Test
    @DisplayName("inscreverAluno – deve lançar exceção quando aluno não existe")
    void inscreverAluno_AlunoNaoExiste_Erro() {
        Aula aula = criarAula(10, estadoAgendada);

        when(idHasher.decode("aulaHash")).thenReturn(10);
        when(idHasher.decode("alunoHash")).thenReturn(5);
        when(aulaAlunoRepository.existsById(any())).thenReturn(false);
        when(aulaRepository.findById(10)).thenReturn(Optional.of(aula));
        when(alunoRepository.findById(5)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> aulaService.inscreverAluno("alunoHash", "aulaHash"));
    }

    // =========================================================================
    // cancelarInscricaoAluno
    // =========================================================================

    @Test
    @DisplayName("cancelarInscricaoAluno – deve cancelar inscrição quando aluno está inscrito")
    void cancelarInscricaoAluno_Sucesso() throws Exception {
        Aula aula = criarAula(10, estadoPendente);

        when(idHasher.decode("aulaHash")).thenReturn(10);
        when(idHasher.decode("alunoHash")).thenReturn(5);
        when(aulaRepository.findById(10)).thenReturn(Optional.of(aula));
        when(aulaAlunoRepository.existsById(any())).thenReturn(true);

        aulaService.cancelarInscricaoAluno("alunoHash", "aulaHash");

        verify(aulaAlunoRepository).deleteById(any());
    }

    @Test
    @DisplayName("cancelarInscricaoAluno – deve lançar exceção quando aula não existe")
    void cancelarInscricaoAluno_AulaNaoExiste_Erro() {
        when(idHasher.decode("aulaHash")).thenReturn(10);
        when(aulaRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(Exception.class,
                () -> aulaService.cancelarInscricaoAluno("alunoHash", "aulaHash"));
    }

    @Test
    @DisplayName("cancelarInscricaoAluno – deve lançar exceção quando aluno não está inscrito")
    void cancelarInscricaoAluno_NaoInscrito_Erro() {
        Aula aula = criarAula(10, estadoPendente);

        when(idHasher.decode("aulaHash")).thenReturn(10);
        when(idHasher.decode("alunoHash")).thenReturn(5);
        when(aulaRepository.findById(10)).thenReturn(Optional.of(aula));
        when(aulaAlunoRepository.existsById(any())).thenReturn(false);

        assertThrows(Exception.class,
                () -> aulaService.cancelarInscricaoAluno("alunoHash", "aulaHash"));
    }

    // =========================================================================
    // validarRealizacao
    // =========================================================================

    @Test
    @DisplayName("validarRealizacao – deve lançar exceção quando aula está cancelada")
    void validarRealizacao_AulaCancelada_Erro() {
        Aula aula = criarAula(10, estadoCancelada);

        when(idHasher.decode("aulaHash")).thenReturn(10);
        when(aulaRepository.findById(10)).thenReturn(Optional.of(aula));

        assertThrows(Exception.class, () -> aulaService.validarRealizacao("aulaHash"));
    }

    @Test
    @DisplayName("validarRealizacao – deve lançar exceção quando aula já foi validada")
    void validarRealizacao_JaValidada_Erro() {
        Aula aula = criarAula(10, estadoValidada);

        when(idHasher.decode("aulaHash")).thenReturn(10);
        when(aulaRepository.findById(10)).thenReturn(Optional.of(aula));

        assertThrows(Exception.class, () -> aulaService.validarRealizacao("aulaHash"));
    }

    @Test
    @DisplayName("validarRealizacao – deve lançar exceção quando aula já foi processada automaticamente")
    void validarRealizacao_JaAutomatico_Erro() {
        Aula aula = criarAula(10, estadoAutomatico);

        when(idHasher.decode("aulaHash")).thenReturn(10);
        when(aulaRepository.findById(10)).thenReturn(Optional.of(aula));

        assertThrows(Exception.class, () -> aulaService.validarRealizacao("aulaHash"));
    }

    @Test
    @DisplayName("validarRealizacao – deve lançar exceção quando aula não existe")
    void validarRealizacao_NaoExiste_Erro() {
        when(idHasher.decode("invalido")).thenReturn(99);
        when(aulaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> aulaService.validarRealizacao("invalido"));
    }

    // =========================================================================
    // processarPagamentosAula
    // =========================================================================

    @Test
    @DisplayName("processarPagamentosAula – deve lançar exceção quando não há professores nem alunos")
    void processarPagamentos_SemProfessoresNemAlunos_Erro() throws Exception {
        Aula aula = criarAula(10, estadoRealizada);

        when(idHasher.encode(10)).thenReturn("hash10");
        when(aulaProfessorService.findAllByAulaId("hash10")).thenReturn(List.of());
        when(aulaAlunoRepository.findAllByAula_Id(10)).thenReturn(List.of());

        assertThrows(Exception.class, () -> aulaService.processarPagamentosAula(aula));
    }

    @Test
    @DisplayName("processarPagamentosAula – deve lançar exceção quando não há alunos inscritos")
    void processarPagamentos_SemAlunos_Erro() throws Exception {
        Aula aula = criarAula(10, estadoRealizada);
        AulaProfessore aulaProfessore = mock(AulaProfessore.class);

        when(idHasher.encode(10)).thenReturn("hash10");
        when(aulaProfessorService.findAllByAulaId("hash10")).thenReturn(List.of(aulaProfessore));
        when(aulaAlunoRepository.findAllByAula_Id(10)).thenReturn(List.of());

        assertThrows(Exception.class, () -> aulaService.processarPagamentosAula(aula));
    }

    @Test
    @DisplayName("processarPagamentosAula – deve gerar pagamentos para alunos e professores")
    void processarPagamentos_Sucesso() throws Exception {
        Aula aula = criarAula(10, estadoRealizada);

        Professore professor = new Professore();
        professor.setId(7);
        professor.setValorHora(new BigDecimal("20.00"));

        AulaProfessore aulaProfessore = new AulaProfessore();
        aulaProfessore.setProfessor(professor);

        Aluno aluno = new Aluno();
        aluno.setId(5);
        AulaAluno aulaAluno = new AulaAluno();
        aulaAluno.setAluno(aluno);

        TipoPagamento tipoAluno = new TipoPagamento();
        tipoAluno.setId(2);
        TipoPagamento tipoProf = new TipoPagamento();
        tipoProf.setId(7);

        when(idHasher.encode(10)).thenReturn("hash10");
        when(aulaProfessorService.findAllByAulaId("hash10")).thenReturn(List.of(aulaProfessore));
        when(aulaAlunoRepository.findAllByAula_Id(10)).thenReturn(List.of(aulaAluno));
        when(tipoPagamentoRepository.findById(2)).thenReturn(Optional.of(tipoAluno));
        when(tipoPagamentoRepository.findById(7)).thenReturn(Optional.of(tipoProf));

        aulaService.processarPagamentosAula(aula);

        verify(pagamentoRepository, times(2)).save(any(Pagamento.class)); // 1 aluno + 1 professor
    }

    // =========================================================================
    // criarAula
    // =========================================================================

    @Test
    @DisplayName("criarAula – deve lançar exceção quando estúdio não suporta a modalidade")
    void criarAula_EstudioSemModalidade_Erro() {
        AulaDto aulaDto = criarAulaDto(1);

        when(idHasher.decode("estHash")).thenReturn(1);
        when(estudioModalidadeRepository.findByEstudio_IdAndModalidade_Id(1, 1))
                .thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> aulaService.criarAula(aulaDto, 1));
    }

    @Test
    @DisplayName("criarAula – deve lançar exceção quando existe conflito de horário no estúdio")
    void criarAula_ConflitoHorario_Erro() {
        AulaDto aulaDto = criarAulaDto(1);
        EstudioModalidade estudioModalidade = mock(EstudioModalidade.class);

        when(idHasher.decode("estHash")).thenReturn(1);
        when(estudioModalidadeRepository.findByEstudio_IdAndModalidade_Id(1, 1))
                .thenReturn(Optional.of(estudioModalidade));
        when(estudioService.conflitoestudio(eq(1), any(), any(), any())).thenReturn(true);

        assertThrows(Exception.class, () -> aulaService.criarAula(aulaDto, 1));
    }
    @Test
    @DisplayName("criarAula – deve guardar e devolver aula quando não há conflito")
    void criarAula_Sucesso() throws Exception {
        // 1. Dados base
        AulaDto aulaDto = criarAulaDto(1);
        Aula aula = criarAula(1, estadoAgendada);
        Estudio estudio = new Estudio();
        estudio.setId(1);

        // 2. Configuração do IdHasher (O escudo para os valores nulos)
        lenient().when(idHasher.decode("estHash")).thenReturn(1);
        lenient().when(idHasher.decode("hash3")).thenReturn(3);
        lenient().when(idHasher.decode("criadoPorHash")).thenReturn(99);
        lenient().when(idHasher.decode(null)).thenReturn(0); // O nulo vira 0

        // 3. Mocks de Serviço (Ajustado para aceitar o 0 ou qualquer valor)
        when(estadoAuloService.findbyId(anyInt())).thenReturn(estadoAgendada); // <--- CORREÇÃO AQUI

        when(estudioModalidadeRepository.findByEstudio_IdAndModalidade_Id(anyInt(), anyInt()))
                .thenReturn(Optional.of(mock(EstudioModalidade.class)));
        when(estudioService.conflitoestudio(anyInt(), any(), any(), any())).thenReturn(false);
        when(estudioService.findEstudiobyId(1)).thenReturn(estudio);
        when(utilizadoreRepository.findById(anyInt())).thenReturn(Optional.of(aula.getCriadoPor()));
        when(aulaRepository.save(any())).thenReturn(aula);

        // 4. Execução
        Aula resultado = aulaService.criarAula(aulaDto, 1);

        // 5. Verificação
        assertNotNull(resultado);
        verify(aulaRepository).save(any());
    }    // buscarHorarioSemana
    // =========================================================================

    @Test
    @DisplayName("buscarHorarioSemana – deve lançar exceção quando utilizador não existe")
    void buscarHorarioSemana_UtilizadorNaoExiste_Erro() {
        when(idHasher.decode("invalido")).thenReturn(99);
        when(utilizadoreRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> aulaService.buscarHorarioSemana("invalido", 0));
    }

    @Test
    @DisplayName("buscarHorarioSemana – deve devolver aulas da semana atual quando offset é 0")
    void buscarHorarioSemana_Sucesso() throws Exception {
        Utilizadore user = new Utilizadore();
        user.setId(5);

        when(idHasher.decode("userHash")).thenReturn(5);
        when(utilizadoreRepository.findById(5)).thenReturn(Optional.of(user));
        when(aulaRepository.buscarHorarioDoAluno(eq(5), any(), any())).thenReturn(List.of());

        List<AulaDto> resultado = aulaService.buscarHorarioSemana("userHash", 0);

        assertNotNull(resultado);
        verify(aulaRepository).buscarHorarioDoAluno(eq(5), any(), any());
    }
}