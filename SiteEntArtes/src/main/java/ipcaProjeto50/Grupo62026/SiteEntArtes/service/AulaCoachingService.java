package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import jakarta.persistence.criteria.CriteriaBuilder;
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
    private  final UtilizadorService utilizadorService;
    private final EncarregadoAlunoRepository encarregadoAlunoRepository;

    @jakarta.persistence.PersistenceContext
    private final jakarta.persistence.EntityManager entityManager;

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
        LocalDate inicioSemana = LocalDate.now();
        LocalDate fimSemana = LocalDate.now().plusYears(1);
        if(modalidade==null || modalidade.isBlank()) return aulaCoachingRepository.buscaAulasCoachingDisponiveis(inicioSemana,fimSemana,idDecoded,AulaService.ID_ESTADO_AGENDADA,pageable).map(aula -> {
            try {
                return convertToAulaCoachingDto(aula);
            } catch (Exception e) {
                throw new RuntimeException("Mapping failed", e);
            }});
        else {
            return aulaCoachingRepository.buscaAulasCoachingDisponiveilPorModalidade(inicioSemana,fimSemana,idHasher.decode(modalidade),idDecoded,AulaService.ID_ESTADO_AGENDADA, pageable).map(aula -> {
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

        // 1. Validações básicas temporais e de negócio
        if(dto.dataAula().isBefore(LocalDate.now()) || (dto.dataAula().equals(LocalDate.now()) && dto.horaInicio().isBefore(LocalTime.now()))){
            throw new Exception("Data de início inferior à Data atual");
        }
        if(dto.maxAlunos() > 8){
            throw new Exception("Nº de alunos max é 8");
        }

        // Assumindo que o DTO já traz IDs numéricos (Integer)
        if (!professorModalidadeRepository.existsByModalidadeIdAndProfessorId(idHasher.decode( dto.modalidadeId()), idHasher.decode( dto.professorId()))) {
            throw new Exception("Professor não leciona esta modalidade");
        }
        if (!disponibilidadeService.verificaMarcacaoValida(
                dto.professorId(), dto.dataAula(), dto.horaInicio(), dto.horaFim())) {
            throw new Exception("Professor não está disponível nesse horário");
        }

        // 2. SELEÇÃO DO ESTÚDIO IDEAL
        List<Integer> estudiosCandidatos = aulaRepository.findEstudiosPorModalidadeOrdenadosPorAulasDoDia(
                idHasher.decode( dto.modalidadeId()),
                dto.dataAula()
        );

        if (estudiosCandidatos.isEmpty()) {
            throw new Exception("Não existem estúdios cadastrados para esta modalidade");
        }

        Integer idEstudioEscolhido = null;

        // Procura o primeiro estúdio da lista que não tenha conflito de horário
        for (Integer idEstudio : estudiosCandidatos) {
            if (!aulaRepository.existeConflitoNoEstudio(idEstudio, dto.dataAula(), dto.horaInicio(), dto.horaFim())) {
                idEstudioEscolhido = idEstudio;
                break;
            }
        }

        if (idEstudioEscolhido == null) {
            throw new Exception("Todos os estúdios compatíveis já possuem aula marcada para esse horário");
        }

        // 3. Salvar a Aula passando o Integer do estúdio escolhido
        AulaCoachingRequestDto newDto = new AulaCoachingRequestDto(
                dto.professorId(),
                idHasher.encode(idEstudioEscolhido), // Injeta o estúdio selecionado automaticamente
                dto.dataAula(),
                dto.horaInicio(),
                dto.horaFim(),
                dto.maxAlunos(),
                dto.modalidadeId(),
                dto.descricao()
        );
        AulaCoaching aulaCoaching = requestDtoParaCoaching(newDto);
        aulaCoaching = aulaCoachingRepository.save(aulaCoaching);
        Integer idAlunoI = idHasher.decode( idAluno);
        // 4. Vinculações (Aluno e Professor)
        Aluno a = alunoRepository.findById(idAlunoI)
                .orElseThrow(() -> new Exception("Aluno não encontrado"));

        aulaAlunoRepository.save(new AulaAluno(
                new AulaAlunoId(aulaCoaching.getId(), idAlunoI),
                aulaCoaching, a
        ));

        Professore p = professoreRepository.findById(idHasher.decode( dto.professorId()))
                .orElseThrow(() -> new Exception("Professor não encontrado"));

        aulaProfessoreRepository.save(new AulaProfessore(
                new AulaProfessoreId(aulaCoaching.getId(), idHasher.decode( dto.professorId())),
                aulaCoaching, p
        ));

        // Notificação
        notificacoesService.criarNotificacao(
                p.getId(),
                a.getId(),
                "Novo Pedido de coaching",
                "Novo pedido de coaching para " + a.getNome() + ". Acesse pedidos pendentes para confirmar",
                "PEDIDO COACHING",
                String.valueOf(aulaCoaching.getId()) // Se a notificação pedir String, convertes aqui
        );

        // 5. Retorna o DTO convertido que já vai levar o estúdio lá dentro!
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

        // 1. Efetua a inscrição do aluno no sistema
        aulaService.inscreverAluno(alunoId, aulaId);

        // ==========================================================================================
        // AUTOMAÇÃO FINANCEIRA: Gerar Lançamento de "Aula Avulso" para o Aluno que se inscreveu
        // ==========================================================================================
        try {
            // 1. Obter o valor_hora_default das configurações usando o entityManager
            String valorHoraConfig = "36.00"; // Fallback de segurança
            try {
                valorHoraConfig = entityManager.createQuery(
                                "SELECT c.valor FROM Configuracoe c WHERE LOWER(c.nomeConfig) = :nomeConfig", String.class)
                        .setParameter("nomeConfig", "valor_hora_default")
                        .getSingleResult();
            } catch (jakarta.persistence.NoResultException e) {
                // Caso não exista na BD, mantém os 36.00
            }
            java.math.BigDecimal valorHora = new java.math.BigDecimal(valorHoraConfig.trim());

            // 2. Calcular o valor total proporcional baseado na duração da sessão (em minutos)
            java.math.BigDecimal duracaoMinutos = java.math.BigDecimal.valueOf(coaching.getDuracaoMinutos());
            java.math.BigDecimal custoTotalSessao = valorHora
                    .divide(new java.math.BigDecimal("60"), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(duracaoMinutos)
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            // 3. Procurar a Categoria "Aula Avulso" na tabela tipo_pagamento
            TipoPagamento tipoAulaAvulso = entityManager.createQuery(
                            "SELECT tp FROM TipoPagamento tp WHERE LOWER(tp.tipoPagamento) = :tipo", TipoPagamento.class)
                    .setParameter("tipo", "aula avulso")
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new Exception("Tipo de pagamento 'Aula Avulso' não foi encontrado na base de dados."));

            // 4. Carregar a entidade Utilizadore do Aluno que se está a inscrever
            Integer idAlunoReal = idHasher.decode(alunoId);
            Utilizadore alunoQueSeInscreveu = entityManager.find(Utilizadore.class, idAlunoReal);

            // 5. Instanciar o objeto Pagamento associado ao Aluno correto
            Pagamento pagamentoCoaching = new Pagamento();
            pagamentoCoaching.setValorPagamento(custoTotalSessao);
            pagamentoCoaching.setPago(false); // Fica pendente para o aluno pagar
            pagamentoCoaching.setDescricao(String.format("Inscrição em Sessão de Coaching (%s) - Duração: %d min",
                    coaching.getModalidade().getNome(),
                    coaching.getDuracaoMinutos()));

            pagamentoCoaching.setIdutilizador(alunoQueSeInscreveu); // Aluno correto mapeado dinamicamente
            pagamentoCoaching.setIdTipoPagamento(tipoAulaAvulso);
            pagamentoCoaching.setDataPagamento(java.time.LocalDate.now()); // Data de emissão/vencimento
            pagamentoCoaching.setDataConfirmado(null);
            pagamentoCoaching.setAula(coaching); // Vincula este pagamento à respetiva aula

            // 6. Persistir na base de dados e sincronizar imediatamente
            entityManager.persist(pagamentoCoaching);
            entityManager.flush();

        } catch (Exception e) {
            System.err.println("Erro crítico ao faturar inscrição em Coaching existente: " + e.getMessage());
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível concluir a inscrição devido a um erro no lançamento financeiro: " + e.getMessage());
        }
        // ==========================================================================================

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
            aulaCoachingRepository.deleteById(idAula);
            return;
        }

        aulaService.cancelarInscricaoAluno(alunoId, aulaId);
    }

    /**
     * Confirma um coaching — muda o estado de PENDENTE para AGENDADA.
     * Lança exceção se o coaching já não estiver no estado PENDENTE.
     */
    @Transactional
    public AulaCoachingDto confirmar(String aulaId, String professorId) throws Exception {
        AulaCoaching coaching = aulaCoachingRepository.findById(idHasher.decode(aulaId))
                .orElseThrow(() -> new Exception("Aula de coaching não encontrada"));
        Professore professore = professorService.findById(professorId);
        boolean aulaProfessore = aulaProfessoreRepository.existsByAula_IdAndProfessor_Id(coaching.getId(), idHasher.decode(professorId));
        if (!aulaProfessore) {
            throw new Exception("Professor sem acesso à Aula");
        }
        if (coaching.getEstado().getId() != AulaService.ID_ESTADO_PENDENTE) {
            throw new Exception("Só é possível confirmar coachings no estado PENDENTE. Estado atual: "
                    + coaching.getEstado().getEstado());
        }
        for (AulaAlunoDto aulaAlunodto : aulaAlunoService.findAllByAulaId(aulaId)) {
            if (utilizadorService.possuiEducando(aulaAlunodto.idAluno())) {
                for (EncarregadoAluno ea : encarregadoAlunoRepository.findAllByAluno_Id(idHasher.decode(aulaAlunodto.idAluno()))) {
                    notificacoesService.criarNotificacao(
                            ea.getEncarregado().getId(),
                            professore.getId(),
                            "Aula de coaching marcada! ",
                            "Aula de coaching de" + coaching.getDataAula() + " das " +
                                    coaching.getHoraInicio() + " às " + coaching.getHoraFim() + ".\nFoi confirmada pelo professor " + professore.getNome()
                            ,
                            "PEDIDO COACHING",
                            idHasher.encode(coaching.getId())
                    );
                }
            }
            notificacoesService.criarNotificacao(
                    idHasher.decode(aulaAlunodto.idAluno()),
                    professore.getId(),
                    "Aula de coaching marcada! ",
                    "Aula de coaching de" + coaching.getDataAula() + " das " +
                            coaching.getHoraInicio() + " às " + coaching.getHoraFim() + ".\nFoi confirmada pelo professor " + professore.getNome()
                    ,
                    "PEDIDO COACHING",
                    idHasher.encode(coaching.getId())
            );
        }
        coaching.setEstado(estadoAuloService.findbyId(AulaService.ID_ESTADO_AGENDADA));

        // ==========================================================================================
        // AUTOMAÇÃO FINANCEIRA: Gerar Lançamento de "Aula Avulso" para o Aluno (Solicitante)
        // ==========================================================================================
        try {
            // 1. Obter o valor_hora_default das configurações usando o entityManager do Service
            String valorHoraConfig = "36.00"; // Fallback de segurança
            try {
                valorHoraConfig = entityManager.createQuery(
                                "SELECT c.valor FROM Configuracoe c WHERE LOWER(c.nomeConfig) = :nomeConfig", String.class)
                        .setParameter("nomeConfig", "valor_hora_default")
                        .getSingleResult();
            } catch (jakarta.persistence.NoResultException e) {
                // Caso não exista na BD, mantém os 36.00
            }
            java.math.BigDecimal valorHora = new java.math.BigDecimal(valorHoraConfig.trim());

            // 2. Calcular o valor total proporcional baseado na duração da sessão (em minutos)
            java.math.BigDecimal duracaoMinutos = java.math.BigDecimal.valueOf(coaching.getDuracaoMinutos());
            java.math.BigDecimal custoTotalSessao = valorHora
                    .divide(new java.math.BigDecimal("60"), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(duracaoMinutos)
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            // 3. Procurar a Categoria "Aula Avulso" na tabela tipo_pagamento
            TipoPagamento tipoAulaAvulso = entityManager.createQuery(
                            "SELECT tp FROM TipoPagamento tp WHERE LOWER(tp.tipoPagamento) = :tipo", TipoPagamento.class)
                    .setParameter("tipo", "aula avulso")
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new Exception("Tipo de pagamento 'Aula Avulso' não foi encontrado na base de dados."));

            // 4. CORREÇÃO: Descobrir quem é o Aluno associado a esta sessão de Coaching
            // Vamos buscar a lista de alunos associados a esta aula (geralmente coaching é 1 para 1)
            List<AulaAlunoDto> alunosNaAula = aulaAlunoService.findAllByAulaId(aulaId);
            if (alunosNaAula.isEmpty()) {
                throw new Exception("Não é possível gerar pagamento: Nenhum aluno está associado a esta sessão de coaching.");
            }

            // Obtemos o DTO do primeiro aluno da lista, descodificamos o ID dele e carregamos a Entidade Utilizadore
            String idAlunoHashed = alunosNaAula.get(0).idAluno();
            Integer idAlunoReal = idHasher.decode(idAlunoHashed);
            Utilizadore alunoSolicitante = entityManager.find(Utilizadore.class, idAlunoReal);

            // 5. Instanciar o objeto Pagamento associado ao Aluno correto
            Pagamento pagamentoCoaching = new Pagamento();
            pagamentoCoaching.setValorPagamento(custoTotalSessao);
            pagamentoCoaching.setPago(false); // Fica pendente para o aluno pagar
            pagamentoCoaching.setDescricao(String.format("Sessão de Coaching Privada (%s) - Duração: %d min com Prof. %s",
                    coaching.getModalidade().getNome(),
                    coaching.getDuracaoMinutos(),
                    professore.getNome()));

            // VINCULAÇÃO CORRIGIDA: Agora aponta garantidamente para o Aluno e não para o Professor!
            pagamentoCoaching.setIdutilizador(alunoSolicitante);
            pagamentoCoaching.setIdTipoPagamento(tipoAulaAvulso);
            pagamentoCoaching.setDataPagamento(java.time.LocalDate.now()); // Data de emissão/vencimento
            pagamentoCoaching.setDataConfirmado(null);
            pagamentoCoaching.setAula(coaching); // Vincula este pagamento à respetiva aula

            // 6. Persistir na base de dados e sincronizar imediatamente
            entityManager.persist(pagamentoCoaching);
            entityManager.flush();

        } catch (Exception e) {
            System.err.println("Erro crítico ao gerar faturamento automático de Coaching: " + e.getMessage());
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível confirmar o coaching devido a um erro no lançamento financeiro: " + e.getMessage());
        }
        // ==========================================================================================

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

        AulaAluno solicitante = aulaAlunoRepository
                .findFirstByAula_Id(aulaCoaching.getId())
                .orElse(null);

        UtilizadoreResumoDto solicitadoPorDto = null;
        if (solicitante != null) {
            solicitadoPorDto = new UtilizadoreResumoDto(
                    idHasher.encode(solicitante.getAluno().getId()),
                    solicitante.getAluno().getNome()
            );
        }

        return new AulaCoachingDto(
                aulaPrincipal,
                aulaCoaching.getMaxAlunos(),
                estadoAuloService.converterParaDto(aulaCoaching.getEstado()),
                modalidadeService.converterParaDto(aulaCoaching.getModalidade()),
                solicitadoPorDto
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
        coaching.setNotas(dto.descricao());
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
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
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