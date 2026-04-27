package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import de.jollyday.Holiday;
import de.jollyday.HolidayCalendar;
import de.jollyday.HolidayManager;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.Repository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

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

    private final DisponibilidadeService disponibilidadeService;
    private final EncarregadoAlunoRepository encarregadoAlunoRepository;
    private final UtilizadorService utilizadorService;
    private final TurmaService turmaService;


    public static final int ID_ESTADO_PENDENTE   = 2;
    public static final int ID_ESTADO_AGENDADA = 3;
    public static final int ID_ESTADO_REALIZADA  = 5;
    public static final int ID_ESTADO_CANCELADA  = 4;
    public static final int ID_ESTADO_PENDENTEVALIDACAO= 6;
    public static final int ID_ESTADO_AUTOMATICO = 7;
    public static final int ID_ESTADO_VALIDADA  = 8;
    public static final int ID_ESTADO_CONTABILIZADO  = 9;
    private final AulaCoachingRepository aulaCoachingRepository;
    private final AulaProfessorService aulaProfessorService;
    private final ProfessorService professorService;
    private final AulaAlunoRepository aulaAlunoRepository;
    private final AlunoRepository alunoRepository;
    private final NotificacoesService notificacoesService;
    private final ProfessorModalidadeRepository professorModalidadeRepository;
    private final PagamentoRepository pagamentoRepository;
    private final CancelamentoService cancelamentoService;
    private final TipoPagamentoRepository tipoPagamentoRepository;
    private final FeriadosService feriadosService;
    private final AulaProfessoreRepository aulaProfessoreRepository;
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
        if(aula.isEmpty()) throw new Exception("Aula/Aluno não coincidem");
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
                idHasher.encode(aula.getCriadoPor().getId()),
                aulaFixaService.convertToDto(aula.getIdHorario()),
                estadoAuloService.converterParaDto(aula.getEstado())
        );
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /**
     * Resolve um utilizador a partir do ID hasheado.
     *
     * @throws Exception se o utilizador não existir
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
    public List<AulaDto> GerarAulasComHorario(HorarioTurmaRequestDto horario, String idProfessor) throws Exception {
        // Validações de data
        if (horario.dataInicio().isAfter(horario.dataValidade())) {
            throw new Exception("Data de início superior à Data final");
        }
        if (horario.dataInicio().isBefore(LocalDate.now())) {
            throw new Exception("Data de início inferior à Data atual");
        }

        // 1. Validar se a Turma e Modalidade existem antes de começar
        var turma = turmaService.findById(horario.idturma());
        if (turma == null || turma.modalidade() == null || turma.modalidade().id() == null) {
            throw new Exception("Erro: Turma ou Modalidade não encontrada ou ID nulo.");
        }

        Integer idModalidade = idHasher.decode(turma.modalidade().id());

        List<AulaDto> erros = new ArrayList<>();
        List<Aula> adicionados = new ArrayList<>();
        HorarioTurmaDto horarioDto = aulaFixaService.convertRequestToDto(horario);

        // Salvar cabeçalho
        HorarioTurma horarioTurma = aulaFixaService.save(horarioDto);

        DayOfWeek diaAlvo = DayOfWeek.of(horario.diaSemana());
        LocalDate dataAtual = horario.dataInicio();
        LocalDate dataFim = horario.dataValidade();

        // Encontrar o primeiro dia válido
        while (dataAtual.getDayOfWeek() != diaAlvo && !dataAtual.isAfter(dataFim)) {
            dataAtual = dataAtual.plusDays(1);
        }

        // 2. Loop de criação de aulas
        while (!dataAtual.isAfter(dataFim)) {
            AulaDto novaAula = horarioParaAulaDto(horarioDto, dataAtual);
            try {
                if (feriadosService.isFeriado(novaAula.dataAula())) {
                    throw new Exception("Erro, aula está a ser marcada em feriado");
                }
                System.out.println("PAssoeifae");
                Aula aulaGuardada = criarAula(novaAula, idModalidade);
                System.out.println("qwdqdwqdqd");
                aulaGuardada.setIdHorario(horarioTurma);
                adicionados.add(aulaGuardada);

            } catch (Exception e) {
                erros.add(novaAula);
                // Isso explica o erro aparecer várias vezes: ele falha para cada semana
                System.out.println("Erro na data " + dataAtual + ": " + e.getMessage());
            }
            dataAtual = dataAtual.plusWeeks(1);
        }

        if (adicionados.isEmpty()) {
            throw new Exception("Erro: Nenhuma aula pôde ser criada no intervalo selecionado.");
        }

        // 3. Persistir e associar ao professor
        // IMPORTANTE: use o retorno do saveAll, pois ele contém os IDs gerados pelo banco
        List<Aula> aulasSalvas = aulaRepository.saveAll(adicionados);

        for (Aula aula : aulasSalvas) {
            if (aula.getId() == null) {
                System.out.println("Aviso: ID da aula " + aula.getDataAula() + " veio nulo do banco!");
                continue;
            }
            // Encode do ID da aula
            String aulaHash = idHasher.encode(aula.getId());
            aulaProfessorService.save(new AulaProfessorDto(idProfessor, aulaHash));
        }

        return erros;
    }    @Transactional
    public void EliminarAulasComHorario(Integer idHorario) throws Exception {
        aulaFixaService.delete(idHorario);
        aulaRepository.deleteAllByIdHorario_Id(idHorario);
    }
    @Transactional
    public void EliminarAulasComHorario(String idHorario) throws Exception {

        aulaProfessorService.deleteAllByHorarioId(idHorario);
        aulaRepository.deleteAllByIdHorario_Id(idHasher.decode(idHorario));
        aulaFixaService.delete(idHasher.decode(idHorario));
    }
    @Transactional
    public void EliminarAulasFuturasComHorario(String idHorarioStr) throws Exception {
        Integer idHorario = idHasher.decode(idHorarioStr);
        LocalDate hoje = LocalDate.now();

        // 1. Apaga as ligações Professor-Aula (tabela de junção) das aulas futuras
        aulaProfessoreRepository.deleteFutureByHorarioId(idHorario, hoje);

        // 2. Apaga as Aulas futuras
        aulaRepository.deleteFutureByHorarioId(idHorario, hoje);

        // NOTA: Não apagamos o aulaFixaService (HorarioTurma) aqui,
        // porque ele ainda tem aulas no passado associadas a ele!
    }
    @Transactional(rollbackFor = Exception.class)
    public List<AulaDto> atualizaPorHorario(HorarioTurmaRequestDto dto, String id, String idProfessor,String idAtualizador) throws Exception {

        Integer idHorarioDecodificado = idHasher.decode(id);
        HorarioTurma horarioExistente = horarioFixoRepository.findById(idHorarioDecodificado)
                .orElseThrow(() -> new Exception("Erro: Horário não encontrado"));

        LocalDate hoje = LocalDate.now();

        // CASO A: O DIA DA SEMANA MUDOU
        if (!Objects.equals(dto.diaSemana(), horarioExistente.getDiaSemana())) {

            // Chamamos o teu método que já trata da limpeza segura do futuro
            EliminarAulasFuturasComHorario(id);

            // Preparamos o DTO para o gerador começar de hoje
            HorarioTurmaRequestDto dtoNovoPeriodo = new HorarioTurmaRequestDto(
                    null,
                    idAtualizador,
                    dto.idturma(),
                    hoje,
                    dto.dataValidade(),
                    dto.diaSemana(),
                    dto.duracaoMinutos(),
                    dto.horaInicio(),
                    dto.horaFim(),
                    dto.estudioId()
            );

            // Atualizamos o cabeçalho existente
            aulaFixaService.update(id, aulaFixaService.convertRequestToDto(dto));

            // Geramos as novas aulas para o novo dia da semana
            return GerarAulasComHorario(dtoNovoPeriodo, idProfessor);
        }

        // CASO B: MUDANÇA APENAS DE HORA OU ESTÚDIO
        else {
            Integer idEstudioNovo = idHasher.decode(dto.estudioId());

            aulaRepository.updateAulasFuturas(
                    idHorarioDecodificado,
                    dto.horaInicio(),
                    dto.horaFim(),
                    idEstudioNovo,
                    dto.duracaoMinutos(),
                    hoje
            );

            aulaFixaService.update(id, aulaFixaService.convertRequestToDto(dto));

            return new ArrayList<>();
        }
    }    public Aula criarAula(AulaDto aulaDto, Integer modalidade) throws Exception {
        // 1. Descodificar o ID do estúdio uma única vez para usar nas validações
        Integer idEstudioDecodificado = idHasher.decode(aulaDto.estudio().id());

        estudioModalidadeRepository
                .findByEstudio_IdAndModalidade_Id(
                        idEstudioDecodificado,
                        modalidade)
                .orElseThrow(() -> new Exception("Este estúdio não permite esta modalidade!"));

        // 2. Verificar conflito usando os dados de tempo (Data, Início, Fim)
        // Passamos os parâmetros individuais em vez do ID da aula
        boolean conflito = estudioService.conflitoestudio(
                idEstudioDecodificado,
                aulaDto.dataAula(),
                aulaDto.horaInicio(),
                aulaDto.horaFim()
        );

        if (conflito) {
            throw new Exception("Conflito de horário: O estúdio '" +
                    aulaDto.estudio().nome() + "' já tem uma aula agendada entre " +
                    aulaDto.horaInicio() + " e " + aulaDto.horaFim() +
                    " no dia " + aulaDto.dataAula());
        }

        // 3. Verificar se o estúdio suporta a modalidade

        // 4. Converter DTO → Entidade e salvar
        Aula aula = aulaDTOparaAula(aulaDto);
        return aulaRepository.save(aula);
    }

    public AulaDto horarioParaAulaDto(HorarioTurmaDto horario, LocalDate dia) throws Exception {

        return new AulaDto(
                null,
                estudioService.findEstudioDtobyId(idHasher.decode(horario.estudioId().id())),
                horario.duracaoMinutos(),
                dia,
                horario.horaInicio(),
                horario.horaFim(),
                horario.idcriadoPor().id(),
                horario,
                estadoAuloService.findbyIdDto(3)
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
        if (aulaDto.criadoPo() != null) {
            utilizadoreRepository.findById(idHasher.decode(aulaDto.criadoPo()))
                    .ifPresent(aula::setCriadoPor);
        }
        aula.setEstado(estadoAuloService.findbyId(idHasher.decode(aulaDto.estado().id())));
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

    @Transactional
    @Scheduled(fixedRate = 60*60*1000) // 1 hora em milissegundos
    public void checkAutomaticoExpiradas() throws Exception {
        verificarEAtualizarAulasExpiradas();
    }
    @Transactional
    public void verificarEAtualizarAulasExpiradas() throws Exception {
        LocalDateTime limite = LocalDateTime.now().minusHours(48);

        // Extrai a data e a hora para passar à query
        List<Aula> aulas = aulaRepository.findAulasPassadasHa48Horas(
                limite.toLocalDate(),
                limite.toLocalTime()
        );
        for (Aula aula : aulas) {
            EstadoAula estadoCancelado = estadoAuloService.findbyId(ID_ESTADO_AUTOMATICO);
            processarPagamentosAula(aula);
            aula.setEstado(estadoCancelado);
        }

        // 4. Gravar todas de uma vez
        aulaRepository.saveAll(aulas);
    }
    public AulaDto atualizaEstado(String id, String idEstado) throws Exception {
        Aula aula= this.bucarPorId(id);
        aula.setEstado(estadoAuloService.findbyId(idHasher.decode(idEstado)));
        return  converterParaDto(aulaRepository.save(aula));
    }

    @Transactional(rollbackFor = Exception.class)
    public AulaDto validarRealizacao(String aulaId) throws Exception {
        Aula aula = aulaRepository.findById(idHasher.decode(aulaId))
                .orElseThrow(() -> new Exception("Aula não encontrada"));

        int estadoAtual = aula.getEstado().getId();

        // 1. Validações de Estado
        if (estadoAtual == ID_ESTADO_CANCELADA) throw new Exception("Não é possível validar uma aula cancelada.");
        if (estadoAtual == ID_ESTADO_VALIDADA || estadoAtual == ID_ESTADO_AUTOMATICO || estadoAtual==ID_ESTADO_CONTABILIZADO) throw new Exception("A aula já foi validada.");

        // 2. Chamar a nova função de processamento financeiro
        this.processarPagamentosAula(aula);

        // 3. Finalizar Estado da Aula
        EstadoAula estadoRealizada = estadoAuloService.findbyId(ID_ESTADO_PENDENTEVALIDACAO);
        aula.setEstado(estadoRealizada);

        return converterParaDto(aulaRepository.save(aula));
    }
    @Transactional(rollbackFor = Exception.class)
    public void processarPagamentosAula(Aula aula) throws Exception {
        // 1. Obter intervenientes (Usando IDs reais já disponíveis na entidade 'aula')
        List<AulaProfessore> professores = aulaProfessorService.findAllByAulaId(idHasher.encode(aula.getId()));
        List<AulaAluno> alunosInscritos = aulaAlunoRepository.findAllByAula_Id(aula.getId());

        if (professores.isEmpty() || alunosInscritos.isEmpty()) {
            throw new Exception("A aula deve ter pelo menos um professor e um aluno para processar pagamentos.");
        }

        // 2. Cálculo do Valor Base
        BigDecimal duracaoHoras = BigDecimal.valueOf(aula.getDuracaoMinutos())
                .divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);

        BigDecimal maiorValorHora = professores.stream()
                .map(p -> p.getProfessor().getValorHora())
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal valorTotalAula = maiorValorHora.multiply(duracaoHoras);

        // 3. Obter Tipos de Pagamento (Entities)
        int idTipoAlunoReal = (aula.getIdHorario() != null) ? 1 : 2;
        TipoPagamento tipoAluno = tipoPagamentoRepository.findById(idTipoAlunoReal)
                .orElseThrow(() -> new Exception("Tipo de pagamento aluno não encontrado"));

        TipoPagamento tipoProf = tipoPagamentoRepository.findById(7)
                .orElseThrow(() -> new Exception("Tipo de remuneração professor não encontrado"));

        String label = (idTipoAlunoReal == 1) ? "Aula Fixa" : "Coaching";

        // 4. Gerar Pagamentos dos Alunos
        BigDecimal valorPorAluno = valorTotalAula.divide(BigDecimal.valueOf(alunosInscritos.size()), 2, java.math.RoundingMode.HALF_UP);

        for (AulaAluno vinculo : alunosInscritos) {
            Pagamento p = new Pagamento();
            p.setValorPagamento(valorPorAluno);
            p.setDescricao("Pagamento " + label + ": " + aula.getDataAula());
            p.setPago(false);
            p.setDataPagamento(LocalDate.now());
            p.setAula(aula);
            p.setIdutilizador(vinculo.getAluno()); // Atribuição direta da Entity
            p.setIdTipoPagamento(tipoAluno);

            pagamentoRepository.save(p);
        }

        // 5. Gerar Remuneração dos Professores
        BigDecimal valorPorProf = valorTotalAula.divide(BigDecimal.valueOf(professores.size()), 2, java.math.RoundingMode.HALF_UP);

        for (AulaProfessore vinculo : professores) {
            Pagamento p = new Pagamento();
            p.setValorPagamento(valorPorProf);
            p.setDescricao("Remuneração " + label + ": " + aula.getDataAula());
            p.setPago(false);
            p.setDataPagamento(LocalDate.now());
            p.setAula(aula);
            p.setIdutilizador(vinculo.getProfessor()); // Atribuição direta da Entity
            p.setIdTipoPagamento(tipoProf);

            pagamentoRepository.save(p);
        }
    }


    public List<AulaDto> buscarAulasProfessorSemana(String professorId, int offset) throws Exception {

        Integer profId = idHasher.decode(professorId);

        // validar se professor existe
        professorService.findById(professorId);

        LocalDate inicioSemana = calcularInicioSemana(offset);
        LocalDate fimSemana = inicioSemana.plusDays(6);

        return aulaRepository.findAulasByProfessorAndSemana(profId, inicioSemana, fimSemana)
                .stream()
                .map(this::converterParaDto)
                .toList();
    }

    @Transactional
    public void inscreverAluno(String alunoId, String aulaId) throws Exception {
        Integer aulaIdDecoded = idHasher.decode(aulaId);
        Integer alunoIdDecoded = idHasher.decode(alunoId);

        // Verifica se já está inscrito
        boolean jaInscrito = aulaAlunoRepository
                .existsById(new AulaAlunoId(aulaIdDecoded, alunoIdDecoded));
        if (jaInscrito) {
            throw new Exception("Aluno já está inscrito nesta aula");
        }

        Aula aula = aulaRepository.findById(aulaIdDecoded)
                .orElseThrow(() -> new Exception("Aula não encontrada"));
        Aluno aluno = alunoRepository.findById(alunoIdDecoded)
                .orElseThrow(() -> new Exception("Aluno não encontrado"));

        AulaAluno aulaAluno = new AulaAluno();
        aulaAluno.setId(new AulaAlunoId(aulaIdDecoded, alunoIdDecoded));
        aulaAluno.setAula(aula);
        aulaAluno.setAluno(aluno);
        for(AulaProfessore professore : aulaProfessorService.findAllByAulaId(aulaId)){
            notificacoesService.criarNotificacao(
                    professore.getProfessor().getId(),
                    aluno.getId(),
                    "Inscição em aula de coaching",
                    "Nova inscrição para aula de coaching de"+ aulaAluno.getAula().getDataAula() +" das "+
                        aulaAluno.getAula().getHoraInicio() + " às " + aulaAluno.getAula().getHoraFim()
                    ,
                    "PEDIDO COACHING",
                    idHasher.encode( aulaAluno.getAula().getId())
        );
        }

        aulaAlunoRepository.save(aulaAluno);
    }

    @Transactional
    public void cancelarInscricaoAluno(String alunoId, String aulaId) throws Exception {
        Aula aulafalta= this.buscarPorId(aulaId).orElseThrow(()->new Exception("Aula não existe"));
        AulaAlunoId id = new AulaAlunoId(idHasher.decode(aulaId), idHasher.decode(alunoId));
        if (!aulaAlunoRepository.existsById(id)) {
            throw new Exception("Aluno não está inscrito nesta aula");
        }
        if(aulafalta.getEstado().getId()!=ID_ESTADO_PENDENTE) {


            if (aulaAlunoRepository.countByAulaId(idHasher.decode(aulaId)) == 1) {
                AulaDto aula = atualizaEstado(aulaId, idHasher.encode(ID_ESTADO_CANCELADA));
                LocalDateTime momentoDaAula = LocalDateTime.of(aula.dataAula(), aula.horaInicio());
                if (!LocalDateTime.now().isBefore(momentoDaAula.minusHours(48))) {
                    aplicaSancoes(alunoId, aulafalta, idHasher.encode(1));
                }
            }
        }
        aulaAlunoRepository.deleteById(id);
    }

    public long contarInscritos(String aulaId) {
        return aulaAlunoRepository.countByAulaId(idHasher.decode(aulaId));
    }
    public void aplicaSancoes(String alunoId, Aula aula,String marcadopor) throws Exception {
        FaltaDto faltaDto = new FaltaDto(null,idHasher.encode(aula.getId()),alunoId,false,"Cancelamento antes das 48 horas");
        cancelamentoService.marcarFalta(faltaDto,marcadopor);
        return;
    }

}


