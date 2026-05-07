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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AulaCoachingServiceTest {

    @Mock private AulaCoachingRepository aulaCoachingRepository;
    @Mock private IdHasher idHasher;
    @Mock private AulaService aulaService;
    @Mock private EstadoAuloService estadoAuloService;
    @Mock private ModalidadeService modalidadeService;
    @Mock private DisponibilidadeService disponibilidadeService;
    @Mock private EstudioRepository estudioRepository;
    @Mock private EstudioService estudioService;
    @Mock private UtilizadoreRepository utilizadoreRepository;
    @Mock private AulaRepository aulaRepository;
    @Mock private AulaAlunoRepository aulaAlunoRepository;
    @Mock private AlunoRepository alunoRepository;
    @Mock private AulaProfessoreRepository aulaProfessoreRepository;
    @Mock private AulaAlunoService aulaAlunoService;
    @Mock private ProfessoreRepository professoreRepository;
    @Mock private ProfessorService professorService;
    @Mock private ProfessorModalidadeRepository professorModalidadeRepository;
    @Mock private EstudioModalidadeRepository estudioModalidadeRepository;
    @Mock private NotificacoesService notificacoesService;
    @Mock private UtilizadorService utilizadorService;
    @Mock private EncarregadoAlunoRepository encarregadoAlunoRepository;

    private AulaCoachingService aulaCoachingService;

    // Entidades e estado reutilizáveis
    private EstadoAula estadoPendente;
    private EstadoAula estadoAgendada;
    private EstadoAula estadoRealizada;
    private EstadoAula estadoCancelada;

    @BeforeEach
    void setUp() {
        aulaCoachingService = new AulaCoachingService(
                aulaCoachingRepository,
                idHasher,
                aulaService,
                estadoAuloService,
                modalidadeService,
                disponibilidadeService,
                estudioRepository,
                estudioService,
                utilizadoreRepository,
                aulaRepository,
                aulaAlunoRepository,
                alunoRepository,
                aulaProfessoreRepository,
                aulaAlunoService,
                professoreRepository,
                professorService,
                professorModalidadeRepository,
                estudioModalidadeRepository,
                notificacoesService,
                utilizadorService,
                encarregadoAlunoRepository
        );

        estadoPendente  = criarEstado(AulaService.ID_ESTADO_PENDENTE,  "PENDENTE");
        estadoAgendada  = criarEstado(AulaService.ID_ESTADO_AGENDADA,  "AGENDADA");
        estadoRealizada = criarEstado(AulaService.ID_ESTADO_REALIZADA, "REALIZADA");
        estadoCancelada = criarEstado(AulaService.ID_ESTADO_CANCELADA, "CANCELADA");
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

    private AulaCoaching criarCoaching(int id, EstadoAula estado, int maxAlunos) {
        AulaCoaching c = new AulaCoaching();
        c.setId(id);
        c.setEstado(estado);
        c.setMaxAlunos(maxAlunos);
        c.setDataAula(LocalDate.now().plusDays(3));
        c.setHoraInicio(LocalTime.of(10, 0));
        c.setHoraFim(LocalTime.of(11, 0));
        Modalidade mod = new Modalidade(); mod.setId(1);
        c.setModalidade(mod);
        return c;
    }

    // =========================================================================
    // findById (String)
    // =========================================================================

    @Test
    @DisplayName("findById – deve devolver DTO quando coaching existe")
    void findById_Sucesso() throws Exception {
        AulaCoaching coaching = criarCoaching(1, estadoAgendada, 5);
        AulaDto aulaDto = mock(AulaDto.class);

        when(idHasher.decode("hash1")).thenReturn(1);
        when(aulaCoachingRepository.findById(1)).thenReturn(Optional.of(coaching));
        when(aulaService.bucarPorIdDto(1)).thenReturn(aulaDto);
        when(estadoAuloService.converterParaDto(any())).thenReturn(mock(EstadoAulaDto.class));
        when(modalidadeService.converterParaDto(any())).thenReturn(mock(ModalidadeDto.class));

        AulaCoachingDto resultado = aulaCoachingService.findById("hash1");

        assertNotNull(resultado);
        verify(aulaCoachingRepository).findById(1);
    }

    @Test
    @DisplayName("findById – deve lançar exceção quando coaching não existe")
    void findById_NaoEncontrado_Erro() {
        when(idHasher.decode("invalido")).thenReturn(99);
        when(aulaCoachingRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> aulaCoachingService.findById("invalido"));
    }

    // =========================================================================
    // realizar
    // =========================================================================

    @Test
    @DisplayName("realizar – deve mudar estado para REALIZADA quando coaching está agendado")
    void realizar_Sucesso() throws Exception {
        AulaCoaching coaching = criarCoaching(10, estadoAgendada, 3);
        AulaDto aulaDto = mock(AulaDto.class);

        when(idHasher.decode("aulahash")).thenReturn(10);
        when(aulaCoachingRepository.findById(10)).thenReturn(Optional.of(coaching));
        when(estadoAuloService.findbyId(AulaService.ID_ESTADO_REALIZADA)).thenReturn(estadoRealizada);
        when(aulaCoachingRepository.save(coaching)).thenReturn(coaching);
        when(aulaService.bucarPorIdDto(10)).thenReturn(aulaDto);
        when(estadoAuloService.converterParaDto(any())).thenReturn(mock(EstadoAulaDto.class));
        when(modalidadeService.converterParaDto(any())).thenReturn(mock(ModalidadeDto.class));

        AulaCoachingDto resultado = aulaCoachingService.realizar("aulahash");

        assertNotNull(resultado);
        verify(aulaCoachingRepository).save(coaching);
        assertEquals(AulaService.ID_ESTADO_REALIZADA, coaching.getEstado().getId());
    }

    @Test
    @DisplayName("realizar – deve lançar exceção quando coaching já está cancelado")
    void realizar_JaCancelado_Erro() {
        AulaCoaching coaching = criarCoaching(10, estadoCancelada, 3);

        when(idHasher.decode("aulahash")).thenReturn(10);
        when(aulaCoachingRepository.findById(10)).thenReturn(Optional.of(coaching));

        assertThrows(Exception.class, () -> aulaCoachingService.realizar("aulahash"));
    }

    @Test
    @DisplayName("realizar – deve lançar exceção quando coaching já está realizado")
    void realizar_JaRealizado_Erro() {
        AulaCoaching coaching = criarCoaching(10, estadoRealizada, 3);

        when(idHasher.decode("aulahash")).thenReturn(10);
        when(aulaCoachingRepository.findById(10)).thenReturn(Optional.of(coaching));

        assertThrows(Exception.class, () -> aulaCoachingService.realizar("aulahash"));
    }

    // =========================================================================
    // cancelar
    // =========================================================================

    @Test
    @DisplayName("cancelar – deve mudar estado para CANCELADA quando coaching está pendente")
    void cancelar_Sucesso() throws Exception {
        AulaCoaching coaching = criarCoaching(20, estadoPendente, 3);
        AulaDto aulaDto = mock(AulaDto.class);

        when(idHasher.decode("cancelHash")).thenReturn(20);
        when(aulaCoachingRepository.findById(20)).thenReturn(Optional.of(coaching));
        when(estadoAuloService.findbyId(AulaService.ID_ESTADO_CANCELADA)).thenReturn(estadoCancelada);
        when(aulaCoachingRepository.save(coaching)).thenReturn(coaching);
        when(aulaService.bucarPorIdDto(20)).thenReturn(aulaDto);
        when(estadoAuloService.converterParaDto(any())).thenReturn(mock(EstadoAulaDto.class));
        when(modalidadeService.converterParaDto(any())).thenReturn(mock(ModalidadeDto.class));

        AulaCoachingDto resultado = aulaCoachingService.cancelar("cancelHash");

        assertNotNull(resultado);
        assertEquals(AulaService.ID_ESTADO_CANCELADA, coaching.getEstado().getId());
        verify(aulaCoachingRepository).save(coaching);
    }

    @Test
    @DisplayName("cancelar – deve lançar exceção quando coaching já está realizado")
    void cancelar_JaRealizado_Erro() {
        AulaCoaching coaching = criarCoaching(20, estadoRealizada, 3);

        when(idHasher.decode("cancelHash")).thenReturn(20);
        when(aulaCoachingRepository.findById(20)).thenReturn(Optional.of(coaching));

        assertThrows(Exception.class, () -> aulaCoachingService.cancelar("cancelHash"));
    }

    @Test
    @DisplayName("cancelar – deve lançar exceção quando coaching já está cancelado")
    void cancelar_JaCancelado_Erro() {
        AulaCoaching coaching = criarCoaching(20, estadoCancelada, 3);

        when(idHasher.decode("cancelHash")).thenReturn(20);
        when(aulaCoachingRepository.findById(20)).thenReturn(Optional.of(coaching));

        assertThrows(Exception.class, () -> aulaCoachingService.cancelar("cancelHash"));
    }

    // =========================================================================
    // inscrever
    // =========================================================================

    @Test
    @DisplayName("inscrever – deve inscrever aluno com sucesso quando há vagas")
    void inscrever_Sucesso() throws Exception {
        // 1. Setup (Cenário)
        AulaCoaching coaching = criarCoaching(30, estadoAgendada, 5);

        when(idHasher.decode("aulaHash")).thenReturn(30);
        when(aulaCoachingRepository.findById(30)).thenReturn(Optional.of(coaching));
        when(aulaService.contarInscritos("aulaHash")).thenReturn(2L); // 2 de 5 vagas ocupadas

        // NOTA: Se as linhas abaixo derem erro de "Unnecessary Stubbing",
        // significa que o teu método 'inscrever' não as utiliza. Apaga-as se for o caso.
        when(aulaService.bucarPorIdDto(30)).thenReturn(mock(AulaDto.class));

        // 2. Execução
        AulaCoachingDto resultado = aulaCoachingService.inscrever("alunoHash", "aulaHash");

        // 3. Asserts (Validação de dados)
        assertNotNull(resultado, "O DTO de retorno não deve ser nulo");
        // Se o teu DTO tiver campos, valida-os aqui:
        // assertEquals(30, resultado.getId());

        // 4. Verifications (Validação de comportamento/efeitos colaterais)
        // Garantir que o serviço de inscrição foi REALMENTE chamado
        verify(aulaService, times(1)).inscreverAluno("alunoHash", "aulaHash");
    }

    @Test
    @DisplayName("inscrever – deve lançar exceção quando aula sem vagas")
    void inscrever_SemVagas_Erro() throws Exception {
        AulaCoaching coaching = criarCoaching(30, estadoAgendada, 3);

        when(idHasher.decode("aulaHash")).thenReturn(30);
        when(aulaCoachingRepository.findById(30)).thenReturn(Optional.of(coaching));
        when(aulaService.contarInscritos("aulaHash")).thenReturn(3L);

        assertThrows(Exception.class, () -> aulaCoachingService.inscrever("alunoHash", "aulaHash"));
        verify(aulaService, never()).inscreverAluno(any(), any());
    }

    @Test
    @DisplayName("inscrever – deve lançar exceção quando aula está cancelada")
    void inscrever_AulaCancelada_Erro() {
        AulaCoaching coaching = criarCoaching(30, estadoCancelada, 5);

        when(idHasher.decode("aulaHash")).thenReturn(30);
        when(aulaCoachingRepository.findById(30)).thenReturn(Optional.of(coaching));

        assertThrows(Exception.class, () -> aulaCoachingService.inscrever("alunoHash", "aulaHash"));
    }

    @Test
    @DisplayName("inscrever – deve lançar exceção quando aula já foi realizada")
    void inscrever_AulaRealizada_Erro() {
        AulaCoaching coaching = criarCoaching(30, estadoRealizada, 5);

        when(idHasher.decode("aulaHash")).thenReturn(30);
        when(aulaCoachingRepository.findById(30)).thenReturn(Optional.of(coaching));

        assertThrows(Exception.class, () -> aulaCoachingService.inscrever("alunoHash", "aulaHash"));
    }

    // =========================================================================
    // salvarMarcarCoaching – validações de negócio
    // =========================================================================

    @Test
    @DisplayName("salvarMarcarCoaching – deve lançar exceção quando data é no passado")
    void salvarCoaching_DataPassada_Erro() {
        AulaCoachingRequestDto dto = new AulaCoachingRequestDto(
                "profHash", "estHash",
                LocalDate.now().minusDays(1),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                3,"modalidadehash"
        );
        assertThrows(Exception.class,
                () -> aulaCoachingService.salvarMarcarCoaching(dto, "alunoHash"));
    }

    @Test
    @DisplayName("salvarMarcarCoaching – deve lançar exceção quando maxAlunos > 8")
    void salvarCoaching_MaxAlunosExcedido_Erro() {
        AulaCoachingRequestDto dto = new AulaCoachingRequestDto(
                "profHash", "estHash",
                LocalDate.now().plusDays(2),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                9, "modalidadeHash"
        );

        assertThrows(Exception.class,
                () -> aulaCoachingService.salvarMarcarCoaching(dto, "alunoHash"));
    }

    @Test
    @DisplayName("salvarMarcarCoaching – deve lançar exceção quando professor não leciona a modalidade")
    void salvarCoaching_ProfessorSemModalidade_Erro() {
        AulaCoachingRequestDto dto = new AulaCoachingRequestDto(
                "profHash", "estHash",
                LocalDate.now().plusDays(2),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                4, "modHash"
        );

        when(idHasher.decode(anyString())).thenReturn(1);
        when(professorModalidadeRepository.existsByModalidadeIdAndProfessorId(anyInt(), anyInt())).thenReturn(false);

        assertThrows(Exception.class,
                () -> aulaCoachingService.salvarMarcarCoaching(dto, "alunoHash"));
    }

    @Test
    @DisplayName("salvarMarcarCoaching – deve lançar exceção quando estúdio incompatível com modalidade")
    void salvarCoaching_EstudioIncompativel_Erro() {
        AulaCoachingRequestDto dto = new AulaCoachingRequestDto(
                "profHash", "estHash",
                LocalDate.now().plusDays(2),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                4, "modHash"
        );

        when(idHasher.decode(anyString())).thenReturn(1);
        when(professorModalidadeRepository.existsByModalidadeIdAndProfessorId(anyInt(), anyInt())).thenReturn(true);
        when(estudioModalidadeRepository.existsByEstudio_IdAndModalidade_Id(anyInt(), anyInt())).thenReturn(false);

        assertThrows(Exception.class,
                () -> aulaCoachingService.salvarMarcarCoaching(dto, "alunoHash"));
    }

    @Test
    @DisplayName("salvarMarcarCoaching – deve lançar exceção quando professor não está disponível")
    void salvarCoaching_ProfessorIndisponivel_Erro() {
        AulaCoachingRequestDto dto = new AulaCoachingRequestDto(
                "profHash", "estHash",
                LocalDate.now().plusDays(2),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                4, "modHash"
        );

        when(idHasher.decode(anyString())).thenReturn(1);
        when(professorModalidadeRepository.existsByModalidadeIdAndProfessorId(anyInt(), anyInt())).thenReturn(true);
        when(estudioModalidadeRepository.existsByEstudio_IdAndModalidade_Id(anyInt(), anyInt())).thenReturn(true);
        when(disponibilidadeService.verificaMarcacaoValida(any(), any(), any(), any())).thenReturn(false);

        assertThrows(Exception.class,
                () -> aulaCoachingService.salvarMarcarCoaching(dto, "alunoHash"));
    }

    @Test
    @DisplayName("salvarMarcarCoaching – deve lançar exceção quando existe conflito no estúdio")
    void salvarCoaching_ConflitoEstudio_Erro() {
        AulaCoachingRequestDto dto = new AulaCoachingRequestDto(
                "profHash", "estHash",
                LocalDate.now().plusDays(2),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                4, "modHash"
        );

        when(idHasher.decode(anyString())).thenReturn(1);
        when(professorModalidadeRepository.existsByModalidadeIdAndProfessorId(anyInt(), anyInt())).thenReturn(true);
        when(estudioModalidadeRepository.existsByEstudio_IdAndModalidade_Id(anyInt(), anyInt())).thenReturn(true);
        when(disponibilidadeService.verificaMarcacaoValida(any(), any(), any(), any())).thenReturn(true);
        when(aulaRepository.existeConflitoNoEstudio(anyInt(), any(), any(), any())).thenReturn(true);

        assertThrows(Exception.class,
                () -> aulaCoachingService.salvarMarcarCoaching(dto, "alunoHash"));
    }

    // =========================================================================
    // validarPresenca
    // =========================================================================

    @Test
    @DisplayName("validarPresenca – deve avançar para AGENDADA quando coaching está PENDENTE")
    void validarPresenca_PendenteParaAgendada() throws Exception {
        AulaCoaching coaching = criarCoaching(40, estadoPendente, 4);
        AulaDto aulaDto = mock(AulaDto.class);

        when(idHasher.decode("aulaHash")).thenReturn(40);
        when(aulaCoachingRepository.findById(40)).thenReturn(Optional.of(coaching));
        when(estadoAuloService.findbyId(AulaService.ID_ESTADO_AGENDADA)).thenReturn(estadoAgendada);
        when(aulaCoachingRepository.save(coaching)).thenReturn(coaching);
        when(aulaService.bucarPorIdDto(40)).thenReturn(aulaDto);
        when(estadoAuloService.converterParaDto(any())).thenReturn(mock(EstadoAulaDto.class));
        when(modalidadeService.converterParaDto(any())).thenReturn(mock(ModalidadeDto.class));
        when(aulaService.buscarAulaAluno("alunoHash", "aulaHash")).thenReturn(aulaDto);

        AulaCoachingDto resultado = aulaCoachingService.validarPresenca("alunoHash", "aulaHash");

        assertNotNull(resultado);
        assertEquals(AulaService.ID_ESTADO_AGENDADA, coaching.getEstado().getId());
        verify(aulaCoachingRepository).save(coaching);
    }

    @Test
    @DisplayName("validarPresenca – não altera estado quando coaching já está AGENDADA")
    void validarPresenca_JaAgendada_NaoAltera() throws Exception {
        AulaCoaching coaching = criarCoaching(40, estadoAgendada, 4);
        AulaDto aulaDto = mock(AulaDto.class);

        when(idHasher.decode("aulaHash")).thenReturn(40);
        when(aulaCoachingRepository.findById(40)).thenReturn(Optional.of(coaching));
        when(aulaService.bucarPorIdDto(40)).thenReturn(aulaDto);
        when(estadoAuloService.converterParaDto(any())).thenReturn(mock(EstadoAulaDto.class));
        when(modalidadeService.converterParaDto(any())).thenReturn(mock(ModalidadeDto.class));
        when(aulaService.buscarAulaAluno("alunoHash", "aulaHash")).thenReturn(aulaDto);

        aulaCoachingService.validarPresenca("alunoHash", "aulaHash");

        verify(aulaCoachingRepository, never()).save(any());
    }

    // =========================================================================
    // eliminar
    // =========================================================================

    @Test
    @DisplayName("eliminar – deve apagar coaching e registos associados quando estado é PENDENTE")
    void eliminar_EstadoPendente_ApagarTudo() throws Exception {
        AulaCoaching coaching = criarCoaching(50, estadoPendente, 3);

        when(idHasher.decode("delHash")).thenReturn(50);
        when(aulaCoachingRepository.findById(50)).thenReturn(Optional.of(coaching));

        aulaCoachingService.eliminar("delHash");

        verify(aulaAlunoRepository).deleteAllByAula_Id(50);
        verify(aulaProfessoreRepository).deleteAllByAula_Id(50);
        verify(aulaRepository).deleteById(50);
        verify(aulaCoachingRepository).deleteById(50);
    }

    @Test
    @DisplayName("eliminar – não apaga coaching quando estado não é PENDENTE")
    void eliminar_EstadoNaoPendente_NaoApaga() throws Exception {
        AulaCoaching coaching = criarCoaching(50, estadoAgendada, 3);

        when(idHasher.decode("delHash")).thenReturn(50);
        when(aulaCoachingRepository.findById(50)).thenReturn(Optional.of(coaching));

        aulaCoachingService.eliminar("delHash");

        verify(aulaAlunoRepository, never()).deleteAllByAula_Id(anyInt());
        verify(aulaCoachingRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("eliminar – deve lançar exceção quando coaching não existe")
    void eliminar_NaoExiste_Erro() {
        when(idHasher.decode("badHash")).thenReturn(99);
        when(aulaCoachingRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> aulaCoachingService.eliminar("badHash"));
    }

    // =========================================================================
    // professorRejeitaCoaching
    // =========================================================================

    @Test
    @DisplayName("professorRejeitaCoaching – deve notificar alunos e eliminar coaching")
    void professorRejeitaCoaching_Sucesso() throws Exception {
        AulaCoaching coaching = criarCoaching(60, estadoPendente, 3);
        Professore prof = new Professore(); prof.setId(7); prof.setNome("Prof Silva");
        AulaProfessore aulaProfessore = new AulaProfessore();
        aulaProfessore.setProfessor(prof);

        AulaAlunoDto alunoDto = mock(AulaAlunoDto.class);
        when(alunoDto.idAluno()).thenReturn("alunoHash");

        when(idHasher.decode("aulaHash")).thenReturn(60);
        when(idHasher.decode("profHash")).thenReturn(7);
        when(idHasher.decode("alunoHash")).thenReturn(5);
        when(idHasher.encode(7)).thenReturn("profHashEncoded");
        when(aulaCoachingRepository.findById(60)).thenReturn(Optional.of(coaching));
        when(aulaProfessoreRepository.findByAula_IdAndProfessor_Id(60, 7)).thenReturn(Optional.of(aulaProfessore));
        when(aulaAlunoService.findAllByAulaId("aulaHash")).thenReturn(List.of(alunoDto));

        aulaCoachingService.professorRejeitaCoaching("aulaHash", "profHash");

        verify(notificacoesService).criarNotificacao(eq(5), eq(7), anyString(), anyString(), eq("PEDIDO COACHING"), anyString());
        // A eliminação é delegada para eliminar() que vai ao repo
        verify(aulaAlunoRepository).deleteAllByAula_Id(60);
    }
}