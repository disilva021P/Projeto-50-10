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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AulaFixaServiceTest {

    @Mock private HorarioFixoRepository horarioFixoRepository;
    @Mock private IdHasher idHasher;
    @Mock private UtilizadoreRepository utilizadoreRepository;
    @Mock private TurmaService turmaService;
    @Mock private EstudioRepository estudioRepository;
    @Mock private TurmaRepository turmaRepository;
    @Mock private ModalidadeService modalidadeService;
    @Mock private EstudioService estudioService;
    @Mock private UtilizadorService utilizadorService;

    private AulaFixaService aulaFixaService;

    @BeforeEach
    void setUp() {
        aulaFixaService = new AulaFixaService(
                horarioFixoRepository,
                idHasher,
                utilizadoreRepository,
                turmaService,
                estudioRepository,
                turmaRepository,
                modalidadeService,
                estudioService,
                utilizadorService
        );
    }

    // =========================================================================
    // Auxiliares
    // =========================================================================

    private HorarioTurma criarHorario(int id) {
        Utilizadore criador = new Utilizadore();
        criador.setId(1); criador.setNome("Admin");

        Turma turma = new Turma(); turma.setId(2);
        Estudio estudio = new Estudio(); estudio.setId(3);

        HorarioTurma h = new HorarioTurma();
        h.setId(id);
        h.setCriadoPor(criador);
        h.setIdturma(turma);
        h.setEstudioId(estudio);
        h.setDataInicio(LocalDate.of(2025, 1, 6));
        h.setDataValidade(LocalDate.of(2025, 12, 31));
        h.setDiaSemana(DayOfWeek.MONDAY.getValue());
        h.setDuracaoMinutos(60);
        h.setHoraInicio(LocalTime.of(9, 0));
        h.setHoraFim(LocalTime.of(10, 0));
        return h;
    }

    private HorarioTurmaDto criarHorarioDto(String id, String turmaHashId) {
        UtilizadoreResumoDto criador = new UtilizadoreResumoDto("criadorHash", "Admin");
        TurmaDto turmaDto = mock(TurmaDto.class);
        lenient().when(turmaDto.id()).thenReturn(turmaHashId);

        EstudioDto estudioDto = mock(EstudioDto.class);
        lenient().when(estudioDto.id()).thenReturn("estHash");

        return new HorarioTurmaDto(
                id,
                criador,
                turmaDto,
                LocalDate.of(2025, 1, 6),
                LocalDate.of(2025, 12, 31),
                DayOfWeek.MONDAY.getValue(),
                60,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                estudioDto
        );
    }

    // =========================================================================
    // findAll (paginado)
    // =========================================================================

    @Test
    @DisplayName("findAll – deve devolver página com horários convertidos")
    void findAll_Sucesso() {
        HorarioTurma h = criarHorario(1);
        Page<HorarioTurma> page = new PageImpl<>(List.of(h));
        Pageable pageable = PageRequest.of(0, 10);

        when(horarioFixoRepository.findAll(pageable)).thenReturn(page);
        when(idHasher.encode(anyInt())).thenReturn("hashId");
        when(turmaService.converterTurmaParaDto(any())).thenReturn(mock(TurmaDto.class));
        when(estudioService.converterParaDto(any())).thenReturn(mock(EstudioDto.class));

        var resultado = aulaFixaService.findAll(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(horarioFixoRepository).findAll(pageable);
    }

    // =========================================================================
    // findById
    // =========================================================================

    @Test
    @DisplayName("findById – deve devolver DTO quando horário existe")
    void findById_Sucesso() throws Exception {
        HorarioTurma h = criarHorario(5);

        when(idHasher.decode("hash5")).thenReturn(5);
        when(horarioFixoRepository.findById(5)).thenReturn(Optional.of(h));
        when(idHasher.encode(anyInt())).thenReturn("someHash");
        when(turmaService.converterTurmaParaDto(any())).thenReturn(mock(TurmaDto.class));
        when(estudioService.converterParaDto(any())).thenReturn(mock(EstudioDto.class));

        HorarioTurmaDto resultado = aulaFixaService.findById("hash5");

        assertNotNull(resultado);
        verify(horarioFixoRepository).findById(5);
    }

    @Test
    @DisplayName("findById – deve lançar exceção quando horário não existe")
    void findById_NaoExiste_Erro() {
        when(idHasher.decode("invalido")).thenReturn(99);
        when(horarioFixoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> aulaFixaService.findById("invalido"));
    }

    // =========================================================================
    // findByIdTurma
    // =========================================================================

    @Test
    @DisplayName("findByIdTurma – deve devolver lista de horários da turma")
    void findByIdTurma_Sucesso() throws Exception {
        HorarioTurma h = criarHorario(10);

        when(idHasher.decode("turmaHash")).thenReturn(2);
        when(horarioFixoRepository.findAllByIdturma_Id(2)).thenReturn(List.of(h));
        when(idHasher.encode(anyInt())).thenReturn("encoded");
        when(turmaService.converterTurmaParaDto(any())).thenReturn(mock(TurmaDto.class));
        when(estudioService.converterParaDto(any())).thenReturn(mock(EstudioDto.class));

        List<HorarioTurmaDto> resultado = aulaFixaService.findByIdTurma("turmaHash");

        assertEquals(1, resultado.size());
        verify(horarioFixoRepository).findAllByIdturma_Id(2);
    }

    // =========================================================================
    // findHorariosPorTurmas
    // =========================================================================

    @Test
    @DisplayName("findHorariosPorTurmas – deve agrupar horários por turma corretamente")
    void findHorariosPorTurmas_Sucesso() {
        // ARRANGE
        int idTurmaInteiro = 2;
        String hashTurma = "turmaHash1";
        HorarioTurma h = criarHorario(20);
        // Certifica-te que o HorarioTurma 'h' tem a Turma com ID 2 lá dentro no criarHorario

        TurmaDto turmaDto = mock(TurmaDto.class);
        // Usamos lenient() porque o id() do DTO pode não ser chamado pelo Service,
        // mas o objeto turmaDto será usado como chave no Map.
        lenient().when(turmaDto.id()).thenReturn(hashTurma);

        when(idHasher.decode(hashTurma)).thenReturn(idTurmaInteiro);
        when(horarioFixoRepository.findAllByIdturma_IdIn(List.of(idTurmaInteiro))).thenReturn(List.of(h));

        // Configurações para a conversão final dos DTOs que vão para dentro da lista do Map
        when(idHasher.encode(anyInt())).thenReturn("encoded");
        when(turmaService.converterTurmaParaDto(any())).thenReturn(turmaDto);
        when(estudioService.converterParaDto(any())).thenReturn(mock(EstudioDto.class));

        // ACT
        Map<TurmaDto, List<HorarioTurmaDto>> resultado =
                aulaFixaService.findHorariosPorTurmas(List.of(hashTurma));

        // ASSERT
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty(), "O mapa não deve estar vazio");
        assertTrue(resultado.containsKey(turmaDto), "O mapa deve conter a turmaDto como chave");
        assertEquals(1, resultado.get(turmaDto).size(), "A lista da turma deve ter 1 horário");

        // VERIFY
        verify(horarioFixoRepository).findAllByIdturma_IdIn(List.of(idTurmaInteiro));
    }
    // =========================================================================
    // save
    // =========================================================================

    @Test
    @DisplayName("save – deve persistir novo horário com sucesso")
    void save_Sucesso() throws Exception {
        HorarioTurmaDto dto = criarHorarioDto("", "turmaHash");
        HorarioTurma entidade = criarHorario(0);

        Utilizadore utilizadore = new Utilizadore(); utilizadore.setId(1);
        Turma turma = new Turma(); turma.setId(2);
        Estudio estudio = new Estudio(); estudio.setId(3);

        when(idHasher.decode("criadorHash")).thenReturn(1);
        when(idHasher.decode("turmaHash")).thenReturn(2);
        when(idHasher.decode("estHash")).thenReturn(3);
        when(utilizadoreRepository.findById(1)).thenReturn(Optional.of(utilizadore));
        when(turmaRepository.findById(2)).thenReturn(Optional.of(turma));
        when(estudioRepository.findById(3)).thenReturn(Optional.of(estudio));
        when(horarioFixoRepository.save(any(HorarioTurma.class))).thenReturn(entidade);

        HorarioTurma resultado = aulaFixaService.save(dto);

        assertNotNull(resultado);
        verify(horarioFixoRepository).save(any(HorarioTurma.class));
    }

    @Test
    @DisplayName("save – deve lançar exceção quando utilizador criador não existe")
    void save_UtilizadorNaoExiste_Erro() {
        HorarioTurmaDto dto = criarHorarioDto("", "turmaHash");

        when(idHasher.decode("criadorHash")).thenReturn(1);
        when(utilizadoreRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> aulaFixaService.save(dto));
        verify(horarioFixoRepository, never()).save(any());
    }

    // =========================================================================
    // update
    // =========================================================================

    @Test
    @DisplayName("update – deve atualizar campos do horário existente")
    void update_Sucesso() throws Exception {
        // ARRANGE
        int idHorario = 7;
        int idEstudio = 3;

        HorarioTurma existente = criarHorario(idHorario);

        // 1. Criamos o DTO.
        // Internamente, o teu método criarHorarioDto vai pôr "estHash" no EstudioDto.
        HorarioTurmaDto dto = criarHorarioDto("hash7", "turmaHash");

        Estudio estudio = new Estudio();
        estudio.setId(idEstudio);

        // 2. CONFIGURAÇÃO DOS MOCKS
        when(idHasher.decode("hash7")).thenReturn(idHorario);

        // IMPORTANTE: Aqui usamos "estHash" porque é o que o DTO devolve no estudioDto.id()
        when(idHasher.decode("estHash")).thenReturn(idEstudio);

        when(horarioFixoRepository.findById(idHorario)).thenReturn(Optional.of(existente));
        when(estudioService.findEstudiobyId(idEstudio)).thenReturn(estudio);
        when(horarioFixoRepository.save(any(HorarioTurma.class))).thenReturn(existente);

        // ACT
        HorarioTurma resultado = aulaFixaService.update("hash7", dto);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(DayOfWeek.MONDAY.getValue(), existente.getDiaSemana());

        // VERIFY
        verify(horarioFixoRepository).save(existente);
    }

    @Test
    @DisplayName("update – deve lançar exceção quando horário não existe")
    void update_NaoExiste_Erro() {
        HorarioTurmaDto dto = criarHorarioDto("badHash", "turmaHash");

        when(idHasher.decode("badHash")).thenReturn(99);
        when(horarioFixoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> aulaFixaService.update("badHash", dto));
        verify(horarioFixoRepository, never()).save(any());
    }

    @Test
    @DisplayName("update – deve lançar RuntimeException quando estúdio não existe")
    void update_EstudioNaoExiste_Erro() throws Exception {
        HorarioTurma existente = criarHorario(7);
        HorarioTurmaDto dto = criarHorarioDto("hash7", "turmaHash");

        when(idHasher.decode("hash7")).thenReturn(7);
        when(idHasher.decode("estHash")).thenReturn(3);
        when(horarioFixoRepository.findById(7)).thenReturn(Optional.of(existente));
        when(estudioService.findEstudiobyId(3)).thenThrow(new Exception("Estudio não encontrado"));

        assertThrows(RuntimeException.class, () -> aulaFixaService.update("hash7", dto));
    }

    // =========================================================================
    // delete (String)
    // =========================================================================

    @Test
    @DisplayName("delete – deve eliminar horário quando existe")
    void delete_PorHash_Sucesso() throws Exception {
        when(idHasher.decode("delHash")).thenReturn(15);
        when(horarioFixoRepository.existsById(15)).thenReturn(true);

        aulaFixaService.delete("delHash");

        verify(horarioFixoRepository).deleteById(15);
    }

    @Test
    @DisplayName("delete – deve lançar exceção quando horário não existe (hash)")
    void delete_PorHash_NaoExiste_Erro() {
        when(idHasher.decode("badHash")).thenReturn(99);
        when(horarioFixoRepository.existsById(99)).thenReturn(false);

        assertThrows(Exception.class, () -> aulaFixaService.delete("badHash"));
        verify(horarioFixoRepository, never()).deleteById(anyInt());
    }

    // =========================================================================
    // delete (Integer)
    // =========================================================================

    @Test
    @DisplayName("delete – deve eliminar horário quando existe (ID inteiro)")
    void delete_PorId_Sucesso() throws Exception {
        when(horarioFixoRepository.existsById(20)).thenReturn(true);

        aulaFixaService.delete(20);

        verify(horarioFixoRepository).deleteById(20);
    }

    @Test
    @DisplayName("delete – deve lançar exceção quando horário não existe (ID inteiro)")
    void delete_PorId_NaoExiste_Erro() {
        when(horarioFixoRepository.existsById(99)).thenReturn(false);

        assertThrows(Exception.class, () -> aulaFixaService.delete(99));
        verify(horarioFixoRepository, never()).deleteById(anyInt());
    }

    // =========================================================================
    // convertToDto / fromDtoToHorarioTurma (nulos)
    // =========================================================================

    @Test
    @DisplayName("convertToDto – deve devolver null quando entidade é null")
    void convertToDto_Null_DevolvNull() {
        assertNull(aulaFixaService.convertToDto(null));
    }

    @Test
    @DisplayName("fromDtoToHorarioTurma – deve devolver null quando DTO é null")
    void fromDtoToHorarioTurma_Null_DevolvNull() throws Exception {
        assertNull(aulaFixaService.fromDtoToHorarioTurma(null));
    }
}