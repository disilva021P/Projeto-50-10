package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.CriarEventosDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.EventoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ParticipanteDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock private EventoRepository eventoRepository;
    @Mock private ParticipantesEventoRepository participantesEventoRepository;
    @Mock private UtilizadoreRepository utilizadoreRepository;
    @Mock private IdHasher idHasher;
    @Mock private EstadoAulaRepository estadoAulaRepository;

    @InjectMocks
    private EventoService eventoService;

    private Utilizadore criador;
    private Evento evento;
    private CriarEventosDto criarDto;

    @BeforeEach
    void setUp() {
        criador = new Utilizadore();
        criador.setId(1);
        criador.setNome("Coordenador");

        evento = new Evento();
        evento.setId(10);
        evento.setNome("Workshop");
        evento.setDescricao("Descrição");
        evento.setDataEvento(LocalDate.now().plusDays(5));
        evento.setHoraInicio(LocalTime.of(9, 0));
        evento.setHoraFim(LocalTime.of(11, 0));
        evento.setLocal("Sala A");
        evento.setPreco(BigDecimal.ZERO);
        evento.setMaxParticipantes(20);
        evento.setCriadoPor(criador);

        criarDto = new CriarEventosDto(
                "Workshop", "Descrição",
                LocalDate.now().plusDays(5),
                LocalTime.of(9, 0), LocalTime.of(11, 0),
                "Sala A", BigDecimal.ZERO, 20, null
        );
    }

    // ─── findById ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById deve retornar DTO quando evento existe")
    void findById_existente_deveRetornarDto() throws Exception {
        when(idHasher.decode("hashEvento")).thenReturn(10);
        when(eventoRepository.findById(10)).thenReturn(Optional.of(evento));
        when(idHasher.encode(10)).thenReturn("hashEvento");
        when(idHasher.encode(1)).thenReturn("hashCriador");
        when(participantesEventoRepository.countByEvento_Id(10)).thenReturn(3L);

        EventoDto resultado = eventoService.findById("hashEvento");

        assertNotNull(resultado);
        assertEquals("hashEvento", resultado.id());
        assertEquals("Workshop", resultado.nome());
        assertEquals("3", resultado.numInscritos());
    }

    @Test
    @DisplayName("findById deve lançar excepção quando evento não existe")
    void findById_inexistente_deveLancarExcecao() {
        when(idHasher.decode("hashErrado")).thenReturn(99);
        when(eventoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> eventoService.findById("hashErrado"));
    }

    // ─── findEventosFuturos ───────────────────────────────────────────────────

    @Test
    @DisplayName("findEventosFuturos deve retornar lista de eventos futuros")
    void findEventosFuturos_deveRetornarLista() {
        when(eventoRepository.findByDataEventoAfterOrderByDataEventoAsc(any(LocalDate.class)))
                .thenReturn(List.of(evento));
        when(idHasher.encode(10)).thenReturn("hashEvento");
        when(idHasher.encode(1)).thenReturn("hashCriador");
        when(participantesEventoRepository.countByEvento_Id(10)).thenReturn(0L);

        List<EventoDto> resultado = eventoService.findEventosFuturos();

        assertEquals(1, resultado.size());
        assertEquals("Workshop", resultado.get(0).nome());
    }

    // ─── criarEvento ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("criarEvento deve guardar evento e retornar DTO")
    void criarEvento_comDadosValidos_deveGuardarERetornarDto() throws Exception {
        EstadoAula estado = new EstadoAula();
        when(idHasher.decode("hashCriador")).thenReturn(1);
        when(utilizadoreRepository.findById(1)).thenReturn(Optional.of(criador));
        when(estadoAulaRepository.findById(3)).thenReturn(Optional.of(estado));
        when(eventoRepository.save(any(Evento.class))).thenReturn(evento);
        when(idHasher.encode(10)).thenReturn("hashEvento");
        when(idHasher.encode(1)).thenReturn("hashCriador");
        when(participantesEventoRepository.countByEvento_Id(10)).thenReturn(0L);

        EventoDto resultado = eventoService.criarEvento("hashCriador", criarDto);

        assertNotNull(resultado);
        assertEquals("Workshop", resultado.nome());
        verify(eventoRepository).save(any(Evento.class));
    }

    @Test
    @DisplayName("criarEvento com participantes deve inscrever cada um")
    void criarEvento_comParticipantes_deveInscreverCadaUm() throws Exception {
        CriarEventosDto dtoComParticipantes = new CriarEventosDto(
                "Workshop", "Desc", LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                "Sala B", BigDecimal.ZERO, 10,
                List.of("hashP1", "hashP2")
        );

        Utilizadore p1 = new Utilizadore(); p1.setId(2);
        Utilizadore p2 = new Utilizadore(); p2.setId(3);
        EstadoAula estado = new EstadoAula();

        when(idHasher.decode("hashCriador")).thenReturn(1);
        when(idHasher.decode("hashP1")).thenReturn(2);
        when(idHasher.decode("hashP2")).thenReturn(3);
        when(utilizadoreRepository.findById(1)).thenReturn(Optional.of(criador));
        when(utilizadoreRepository.findById(2)).thenReturn(Optional.of(p1));
        when(utilizadoreRepository.findById(3)).thenReturn(Optional.of(p2));
        when(estadoAulaRepository.findById(3)).thenReturn(Optional.of(estado));
        when(eventoRepository.save(any())).thenReturn(evento);
        when(idHasher.encode(anyInt())).thenReturn("hash");
        when(participantesEventoRepository.countByEvento_Id(any())).thenReturn(2L);

        eventoService.criarEvento("hashCriador", dtoComParticipantes);

        verify(participantesEventoRepository, times(2)).save(any(ParticipantesEvento.class));
    }

    @Test
    @DisplayName("criarEvento deve lançar excepção se criador não existir")
    void criarEvento_criadorInexistente_deveLancarExcecao() {
        when(idHasher.decode("hashErrado")).thenReturn(99);
        when(utilizadoreRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> eventoService.criarEvento("hashErrado", criarDto));
        verify(eventoRepository, never()).save(any());
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete deve remover evento existente")
    void delete_existente_deveRemover() throws Exception {
        when(idHasher.decode("hashEvento")).thenReturn(10);
        when(eventoRepository.existsById(10)).thenReturn(true);

        eventoService.delete("hashEvento");

        verify(eventoRepository).deleteById(10);
    }

    @Test
    @DisplayName("delete deve lançar excepção se evento não existir")
    void delete_inexistente_deveLancarExcecao() {
        when(idHasher.decode("hashEvento")).thenReturn(10);
        when(eventoRepository.existsById(10)).thenReturn(false);

        assertThrows(Exception.class, () -> eventoService.delete("hashEvento"));
        verify(eventoRepository, never()).deleteById(any());
    }

    // ─── adicionarParticipante ────────────────────────────────────────────────

    @Test
    @DisplayName("adicionarParticipante deve guardar inscrição quando utilizador não está inscrito")
    void adicionarParticipante_novoUtilizador_deveGuardar() throws Exception {
        Utilizadore participante = new Utilizadore(); participante.setId(2);

        when(idHasher.decode("hashEvento")).thenReturn(10);
        when(idHasher.decode("hashUser")).thenReturn(2);
        when(participantesEventoRepository.existsByEventoIdAndUtilizadorId(10, 2)).thenReturn(false);
        when(eventoRepository.findById(10)).thenReturn(Optional.of(evento));
        when(utilizadoreRepository.findById(2)).thenReturn(Optional.of(participante));

        eventoService.adicionarParticipante("hashEvento", "hashUser");

        verify(participantesEventoRepository).save(any(ParticipantesEvento.class));
    }

    @Test
    @DisplayName("adicionarParticipante deve lançar excepção se já está inscrito")
    void adicionarParticipante_jaInscrito_deveLancarExcecao() {
        when(idHasher.decode("hashEvento")).thenReturn(10);
        when(idHasher.decode("hashUser")).thenReturn(2);
        when(participantesEventoRepository.existsByEventoIdAndUtilizadorId(10, 2)).thenReturn(true);

        assertThrows(Exception.class,
                () -> eventoService.adicionarParticipante("hashEvento", "hashUser"));
        verify(participantesEventoRepository, never()).save(any());
    }

    // ─── removerParticipante ──────────────────────────────────────────────────

    @Test
    @DisplayName("removerParticipante deve apagar inscrição existente")
    void removerParticipante_inscrito_deveApagar() throws Exception {
        when(idHasher.decode("hashEvento")).thenReturn(10);
        when(idHasher.decode("hashUser")).thenReturn(2);
        when(participantesEventoRepository.existsByEventoIdAndUtilizadorId(10, 2)).thenReturn(true);

        eventoService.removerParticipante("hashEvento", "hashUser");

        verify(participantesEventoRepository).deleteByEventoIdAndUtilizadorId(10, 2);
    }

    @Test
    @DisplayName("removerParticipante deve lançar excepção se não está inscrito")
    void removerParticipante_naoInscrito_deveLancarExcecao() {
        when(idHasher.decode("hashEvento")).thenReturn(10);
        when(idHasher.decode("hashUser")).thenReturn(2);
        when(participantesEventoRepository.existsByEventoIdAndUtilizadorId(10, 2)).thenReturn(false);

        assertThrows(Exception.class,
                () -> eventoService.removerParticipante("hashEvento", "hashUser"));
        verify(participantesEventoRepository, never()).deleteByEventoIdAndUtilizadorId(any(), any());
    }

    // ─── inscreverParticipante ────────────────────────────────────────────────

    @Test
    @DisplayName("inscreverParticipante deve reativar inscrição cancelada")
    void inscreverParticipante_inscricaoCancelada_deveReativar() throws Exception {
        ParticipantesEvento pe = new ParticipantesEvento();
        pe.setCancelado(true);
        ParticipantesEventoId idComposto = new ParticipantesEventoId(10, 2);

        when(idHasher.decode("hashEvento")).thenReturn(10);
        when(idHasher.decode("hashUser")).thenReturn(2);
        when(participantesEventoRepository.findById(any())).thenReturn(Optional.of(pe));

        eventoService.inscreverParticipante("hashEvento", "hashUser");

        assertFalse(pe.getCancelado());
        verify(participantesEventoRepository).save(pe);
    }

    @Test
    @DisplayName("inscreverParticipante deve lançar excepção se já está activo")
    void inscreverParticipante_jaActivo_deveLancarExcecao() {
        ParticipantesEvento pe = new ParticipantesEvento();
        pe.setCancelado(false);

        when(idHasher.decode("hashEvento")).thenReturn(10);
        when(idHasher.decode("hashUser")).thenReturn(2);
        when(participantesEventoRepository.findById(any())).thenReturn(Optional.of(pe));

        assertThrows(Exception.class,
                () -> eventoService.inscreverParticipante("hashEvento", "hashUser"));
    }

    // ─── listarParticipantes ──────────────────────────────────────────────────

    @Test
    @DisplayName("listarParticipantes deve retornar DTOs com dados corretos")
    void listarParticipantes_deveRetornarDtos() {
        Utilizadore u = new Utilizadore();
        u.setNome("Ana"); u.setEmail("ana@escola.com");

        ParticipantesEvento pe = new ParticipantesEvento();
        pe.setUtilizador(u);
        pe.setPago(true);
        pe.setCancelado(false);

        when(idHasher.decode("hashEvento")).thenReturn(10);
        when(participantesEventoRepository.findByEventoId(10)).thenReturn(List.of(pe));

        List<ParticipanteDto> resultado = eventoService.listarParticipantes("hashEvento");

        assertEquals(1, resultado.size());
        assertEquals("Ana", resultado.get(0).utilizadorNome());
        assertTrue(resultado.get(0).pago());
        assertFalse(resultado.get(0).cancelado());
    }
}