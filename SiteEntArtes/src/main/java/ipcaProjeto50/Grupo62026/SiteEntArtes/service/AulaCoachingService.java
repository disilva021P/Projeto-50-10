package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaAlunoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaCoachingDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaCoachingRequestDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AulaCoachingService {

    private final AulaCoachingRepository aulaCoachingRepository;
    private final IdHasher idHasher;
    private final AulaService aulaService;
    private final EstadoAuloService estadoAuloService;
    private final ModalidadeService modalidadeService;
    private final DisponibilidadeService disponibilidadeService;
    private final EstudioRepository estudioRepository;
    private final EstudioService estudioService;
    private final UtilizadoreRepository utilizadoreRepository;
    private final AulaRepository aulaRepository;
    private final AulaAlunoRepository aulaAlunoRepository;
    private final AlunoRepository alunoRepository;
    private final AulaProfessoreRepository aulaProfessoreRepository;
    private final AulaAlunoService aulaAlunoService;
    private final ProfessoreRepository professoreRepository;
    private final ProfessorService professorService;
    private final ProfessorModalidadeRepository professorModalidadeRepository;
    private final EstudioModalidadeRepository estudioModalidadeRepository;
    private final NotificacoesService notificacoesService;

    // =========================================================================
    // Leitura
    // =========================================================================

    public AulaCoachingDto findById(Integer id) throws Exception {
        return convertToAulaCoachingDto(
                aulaCoachingRepository.findById(id)
                        .orElseThrow(() -> new Exception("Aula de coaching não encontrada"))
        );
    }

    public AulaCoachingDto findById(String id) throws Exception {
        return convertToAulaCoachingDto(
                aulaCoachingRepository.findById(idHasher.decode(id))
                        .orElseThrow(() -> new Exception("Aula de coaching não encontrada"))
        );
    }

    /** Pega em TODOS OS COACHINGS do aluno */
    public List<AulaCoachingDto> findAllbyAlunoId(String alunoId) {
        return aulaCoachingRepository.buscarAulaCoachingPorAluno(idHasher.decode(alunoId))
                .stream()
                .map(aula -> {
                    try {
                        return convertToAulaCoachingDto(aula);
                    } catch (Exception e) {
                        throw new RuntimeException("Mapping failed", e);
                    }
                })
                .toList();
    }

    /** Pega em TODOS os COACHINGS do aluno paginado */
    public Page<AulaCoachingDto> findAllbyAlunoIdPage(String alunoId, Pageable pageable) {
        Integer idDecoded = idHasher.decode(alunoId);
        return aulaCoachingRepository.buscarAulaCoachingPorAluno(idDecoded, pageable)
                .map(aula -> {
                    try {
                        return convertToAulaCoachingDto(aula);
                    } catch (Exception e) {
                        throw new RuntimeException("Mapping failed", e);
                    }
                });
    }

    public Page<AulaCoachingDto> findAllPorAlunoIDModalidadePage(String alunoId,String modalidade,int offset, Pageable pageable) throws Exception {
        Integer idDecoded = idHasher.decode(alunoId);
        if(offset<0) throw new Exception("Erro: Não pode inscrever-se em aulas passadas");
        LocalDate inicioSemana = calcularInicioSemana(offset);
        LocalDate fimSemana = inicioSemana.plusDays(6);
        if(modalidade==null || modalidade.isBlank()) return aulaCoachingRepository.buscaAulasCoachingDisponiveis(inicioSemana,fimSemana,pageable).map(aula -> {
                    try {
                        return convertToAulaCoachingDto(aula);
                    } catch (Exception e) {
                        throw new RuntimeException("Mapping failed", e);
                    }});
        else {
            return aulaCoachingRepository.buscaAulasCoachingDisponiveilPorModalidade(inicioSemana,fimSemana,idHasher.decode(modalidade), pageable).map(aula -> {
                try {
                    return convertToAulaCoachingDto(aula);
                } catch (Exception e) {
                    throw new RuntimeException("Mapping failed", e);
                }});
        }

    }
    /** Coachings do aluno na semana indicada pelo offset (0 = semana atual) */
    public List<AulaCoachingDto> buscarCoachingSemana(String userId, int offset, Pageable pageable) throws Exception {
        encontraUtilizador(userId);
        LocalDate inicioSemana = calcularInicioSemana(offset);
        LocalDate fimSemana = inicioSemana.plusDays(6);
        return aulaCoachingRepository
                .buscarAulaCoachingPorAlunoSemana(idHasher.decode(userId), inicioSemana, fimSemana, pageable)
                .stream()
                .map(aula -> {
                    try {
                        return convertToAulaCoachingDto(aula);
                    } catch (Exception e) {
                        throw new RuntimeException("Mapping failed", e);
                    }
                })
                .toList();
    }

    /** Devolve todos os coachings (para a coordenação). */
    public Page<AulaCoachingDto> findAll(Pageable pageable) {
        return aulaCoachingRepository.findAll(pageable).map(aula -> {
            try {
                return convertToAulaCoachingDto(aula);
            } catch (Exception e) {
                throw new RuntimeException("Mapping failed", e);
            }
        });
    }

    /** Devolve coachings PENDENTES associados a um professor. */
    public Page<AulaCoachingDto> findPendentesByProfessorId(String professorId, Pageable pageable) {
        Integer idDecoded = idHasher.decode(professorId);
        return aulaCoachingRepository
                .buscarAulaCoachingPendentesPorProfessor(idDecoded, pageable)
                .map(aula -> {
                    try {
                        return convertToAulaCoachingDto(aula);
                    } catch (Exception e) {
                        throw new RuntimeException("Mapping failed", e);
                    }
                });
    }

    // =========================================================================
    // Escrita
    // =========================================================================

    /**
     * Cria uma nova aula de coaching (chamado pelo professor / coordenação).
     */
    @Transactional
    public AulaCoachingDto salvarMarcarCoaching(AulaCoachingRequestDto dto, String idAluno) throws Exception {
        if(dto.dataAula().isBefore(LocalDate.now()) || (dto.dataAula().equals(LocalDate.now()) && dto.horaInicio().isBefore(LocalTime.now()))){
            throw new Exception("Data de início inferior à Data atual");
        }
        if(dto.maxAlunos()>8){
            throw new Exception("Nº de alunos max é 8");
        }
        if (!professorModalidadeRepository.existsByModalidadeIdAndProfessorId(idHasher.decode(dto.modalidadeId()), idHasher.decode( dto.professorId()) )) {
            throw new Exception("Professor não leciona esta modalidade");
        }
        if (!estudioModalidadeRepository.existsByEstudio_IdAndModalidade_Id(
                idHasher.decode(dto.estudioId()), idHasher.decode(dto.modalidadeId()))) {
            throw new Exception("Este estúdio não é compatível com esta modalidade");
        }
        if (!disponibilidadeService.verificaMarcacaoValida(
                dto.professorId(), dto.dataAula(), dto.horaInicio(), dto.horaFim())) {
            throw new Exception("Professor não está disponível nesse horário");
        }
        if (aulaRepository.existeConflitoNoEstudio(idHasher.decode( dto.estudioId()),dto.dataAula(),dto.horaInicio(),dto.horaFim())) {
            throw new Exception("Estúdio já possui aula marcada para esse horário");
        }

        AulaCoaching aulaCoaching = aulaCoachingRepository.save(requestDtoParaCoaching(dto));

        Aluno a = alunoRepository.findById(idHasher.decode(idAluno))
                .orElseThrow(() -> new Exception("Aluno não encontrado"));
        aulaAlunoRepository.save(new AulaAluno(
                new AulaAlunoId(aulaCoaching.getId(), idHasher.decode(idAluno)),
                    aulaCoaching,a
                // Removido o ";" aqui
        ));
        Professore p = professoreRepository.findById(idHasher.decode(dto.professorId()))
                .orElseThrow(() -> new Exception("Professor não encontrado"));
        aulaProfessoreRepository.save(new AulaProfessore(
                new AulaProfessoreId(aulaCoaching.getId(), idHasher.decode(dto.professorId())),
                aulaCoaching, p
        ));
        notificacoesService.criarNotificacao(
                p.getId(),
                a.getId(),
                "Novo Pedido de coaching",
                "Novo pedido de coaching para "+ a.getNome() +". Acesse pedidos pendentes para confirmar",
                "PEDIDO COACHING",
                idHasher.encode(aulaCoaching.getId())
        );
        return convertToAulaCoachingDto(aulaCoaching);
    }

    /**
     * Inscreve um aluno numa aula de coaching existente.
     * Verifica se a aula está confirmada/agendada e se ainda tem vagas.
     */
    @Transactional
    public AulaCoachingDto inscrever(String alunoId, String aulaId) throws Exception {
        AulaCoaching coaching = aulaCoachingRepository.findById(idHasher.decode(aulaId))
                .orElseThrow(() -> new Exception("Aula de coaching não encontrada"));

        int estadoAtual = coaching.getEstado().getId();
        if (estadoAtual == AulaService.ID_ESTADO_CANCELADA) {
            throw new Exception("Não é possível inscrever numa aula cancelada");
        }
        if (estadoAtual == AulaService.ID_ESTADO_REALIZADA) {
            throw new Exception("Não é possível inscrever numa aula já realizada");
        }

        long inscritos = aulaService.contarInscritos(aulaId);
        if (inscritos >= coaching.getMaxAlunos()) {
            throw new Exception("Aula de coaching sem vagas disponíveis");
        }

        aulaService.inscreverAluno(alunoId, aulaId);
        return convertToAulaCoachingDto(coaching);
    }

    /**
     * Cancela a inscrição de um aluno numa aula de coaching.
     * Só é possível cancelar se a aula ainda não foi realizada.
     */
    @Transactional
    public void cancelarInscricao(String alunoId, String aulaId) throws Exception {
        Integer idAula =idHasher.decode(aulaId);
        AulaCoaching coaching = aulaCoachingRepository.findById(idAula)
                                .orElseThrow(() -> new Exception("Aula de coaching não encontrada"));
        if (coaching.getEstado().getId() == AulaService.ID_ESTADO_CANCELADA) {
            throw new Exception("Não é possível cancelar a inscrição numa aula já cancelada");
        }
        if (coaching.getEstado().getId() > AulaService.ID_ESTADO_REALIZADA) {
            throw new Exception("Não é possível cancelar a inscrição numa aula já realizada");
        }
        LocalDateTime agora = LocalDateTime.now();

        // 2. Obter o momento da aula (assumindo que a tua entidade Aula tem data e horaInicio)
        // Se a data e hora estiverem em campos separados:
        LocalDateTime momentoDaAula = LocalDateTime.of(coaching.getDataAula(), coaching.getHoraInicio());
        // 3. Verificar se o momento da aula está entre "agora" e "agora + 48h"
        // E também garantir que a aula não é no passado (opcional, mas recomendado)
        if(coaching.getEstado().getId() == AulaService.ID_ESTADO_PENDENTE){
            aulaService.cancelarInscricaoAluno(alunoId, aulaId);
            aulaProfessoreRepository.deleteAllByAula_Id(idAula);
            aulaRepository.deleteById(idAula);
            aulaCoachingRepository.deleteById(idAula);
            return;
        }

        aulaService.cancelarInscricaoAluno(alunoId, aulaId);
        return;
    }

    /**
     * Confirma um coaching — muda o estado de PENDENTE para AGENDADA.
     * Lança exceção se o coaching já não estiver no estado PENDENTE.
     */
    @Transactional
    public AulaCoachingDto confirmar(String aulaId,String professorId) throws Exception {
        AulaCoaching coaching = aulaCoachingRepository.findById(idHasher.decode(aulaId))
                .orElseThrow(() -> new Exception("Aula de coaching não encontrada"));
        Professore professore = professorService.findById(professorId);
        boolean aulaProfessore = aulaProfessoreRepository.existsByAula_IdAndProfessor_Id(coaching.getId(), idHasher.decode(professorId));
        if(!aulaProfessore) {
            throw new Exception("Professor sem acesso à Aula");
        }
        if (coaching.getEstado().getId() != AulaService.ID_ESTADO_PENDENTE) {
            throw new Exception("Só é possível confirmar coachings no estado PENDENTE. Estado atual: "
                    + coaching.getEstado().getEstado());
        }
        for(AulaAlunoDto aulaAluno: aulaAlunoService.findAllByAulaId(aulaId))
        notificacoesService.criarNotificacao(
                idHasher.decode(aulaAluno.idAluno()),
                professore.getId(),
                "Aula de coaching marcada! ",
                "Aula de coaching de"+ coaching.getDataAula() +" das "+
                        coaching.getHoraInicio() + " às " + coaching.getHoraFim() + ".\nFoi confirmada pelo professor " + professore.getNome()
                ,
                "PEDIDO COACHING",
                idHasher.encode( coaching.getId())
        );
        coaching.setEstado(estadoAuloService.findbyId(AulaService.ID_ESTADO_AGENDADA));
        return convertToAulaCoachingDto(aulaCoachingRepository.save(coaching));
    }

    /**
     * Regista a realização de um coaching — muda o estado para REALIZADA.
     * Pode ser chamado pelo professor ou pela coordenação.
     * Lança exceção se a aula já estiver cancelada ou já realizada.
     */
    @Transactional
    public AulaCoachingDto realizar(String aulaId) throws Exception {
        AulaCoaching coaching = aulaCoachingRepository.findById(idHasher.decode(aulaId))
                .orElseThrow(() -> new Exception("Aula de coaching não encontrada"));

        int estadoAtual = coaching.getEstado().getId();
        if (estadoAtual == AulaService.ID_ESTADO_CANCELADA) {
            throw new Exception("Não é possível realizar uma aula cancelada");
        }
        if (estadoAtual == AulaService.ID_ESTADO_REALIZADA) {
            throw new Exception("A aula já se encontra no estado REALIZADA");
        }

        coaching.setEstado(estadoAuloService.findbyId(AulaService.ID_ESTADO_REALIZADA));
        return convertToAulaCoachingDto(aulaCoachingRepository.save(coaching));
    }

    /**
     * Cancela um coaching — muda o estado para CANCELADA.
     * Lança exceção se a aula já estiver realizada ou já cancelada.
     */
    @Transactional
    public AulaCoachingDto cancelar(String aulaId) throws Exception {
        AulaCoaching coaching = aulaCoachingRepository.findById(idHasher.decode(aulaId))
                .orElseThrow(() -> new Exception("Aula de coaching não encontrada"));

        int estadoAtual = coaching.getEstado().getId();
        if (estadoAtual == AulaService.ID_ESTADO_REALIZADA) {
            throw new Exception("Não é possível cancelar uma aula já realizada");
        }
        if (estadoAtual == AulaService.ID_ESTADO_CANCELADA) {
            throw new Exception("A aula já se encontra cancelada");
        }

        coaching.setEstado(estadoAuloService.findbyId(AulaService.ID_ESTADO_CANCELADA));
        return convertToAulaCoachingDto(aulaCoachingRepository.save(coaching));
    }

    /**
     * Valida a presença de um aluno num coaching.
     * Confirma que o aluno está inscrito na aula e regista a sua presença,
     * avançando o estado para AGENDADA se ainda estava PENDENTE.
     */
    @Transactional
    public AulaCoachingDto validarPresenca(String alunoId, String aulaId) throws Exception {
        aulaService.buscarAulaAluno(alunoId, aulaId);

        AulaCoaching coaching = aulaCoachingRepository.findById(idHasher.decode(aulaId))
                .orElseThrow(() -> new Exception("Aula de coaching não encontrada"));

        if (coaching.getEstado().getId() == AulaService.ID_ESTADO_PENDENTE) {
            coaching.setEstado(estadoAuloService.findbyId(AulaService.ID_ESTADO_AGENDADA));
            return convertToAulaCoachingDto(aulaCoachingRepository.save(coaching));
        }

        return convertToAulaCoachingDto(coaching);
    }

    // =========================================================================
    // Conversão
    // =========================================================================

    public AulaCoachingDto convertToAulaCoachingDto(AulaCoaching aulaCoaching) throws Exception {
        AulaDto aulaPrincipal = aulaService.bucarPorIdDto(aulaCoaching.getId());
        return new AulaCoachingDto(
                aulaPrincipal,
                aulaCoaching.getMaxAlunos(),
                estadoAuloService.converterParaDto(aulaCoaching.getEstado()),
                modalidadeService.converterParaDto(aulaCoaching.getModalidade())
        );
    }

    /**
     * Converte AulaCoachingRequestDto → entidade AulaCoaching para persistência.
     */
    private AulaCoaching requestDtoParaCoaching(AulaCoachingRequestDto dto) throws Exception {
        AulaCoaching coaching = new AulaCoaching();

        // Campos de Aula
        coaching.setEstudio(estudioRepository.findById(idHasher.decode(dto.estudioId()))
                .orElseThrow(() -> new Exception("Estúdio não encontrado")));
        coaching.setCriadoPor(utilizadoreRepository.findById(idHasher.decode(dto.professorId()))
                .orElseThrow(() -> new Exception("Professor não encontrado")));
        coaching.setDataAula(dto.dataAula());
        coaching.setHoraInicio(dto.horaInicio());
        coaching.setHoraFim(dto.horaFim());
        coaching.setDuracaoMinutos((int) Duration.between(dto.horaInicio(), dto.horaFim()).toMinutes());
        coaching.setEstado(estadoAuloService.findbyId(AulaService.ID_ESTADO_PENDENTE));

        // Campos de AulaCoaching
        coaching.setMaxAlunos(dto.maxAlunos() != null ? dto.maxAlunos() : 8);
        coaching.setModalidade(modalidadeService.findById(idHasher.decode(dto.modalidadeId())));

        return coaching;
    }

    // =========================================================================
    // Auxiliares
    // =========================================================================

    private Utilizadore encontraUtilizador(String userId) throws Exception {
        return utilizadoreRepository.findById(idHasher.decode(userId))
                .orElseThrow(() -> new Exception("Utilizador não encontrado com id: " + userId));
    }

    private LocalDate calcularInicioSemana(int offset) {
        return LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                .plusWeeks(offset);
    }

    @Transactional
    public void eliminar(String id) throws Exception {
        Integer idReal = idHasher.decode( id);
       AulaCoaching coaching= aulaCoachingRepository.findById(idReal)
                .orElseThrow(() -> new Exception("Aula de coaching não encontrada"));
        if(coaching.getEstado().getId() == AulaService.ID_ESTADO_PENDENTE){
            aulaAlunoRepository.deleteAllByAula_Id(idReal);
            aulaProfessoreRepository.deleteAllByAula_Id(idReal);
            aulaRepository.deleteById(idReal);
            aulaCoachingRepository.deleteById(idReal);
            return;
        }
    }

    @Transactional
    public void professorRejeitaCoaching(String idAula,String idProfessor) throws Exception {
        AulaCoaching aula = aulaCoachingRepository.findById(idHasher.decode(idAula)).orElseThrow(()-> new Exception( "Aula não encontrada"));
        AulaProfessore aulaProfessore = aulaProfessoreRepository.findByAula_IdAndProfessor_Id(idHasher.decode(idAula), idHasher.decode(idProfessor) ).orElseThrow(()-> new Exception( "Professor não possui esta aula"));
        for(AulaAlunoDto aulaAluno: aulaAlunoService.findAllByAulaId(idAula))
            notificacoesService.criarNotificacao(
                    idHasher.decode(aulaAluno.idAluno()),
                    aulaProfessore.getProfessor().getId(),
                    "Aula de coaching rejeitada! ",
                    "Aula de coaching de "+ aula.getDataAula() +" das "+
                            aula.getHoraInicio() + " às " + aula.getHoraFim() + ".\nFoi rejeitada pelo professor " + aulaProfessore.getProfessor().getNome()
                    ,
                    "PEDIDO COACHING",
                    idHasher.encode( aulaProfessore.getProfessor().getId())
            );
        eliminar(idAula);
    }
}