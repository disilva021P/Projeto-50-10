package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.HorarioTurmaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.EstudioModalidadeRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.EstudioRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@RequiredArgsConstructor
@Service
public class AulaService {
    private final EstudioService estudioService;
    private final EstudioModalidadeRepository estudioModalidadeRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final AulaRepository aulaRepository;
    private final IdHasher idHasher;
    private final EstadoAuloService estadoAuloService;

    //region feito
    // -------------------------------------------------------------------------
    // CRUD base
    // -------------------------------------------------------------------------

    /** Devolve todas as aulas sem paginação. */
    public List<Aula> findAll() {
        return aulaRepository.findAll();
    }

    /** Devolve todas as aulas com paginação. */
    public PagedModel<Aula> findAll(Pageable paginacao) {
        Page<Aula> page = aulaRepository.findAll(paginacao);
        return new PagedModel<>(page);
    }

    /** Procura uma aula pelo seu ID interno. */
    public Optional<Aula> buscarPorId(Integer id) {
        return aulaRepository.findById(id);
    }

    /** Cria ou atualiza uma aula. */
    public Aula salvar(Aula aula) {
        return aulaRepository.save(aula);
    }

    /** Elimina uma aula pelo seu ID interno. */
    public void eliminar(Integer id) {
        aulaRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Queries por data
    // -------------------------------------------------------------------------

    /**
     * Devolve todas as aulas de um dia específico.
     * Se {@code data} for null usa o dia de hoje.
     */
    public List<AulaDto> findByDataAula(LocalDate data) {
        LocalDate dia = (data != null) ? data : LocalDate.now();
        return aulaRepository.findByDataAula(dia).stream().map(this::converterParaDto).toList();
    }

    // -------------------------------------------------------------------------
    // Horários do aluno
    // -------------------------------------------------------------------------

    /**
     * Devolve as aulas de um aluno num dia específico.
     *
     * @param dataAula data pretendida
     * @param userId   ID hasheado do utilizador
     */
    public List<AulaDto> buscarAulaporEmail_Data(LocalDate dataAula, String userId) {
        Utilizadore utilizador = encontraUtilizador(userId);
        List<Aula> aulas = aulaRepository.findByDataEAluno(dataAula, utilizador.getId());
        return converterListaAulaParaAulaDto(aulas);
    }

    /**
     * Devolve o horário semanal de um aluno.
     *
     * @param userId ID hasheado do utilizador
     * @param offset 0 = semana atual, 1 = semana seguinte
     */
    public List<AulaDto> buscarHorarioSemana(String userId, int offset) {
        encontraUtilizador(userId); // valida existência
        LocalDate inicioSemana = calcularInicioSemana(offset);
        LocalDate fimSemana = inicioSemana.plusDays(6);
        return aulaRepository.buscarHorarioDoAluno(idHasher.decode(userId), inicioSemana, fimSemana)
                .stream()
                .map(this::converterParaDto)
                .toList();
    }

    /**
     * Devolve uma aula específica de um aluno, verificando se este faz parte dela.
     *
     * @param userId ID hasheado do utilizador
     * @param aulaId ID hasheado da aula
     * @return Optional com o DTO da aula, ou vazio se o aluno não estiver inscrito
     */
    public AulaDto buscarAulaAluno(String userId, String aulaId) {
        Utilizadore utilizador = encontraUtilizador(userId);
        Integer aulaIdDecoded = idHasher.decode(aulaId);
        Optional<Aula> aula = aulaRepository.findAulaByIdAndAlunoId(aulaIdDecoded,utilizador.getId());
        if(aula.isEmpty()) throw new RuntimeException("Aula/Aluno não coincidem");
        return converterParaDto(aula.get());
    }

    // -------------------------------------------------------------------------
    // Conversão
    // -------------------------------------------------------------------------

    /** Converte uma lista de {@link Aula} para uma lista de {@link AulaDto}. */
    public List<AulaDto> converterListaAulaParaAulaDto(List<Aula> aulas) {
        return aulas.stream()
                .map(this::converterParaDto)
                .toList();
    }

    /** Converte uma {@link Aula} para {@link AulaDto}, codificando o ID. */
    public AulaDto converterParaDto(Aula aula) {
        return new AulaDto(
                idHasher.encode(aula.getId()),
                aula.getModalidade(),
                converterEstudioParaDto(aula.getEstudio()),
                aula.getDuracaoMinutos(),
                aula.getDataAula(),
                aula.getHoraInicio(),
                aula.getHoraFim(),
                aula.getEstado(),
                aula.getCriadoPor(),
                aula.getNotas()
        );

    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /**
     * Resolve um utilizador a partir do ID hasheado.
     *
     * @throws RuntimeException se o utilizador não existir
     */
    private Utilizadore encontraUtilizador(String userId) {
        return utilizadoreRepository.findById(idHasher.decode(userId))
                .orElseThrow(() -> new RuntimeException("Erro a encontrar utilizador com id: " + userId));
    }

    /**
     * Calcula o domingo que inicia a semana com base no offset.
     *
     * @param offset 0 = semana atual, positivo = semanas futuras
     */
    private LocalDate calcularInicioSemana(int offset) {
        return LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                .plusWeeks(offset);
    }
    //endregion

    public List<AulaDto> GerarAulasComHorario(HorarioTurmaDto horario) {
        List<AulaDto> erros = new ArrayList<>();
        DayOfWeek diaAlvo = DayOfWeek.of(horario.diaSemana());
        LocalDate dataAtual = horario.dataInicio();
        LocalDate dataFim = horario.dataValidade();

        // 1. Encontrar o primeiro dia válido
        while (dataAtual.getDayOfWeek() != diaAlvo && !dataAtual.isAfter(dataFim)) {
            dataAtual = dataAtual.plusDays(1);
        }

        // 2. Tentar criar cada aula no intervalo
        while (!dataAtual.isAfter(dataFim)) {
            AulaDto novaAula = horarioParaAulaDto(horario, dataAtual);
            try {
                Aula aulaGuardada = criarAula(novaAula);
            } catch (Exception e) {
                erros.add(novaAula);
            }

            dataAtual = dataAtual.plusWeeks(1);
        }

        return erros;
    }
    public Aula criarAula(AulaDto aulaDto) throws Exception {
        boolean conflito = aulaRepository.existeConflitoNoEstudio(
                idHasher.decode(aulaDto.estudio().id()),  // use getId() not .id()
                aulaDto.dataAula(),
                aulaDto.horaInicio(),
                aulaDto.horaFim()
        );
        if (conflito) {
            throw new RuntimeException("Conflito de horário: O estúdio '" +
                    aulaDto.estudio().nome() + "' já tem uma aula agendada entre " +
                    aulaDto.horaInicio() + " e " + aulaDto.horaFim() +
                    " no dia " + aulaDto.dataAula());
        }

        estudioModalidadeRepository
                .findByEstudio_IdAndModalidade_Id(
                        idHasher.decode(aulaDto.estudio().id()),
                        aulaDto.modalidade().getId())
                .orElseThrow(() -> new RuntimeException("Este estúdio não permite esta modalidade!"));

        // Convert DTO → entity and save
        Aula aula = aulaDTOparaAula(aulaDto);
        return aulaRepository.save(aula);
    }

    public AulaDto horarioParaAulaDto(HorarioTurmaDto horario, LocalDate dia){

        return new AulaDto(
                null,
                horario.idturma().getModalidade(),
                horario.estudioId(),
                horario.duracaoMinutos(),
                dia,
                horario.horaInicio(),
                horario.horaFim(),
                estadoAuloService.findbyId(3),
                horario.criadoPor(),
                "Aula criada por Horário Fixo"
        );
    }
    public Aula aulaDTOparaAula(AulaDto aulaDto) throws Exception {
        if (aulaDto == null) return null;

        Aula aula = new Aula();
        aula.setEstudio( estudioService.findEstudiobyId(idHasher.decode(aulaDto.estudio().id())));
        aula.setDuracaoMinutos(aulaDto.duracaoMinutos());
        aula.setDataAula(aulaDto.dataAula());
        aula.setHoraInicio(aulaDto.horaInicio());
        aula.setHoraFim(aulaDto.horaFim());
        aula.setNotas(aulaDto.notas());

        // Resolve criadoPor from DB if provided
        if (aulaDto.criadoPor() != null) {
            utilizadoreRepository.findById(idHasher.decode(aulaDto.criadoPor().id()))
                    .ifPresent(aula::setCriadoPor);
        }

        return aula;
    }
}