package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.HorarioTurmaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Objects;
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
    private final ModalidadeService modalidadeService;
    private final AulaFixaService aulaFixaService;
    private final HorarioFixoRepository horarioFixoRepository;
    private final EncarregadoAlunoRepository encarregadoAlunoRepository;
    private final UtilizadorService utilizadorService;
    //region feito
    // -------------------------------------------------------------------------
    // CRUD base
    // -------------------------------------------------------------------------

    /** Devolve todas as aulas sem paginação. */
    public List<AulaDto> findAll() {
        return aulaRepository.findAll().stream().map(this::converterParaDto).toList();
    }

    /** Devolve todas as aulas com paginação. */
    public PagedModel<AulaDto> findAll(Pageable paginacao) {
        Page<AulaDto> page = aulaRepository.findAll(paginacao).map(this::converterParaDto);
        return new PagedModel<>(page);
    }

    /** Procura uma aula pelo seu ID. */
    public Optional<Aula> buscarPorId(Integer id) {
        return aulaRepository.findById(id);
    }
    public Optional<Aula> buscarPorId(String id) {
        return aulaRepository.findById(idHasher.decode(id));
    }
    public Aula bucarPorId(Integer id) throws Exception {
        return aulaRepository.findById(id).orElseThrow(()-> new Exception("Aula não encontrada"));
    }
    public Aula bucarPorId(String id) throws Exception {
        return aulaRepository.findById(idHasher.decode( id)).orElseThrow(()-> new Exception("Aula não encontrada"));
    }
    public AulaDto bucarPorIdDto(Integer id) throws Exception {
        return converterParaDto(aulaRepository.findById(id).orElseThrow(()-> new Exception("Aula não encontrada")));
    }
    public AulaDto bucarPorIdDto(String id) throws Exception {
        return converterParaDto(aulaRepository.findById(idHasher.decode( id)).orElseThrow(()-> new Exception("Aula não encontrada")));
    }
    /** Cria ou atualiza uma aula. */
    public Aula salvar(Aula aula) {
        return aulaRepository.save(aula);
    }

    /** Elimina uma aula pelo seu ID interno. */
    public void eliminar(Integer id) {
        aulaRepository.deleteById(id);
    }
    public void eliminar(String id) {
        aulaRepository.deleteById(idHasher.decode(id));
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
    public List<AulaDto> buscarAulaporId_Data(LocalDate dataAula, String userId) throws Exception {
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
    public List<AulaDto> buscarHorarioSemana(String userId, int offset) throws Exception {
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
    public AulaDto buscarAulaAluno(String userId, String aulaId) throws Exception {
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
        if(aula== null) return null;
        Utilizadore u = aula.getCriadoPor();
        return new AulaDto(
                idHasher.encode(aula.getId()),
                estudioService.converterParaDto(aula.getEstudio()),
                aula.getDuracaoMinutos(),
                aula.getDataAula(),
                aula.getHoraInicio(),
                aula.getHoraFim(),
                new UtilizadoreResumoDto(idHasher.encode(u.getId()),u.getNome())
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
    private Utilizadore encontraUtilizador(String userId) throws Exception {
        return utilizadoreRepository.findById(idHasher.decode(userId))
                .orElseThrow(() -> new Exception("Erro a encontrar utilizador com id: " + userId));
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

    /**
     *
     * @param horario
     * @return Lista de erros
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public List<AulaDto> GerarAulasComHorario(HorarioTurmaDto horario) throws Exception {
        if (horario.dataInicio().isAfter(horario.dataValidade())) {
            throw new Exception("Data de início superior à Data final");
        }

        List<AulaDto> erros = new ArrayList<>();
        List<Aula> adicionados = new ArrayList<>();

        // 1. Criar primeiro o cabeçalho do horário
        HorarioTurma horarioTurma = aulaFixaService.save(horario);

        DayOfWeek diaAlvo = DayOfWeek.of(horario.diaSemana());
        LocalDate dataAtual = horario.dataInicio();
        LocalDate dataFim = horario.dataValidade();

        // Encontrar o primeiro dia válido
        while (dataAtual.getDayOfWeek() != diaAlvo && !dataAtual.isAfter(dataFim)) {
            dataAtual = dataAtual.plusDays(1);
        }

        Integer idModalidade = idHasher.decode(horario.idturma().modalidade().id());

        // 2. Tentar criar cada aula no intervalo
        while (!dataAtual.isAfter(dataFim)) {
            AulaDto novaAula = horarioParaAulaDto(horario, dataAtual);
            try {
                // Assume-se que criarAula já faz a lógica de negócio e devolve a entidade
                Aula aulaGuardada = criarAula(novaAula, idModalidade);

                // Associar o horário à aula antes de adicionar à lista
                aulaGuardada.setIdHorario(horarioTurma);
                adicionados.add(aulaGuardada);
            } catch (Exception e) {
                erros.add(novaAula);
            }
            dataAtual = dataAtual.plusWeeks(1);
        }

        if (adicionados.isEmpty()) {
            // O @Transactional fará o rollback do horarioTurma guardado acima
            throw new Exception("Erro: Nenhuma aula pôde ser criada no intervalo selecionado.");
        }

        // 3. Gravação em lote (muito mais rápido)
        aulaRepository.saveAll(adicionados);

        return erros;
    }
    public void EliminarAulasComHorario(Integer idHorario) throws Exception {
        aulaFixaService.delete(idHorario);
        aulaRepository.deleteAllByIdHorario_Id(idHorario);
    }
    public void EliminarAulasComHorario(String idHorario) throws Exception {
        aulaFixaService.delete(idHasher.decode(idHorario));
        aulaRepository.deleteAllByIdHorario_Id(idHasher.decode(idHorario));
    }
    public List<AulaDto> atualizaPorHorario(HorarioTurmaDto horarioTurmaDto) throws Exception {

        HorarioTurma horarioTurma = horarioFixoRepository
                .findById(idHasher.decode(horarioTurmaDto.id()))
                .orElseThrow(() -> new Exception("Erro ao encontrar horário"));



        List<Aula> aulasAtualizadas = new ArrayList<>();
        if(!Objects.equals(horarioTurmaDto.diaSemana(), horarioTurma.getDiaSemana())){
            EliminarAulasComHorario(horarioTurma.getId());

            return GerarAulasComHorario(horarioTurmaDto);
        }
        List<Aula> aulas = aulaRepository.findAllByIdHorario_Id(horarioTurma.getId());
        if (aulas.isEmpty()) return new ArrayList<>();
        for (Aula aula : aulas) {

            // Atualizar campos com base no horário
            aula.setHoraInicio(horarioTurmaDto.horaInicio());
            aula.setHoraFim(horarioTurmaDto.horaFim());
            aula.setDuracaoMinutos(horarioTurmaDto.duracaoMinutos());

            aula.setEstudio(
                    estudioService.findEstudiobyId(
                            idHasher.decode(horarioTurmaDto.estudioId().id())
                    )
            );

            aulasAtualizadas.add(aulaRepository.save(aula));
        }

        // Converter para DTO
        return aulasAtualizadas.stream()
                .map(this::converterParaDto)
                .toList();
    }
    public Aula criarAula(AulaDto aulaDto, Integer modalidade) throws Exception {
        boolean conflito = aulaRepository.existeConflitoNoEstudio(
                idHasher.decode(aulaDto.estudio().id()),
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
                        modalidade)
                .orElseThrow(() -> new RuntimeException("Este estúdio não permite esta modalidade!"));

        // Convert DTO → entity and save
        Aula aula = aulaDTOparaAula(aulaDto);
        return aulaRepository.save(aula);
    }

    public AulaDto horarioParaAulaDto(HorarioTurmaDto horario, LocalDate dia){

        return new AulaDto(
                null,
                horario.estudioId(),
                horario.duracaoMinutos(),
                dia,
                horario.horaInicio(),
                horario.horaFim(),
                horario.criadoPor(),
                horario
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

        // Resolve criadoPor from DB if provided
        if (aulaDto.criadoPor() != null) {
            utilizadoreRepository.findById(idHasher.decode(aulaDto.criadoPor().id()))
                    .ifPresent(aula::setCriadoPor);
        }

        return aula;
    }

    public List<AulaDto> devolveAulasEducandos(String id, Integer offset) throws Exception {


        List<UtilizadoreResumoDto> educandos = utilizadorService.findEducandosdeEducador(idHasher.decode(id));

        List<AulaDto> todasAsAulas = new ArrayList<>();
        for (UtilizadoreResumoDto educando : educandos) {
            // O id que vem no DTO já está em String/Hash, passamos direto
            List<AulaDto> aulasDosFilho = buscarHorarioSemana(educando.id(), offset);
            todasAsAulas.addAll(aulasDosFilho);
        }
        return todasAsAulas;
    }
}