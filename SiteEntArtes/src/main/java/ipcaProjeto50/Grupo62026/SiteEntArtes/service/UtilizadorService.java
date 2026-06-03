package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.exception.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jdk.jshell.execution.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilizadorService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String LETRAS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final SecureRandom GeneradorRandomico = new SecureRandom();
    private final UtilizadoreRepository utilizadoreRepository;
    private final EncarregadoAlunoRepository encarregadoAluno;
    private final TipoUtilizadorRepository tipoUtilizadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdHasher idHasher;
    private final TokenRecuperacaoRepository tokenRecuperacaoRepository;
    private final EmailService emailService;
    private final AlunoRepository alunoRepository;
    private final ProfessoreRepository professoreRepository;
    private final TurmaAlunoRepository turmaAlunoRepository;
    private final TurmaRepository turmaRepository;
    private final ProfessorModalidadeRepository professorModalidadeRepository;
    private final ModalidadeRepository modalidadeRepository;
    private final EncarregadoAlunoRepository encarregadoAlunoRepository;

    // Pagamentos Mensalidade, Inscrição, Seguro
    private final TipoPagamentoRepository tipoPagamentoRepository;

    private final TurmaEncarregadoRepository turmaEncarregadoRepository;



    // ─── Listar todos, com filtro opcional por tipo ───────────────────────────
    public Page<UtilizadorResponseDto> listarTodos(String tipoFiltro, Pageable pageable) {
        Page<Utilizadore> lista;
        if (tipoFiltro != null && !tipoFiltro.isBlank()) {
            lista = utilizadoreRepository.findAllByTipo_TipoUtilizador(tipoFiltro,pageable);
        } else {
            lista = utilizadoreRepository.findAll(pageable);
        }
        return lista.map(this::toResponseDTO);
    }

    // ─── Ver detalhe de um utilizador ────────────────────────────────────────
    public UtilizadorResponseDto verDetalhe(String id) {
        Utilizadore utilizador = utilizadoreRepository.findById(idHasher.decode(id))
                .orElseThrow(() -> new UtilizadorNaoEncontradoException(id));
        return toResponseDTO(utilizador);
    }

    // ─── Criar utilizador (só coordenação) ───────────────────────────────────
    @Transactional
    public UtilizadorResponseDto criarUtilizador(CriarUtilizadorDto dto) throws Exception {

        // Normalizar tipo e decifrar a Hash que veio do Frontend
        String tipoid = dto.id_tipoUtilizador();
        Integer idTipoDecoded = idHasher.decode(tipoid);

        // Buscar tipo na base de dados (ex: ROLE_ALUNO)
        TipoUtilizador tipo = tipoUtilizadorRepository
                .findById(idTipoDecoded)
                .orElseThrow(() -> new Exception("Tipo de utilizador não encontrado"));

        // Criar entidade Utilizador consoante o Tipo
        Utilizadore utilizador;

        if (idTipoDecoded != null && idTipoDecoded == 3) { // Aluno
            utilizador = new Aluno();
            ((Aluno) utilizador).setNotas("");

        } else if (idTipoDecoded != null && idTipoDecoded == 2) { // Professor
            Professore prof = new Professore();

            // Preenche os dados específicos do professor vindos do teu DTO atualizado
            prof.setValorHora(dto.valorHora());
            prof.setProfessorExterno(dto.professorExterno());
            prof.setNotas("");

            utilizador = prof;

        } else {
            utilizador = new Utilizadore();
        }

        // Preencher dados comuns
        utilizador.setNome(dto.nome());
        utilizador.setEmail(dto.email());
        utilizador.setNif(dto.nif());
        utilizador.setTelefone(dto.telefone());
        utilizador.setTipo(tipo);

        // Regra de validação de idade
        if (tipo.getId() == 3 && utilizador.isMenorIdade()) {
            utilizador.setAtivo(false);
        } else {
            utilizador.setAtivo(true);
        }

        utilizador.setDataNascimento(dto.dataNascimento());
        utilizador.setCriadoEm(LocalDateTime.now());
        utilizador.setEditadoEm(LocalDateTime.now());

        // Gerador da Palavra-Passe Temporária (12 caracteres)
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            int index = GeneradorRandomico.nextInt(LETRAS.length());
            sb.append(LETRAS.charAt(index));
        }
        utilizador.setPalavraPasse(passwordEncoder.encode(sb));

        // Salvar na BD mantendo a instância sincronizada no Hibernate
        Utilizadore utilizadorSalvo;
        if (utilizador instanceof Aluno) {
            utilizadorSalvo = alunoRepository.save((Aluno) utilizador);

            // ==========================================================================================
            // AUTOMAÇÃO DINÂMICA: Gerar Lançamentos de Inscrição e Seguro ao Criar Aluno
            // ==========================================================================================
            try {
                // -------------------------------------------------------------------------
                // 1. OBTENÇÃO DOS VALORES DAS CONFIGURAÇÕES
                // -------------------------------------------------------------------------

                // A) Buscar Taxa de Inscrição (Tenta 'inscricao_base', senão usa 'mensalidade_base', senão fallback 35.00)
                String valorInscricaoConfig = "35.00";
                try {
                    valorInscricaoConfig = entityManager.createQuery(
                                    "SELECT c.valor FROM Configuracoe c WHERE LOWER(c.nomeConfig) = :nomeConfig", String.class)
                            .setParameter("nomeConfig", "inscricao_base")
                            .getSingleResult();
                } catch (jakarta.persistence.NoResultException e) {
                    try {
                        valorInscricaoConfig = entityManager.createQuery(
                                        "SELECT c.valor FROM Configuracoe c WHERE LOWER(c.nomeConfig) = :nomeConfig", String.class)
                                .setParameter("nomeConfig", "mensalidade_base")
                                .getSingleResult();
                    } catch (jakarta.persistence.NoResultException ex) {
                        // Mantém o valor default de 35.00
                    }
                }
                BigDecimal valorFinalInscricao = new BigDecimal(valorInscricaoConfig.trim());

                // B) Buscar Taxa de Seguro (Tenta 'seguro_base', senão assume fallback de 15.00)
                String valorSeguroConfig = "15.00";
                try {
                    valorSeguroConfig = entityManager.createQuery(
                                    "SELECT c.valor FROM Configuracoe c WHERE LOWER(c.nomeConfig) = :nomeConfig", String.class)
                            .setParameter("nomeConfig", "seguro_base")
                            .getSingleResult();
                } catch (jakarta.persistence.NoResultException e) {
                    // Se não encontrar 'seguro_base' na BD, assume os 15.00€ automaticamente
                }
                BigDecimal valorFinalSeguro = new BigDecimal(valorSeguroConfig.trim());


                // -------------------------------------------------------------------------
                // 2. BUSCA DOS TIPOS DE PAGAMENTO (CATEGORIAS)
                // -------------------------------------------------------------------------

                // A) Categoria "Inscrição"
                TipoPagamento tipoInscricao = entityManager.createQuery(
                                "SELECT tp FROM TipoPagamento tp WHERE LOWER(tp.tipoPagamento) = :tipo", TipoPagamento.class)
                        .setParameter("tipo", "inscrição")
                        .getResultStream()
                        .findFirst()
                        .orElseThrow(() -> new Exception("Tipo de pagamento 'Inscrição' não foi encontrado na tabela tipo_pagamento."));

                // B) Categoria "Seguro"
                TipoPagamento tipoSeguro = entityManager.createQuery(
                                "SELECT tp FROM TipoPagamento tp WHERE LOWER(tp.tipoPagamento) = :tipo", TipoPagamento.class)
                        .setParameter("tipo", "seguro")
                        .getResultStream()
                        .findFirst()
                        .orElseThrow(() -> new Exception("Tipo de pagamento 'Seguro' não foi encontrado na tabela tipo_pagamento."));


                // -------------------------------------------------------------------------
                // 3. CRIAÇÃO E PERSISTÊNCIA DOS LANÇAMENTOS FINANCEIROS
                // -------------------------------------------------------------------------
                LocalDate dataAtual = LocalDate.now();

                // Lançamento A: Taxa de Inscrição
                Pagamento taxaInscricao = new Pagamento();
                taxaInscricao.setValorPagamento(valorFinalInscricao);
                taxaInscricao.setPago(false);
                taxaInscricao.setDescricao("Taxa de Inscrição Inicial de Aluno: " + utilizadorSalvo.getNome());
                taxaInscricao.setIdutilizador(utilizadorSalvo);
                taxaInscricao.setIdTipoPagamento(tipoInscricao);
                taxaInscricao.setDataPagamento(dataAtual);
                taxaInscricao.setDataConfirmado(null);
                taxaInscricao.setAula(null);
                entityManager.persist(taxaInscricao);

                // Lançamento B: Taxa de Seguro Escolar Anual
                Pagamento taxaSeguro = new Pagamento();
                taxaSeguro.setValorPagamento(valorFinalSeguro);
                taxaSeguro.setPago(false);
                taxaSeguro.setDescricao("Seguro Obrigatório Anual de Aluno: " + utilizadorSalvo.getNome());
                taxaSeguro.setIdutilizador(utilizadorSalvo);
                taxaSeguro.setIdTipoPagamento(tipoSeguro);
                taxaSeguro.setDataPagamento(dataAtual);
                taxaSeguro.setDataConfirmado(null);
                taxaSeguro.setAula(null);
                entityManager.persist(taxaSeguro);


            } catch (Exception e) {
                System.err.println("Erro crítico ao gerar obrigações financeiras de entrada: " + e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Incapaz de concluir a inscrição do aluno. Falha na configuração financeira: " + e.getMessage());
            }

            if (dto.idTurmasIniciais() != null && !dto.idTurmasIniciais().isEmpty()) {
                // Iterar por todas as hashes de turmas enviadas pelo Frontend
                for (String turmaHash : dto.idTurmasIniciais()) {
                    if (turmaHash != null && !turmaHash.trim().isEmpty()) {
                        try {
                            Integer idTurmaDecoded = idHasher.decode(turmaHash);
                            Turma turma = turmaRepository.findById(idTurmaDecoded)
                                    .orElseThrow(() -> new Exception("Turma não encontrada"));

                            //Criar a chave composta (ID Aluno + ID Turma)
                            TurmaAlunoId identificadorIntermedio = new TurmaAlunoId();
                            identificadorIntermedio.setAlunoId(utilizadorSalvo.getId());
                            identificadorIntermedio.setTurmaId(turma.getId());

                            //Criar a entidade intermédia e associar os objetos
                            TurmaAluno inscricao = new TurmaAluno();
                            inscricao.setId(identificadorIntermedio);
                            inscricao.setAluno((Aluno) utilizadorSalvo);
                            inscricao.setTurma(turma);

                            inscricao.setInscritoEm(java.time.LocalDate.now());

                            // Guardar na tabela turma_alunos
                            turmaAlunoRepository.save(inscricao);
                        } catch (Exception e) {
                            System.err.println("Erro ao inscrever aluno na turma hash [" + turmaHash + "]: " + e.getMessage());
                        }
                    }
                }
            }

        } else if (utilizador instanceof Professore) {
            //Gravar o professor na BD para gerar o ID original dele
            utilizadorSalvo = professoreRepository.save((Professore) utilizador);
            Professore profSalvo = (Professore) utilizadorSalvo;

            // ─── BLOCO DE GRAVAÇÃO DAS MODALIDADES DO PROFESSOR ──────────────────────
            if (dto.modalidadesIds() != null && !dto.modalidadesIds().isEmpty()) {
                for (String modalidadeHash : dto.modalidadesIds()) {
                    if (modalidadeHash != null && !modalidadeHash.trim().isEmpty()) {
                        try {
                            Integer idModalidadeDecoded = idHasher.decode(modalidadeHash);
                            Modalidade modalidade = modalidadeRepository.findById(idModalidadeDecoded)
                                    .orElseThrow(() -> new Exception("Modalidade não encontrada com o ID: " + idModalidadeDecoded));

                            //Criar a chave composta
                            ProfessorModalidadeId identificadorIntermedio = new ProfessorModalidadeId();
                            identificadorIntermedio.setProfessorId(profSalvo.getId());
                            identificadorIntermedio.setModalidadeId(modalidade.getId());

                            //Criar a entidade intermédia e associar os objetos
                            ProfessorModalidade vinculo = new ProfessorModalidade();
                            vinculo.setId(identificadorIntermedio);
                            vinculo.setProfessor(profSalvo);
                            vinculo.setModalidade(modalidade);

                            //Guardar definitivamente na tabela professor_modalidade
                            professorModalidadeRepository.save(vinculo);

                        } catch (Exception e) {
                            System.err.println("ERRO REAL ao associar professor à modalidade hash [" + modalidadeHash + "]:");
                            e.printStackTrace();
                        }
                    }
                }
                // Força o Spring a descarregar tudo para a BD ANTES de converter para o ResponseDto
                professorModalidadeRepository.flush();
            }

        } else {
            // Guarda o encarregado para gerar o ID
            utilizadorSalvo = utilizadoreRepository.save(utilizador);

            // ─── PAGAMENTOS: só se escolheu turmas ───────────────────────────────
            if (dto.idTurmasIniciais() != null && !dto.idTurmasIniciais().isEmpty()) {
                try {
                    // Valor de inscrição
                    String valorInscricaoConfig = "35.00";
                    try {
                        valorInscricaoConfig = entityManager.createQuery(
                                        "SELECT c.valor FROM Configuracoe c WHERE LOWER(c.nomeConfig) = :n", String.class)
                                .setParameter("n", "inscricao_base")
                                .getSingleResult();
                    } catch (jakarta.persistence.NoResultException e) {
                        try {
                            valorInscricaoConfig = entityManager.createQuery(
                                            "SELECT c.valor FROM Configuracoe c WHERE LOWER(c.nomeConfig) = :n", String.class)
                                    .setParameter("n", "mensalidade_base")
                                    .getSingleResult();
                        } catch (jakarta.persistence.NoResultException ex) { /* usa 35.00 */ }
                    }

                    // Valor de seguro
                    String valorSeguroConfig = "15.00";
                    try {
                        valorSeguroConfig = entityManager.createQuery(
                                        "SELECT c.valor FROM Configuracoe c WHERE LOWER(c.nomeConfig) = :n", String.class)
                                .setParameter("n", "seguro_base")
                                .getSingleResult();
                    } catch (jakarta.persistence.NoResultException e) { /* usa 15.00 */ }

                    TipoPagamento tipoInscricao = entityManager.createQuery(
                                    "SELECT tp FROM TipoPagamento tp WHERE LOWER(tp.tipoPagamento) = :tipo", TipoPagamento.class)
                            .setParameter("tipo", "inscrição")
                            .getResultStream().findFirst()
                            .orElseThrow(() -> new Exception("Tipo de pagamento 'Inscrição' não encontrado."));

                    TipoPagamento tipoSeguro = entityManager.createQuery(
                                    "SELECT tp FROM TipoPagamento tp WHERE LOWER(tp.tipoPagamento) = :tipo", TipoPagamento.class)
                            .setParameter("tipo", "seguro")
                            .getResultStream().findFirst()
                            .orElseThrow(() -> new Exception("Tipo de pagamento 'Seguro' não encontrado."));

                    LocalDate dataAtual = LocalDate.now();

                    Pagamento taxaInscricao = new Pagamento();
                    taxaInscricao.setValorPagamento(new BigDecimal(valorInscricaoConfig.trim()));
                    taxaInscricao.setPago(false);
                    taxaInscricao.setDescricao("Taxa de Inscrição Inicial de Encarregado: " + utilizadorSalvo.getNome());
                    taxaInscricao.setIdutilizador(utilizadorSalvo);
                    taxaInscricao.setIdTipoPagamento(tipoInscricao);
                    taxaInscricao.setDataPagamento(dataAtual);
                    taxaInscricao.setDataConfirmado(null);
                    taxaInscricao.setAula(null);
                    entityManager.persist(taxaInscricao);

                    Pagamento taxaSeguro = new Pagamento();
                    taxaSeguro.setValorPagamento(new BigDecimal(valorSeguroConfig.trim()));
                    taxaSeguro.setPago(false);
                    taxaSeguro.setDescricao("Seguro Obrigatório Anual de Encarregado: " + utilizadorSalvo.getNome());
                    taxaSeguro.setIdutilizador(utilizadorSalvo);
                    taxaSeguro.setIdTipoPagamento(tipoSeguro);
                    taxaSeguro.setDataPagamento(dataAtual);
                    taxaSeguro.setDataConfirmado(null);
                    taxaSeguro.setAula(null);
                    entityManager.persist(taxaSeguro);

                } catch (Exception e) {
                    System.err.println("Erro ao gerar pagamentos do encarregado: " + e.getMessage());
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Falha na configuração financeira do encarregado: " + e.getMessage());
                }

                // ─── INSCRIÇÃO NAS TURMAS ─────────────────────────────────────────
                for (String turmaHash : dto.idTurmasIniciais()) {
                    if (turmaHash != null && !turmaHash.trim().isEmpty()) {
                        try {
                            Integer idTurmaDecoded = idHasher.decode(turmaHash);
                            Turma turma = turmaRepository.findById(idTurmaDecoded)
                                    .orElseThrow(() -> new Exception("Turma não encontrada"));

                            TurmaEncarregadoId idIntermedio = new TurmaEncarregadoId();
                            idIntermedio.setTurmaId(turma.getId());
                            idIntermedio.setEncarregadoId(utilizadorSalvo.getId());

                            TurmaEncarregado inscricao = new TurmaEncarregado();
                            inscricao.setId(idIntermedio);
                            inscricao.setTurma(turma);
                            inscricao.setEncarregado(utilizadorSalvo);
                            inscricao.setInscritoEm(LocalDate.now());

                            turmaEncarregadoRepository.save(inscricao);
                        } catch (Exception e) {
                            System.err.println("Erro ao inscrever encarregado na turma [" + turmaHash + "]: " + e.getMessage());
                        }
                    }
                }
                turmaEncarregadoRepository.flush();
            }

            // ─── EDUCANDOS ────────────────────────────────────────────────────────
            if (dto.idEducandosIniciais() != null && !dto.idEducandosIniciais().isEmpty()) {
                for (String alunoHash : dto.idEducandosIniciais()) {
                    if (alunoHash != null && !alunoHash.trim().isEmpty()) {
                        try {
                            Integer idAlunoDecoded = idHasher.decode(alunoHash);
                            Aluno aluno = alunoRepository.findById(idAlunoDecoded)
                                    .orElseThrow(() -> new Exception("Aluno não encontrado"));

                            EncarregadoAlunoId chaveComposta = new EncarregadoAlunoId();
                            chaveComposta.setEncarregadoId(utilizadorSalvo.getId());
                            chaveComposta.setAlunoId(aluno.getId());

                            EncarregadoAluno associacao = new EncarregadoAluno();
                            associacao.setId(chaveComposta);
                            associacao.setEncarregado(utilizadorSalvo);
                            associacao.setAluno(aluno);

                            encarregadoAluno.save(associacao);
                        } catch (Exception e) {
                            System.err.println("Erro ao associar educando [" + alunoHash + "]: " + e.getMessage());
                        }
                    }
                }
                encarregadoAluno.flush();
            }
        }

        // Template HTML Original de Boas-Vindas
        String mensagem = "<div style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>"
                + "<h1 style='color: #2c3e50; border-bottom: 2px solid #e74c3c; padding-bottom: 10px;'>Bem-vindo à Escola EntArtes!</h1>"
                + "<p>Caro/a utilizador(a),</p>"
                + "<p>A sua conta foi criada com sucesso na nossa plataforma.</p>"
                + "<p>Para efetuar o seu primeiro acesso, utilize a seguinte palavra-passe temporária:</p>"
                + "<div style='text-align: center; margin: 25px 0;'>"
                + "  <span style='background:#fff4f4; padding:15px 25px; font-size: 22px; font-family: monospace; "
                + "  font-weight: bold; color: #c0392b; border: 2px dashed #e74c3c; border-radius: 5px; display: inline-block;'>"
                +    sb +
                "  </span>"
                + "</div>"
                + "<p style='background: #fff3cd; padding: 10px; border-radius: 5px; color: #856404;'>"
                + "<strong>⚠️ Importante:</strong> Por questões de segurança, é obrigatório alterar esta palavra-passe "
                + "assim que entrar na plataforma pela primeira vez.</p>"
                + "<p>Pode aceder ao portal através do link: <a href='http://localhost:3000/login' style='color: #3498db;'>Portal EntArtes</a></p>"
                + "<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>"
                + "<p>Estamos ansiosos por tê-lo(a) connosco!</p>"
                + "<p>Cumprimentos,<br><strong>Equipa de Gestão EntArtes</strong></p>"
                + "</div>";

        // Envio do e-mail de forma isolada
        try {
            emailService.enviaEmail(utilizadorSalvo.getEmail(), "Bem-vindo à Escola EntArtes - Dados de Acesso", mensagem);
        } catch (Exception mailEx) {
            System.err.println("Aviso: Não foi possível enviar o e-mail de boas-vindas: " + mailEx.getMessage());
        }

        // Converter e devolver o DTO oficial
        return toResponseDTO(utilizadorSalvo);
    }

    @Transactional
    public UtilizadorResponseDto editarUtilizador(String idHashed, EditarUtilizadorDto dto) throws Exception {
        Integer idUtilizador = idHasher.decode(idHashed);

        Utilizadore utilizador = utilizadoreRepository.findById(idUtilizador)
                .orElseThrow(() -> new EntityNotFoundException("Utilizador não encontrado"));

        //Atualizar dados comuns (Utilizadore)
        utilizador.setNome(dto.nome());
        utilizador.setEmail(dto.email());
        utilizador.setNif(dto.nif());
        utilizador.setTelefone(dto.telefone());
        utilizador.setDataNascimento(dto.dataNascimento());
        utilizador.setEditadoEm(LocalDateTime.now());

        Integer tipoId = utilizador.getTipo().getId();

        //Fluxo específico para PROFESSOR
        if (tipoId == 2) {
            Professore prof = professoreRepository.findById(idUtilizador)
                    .orElseThrow(() -> new Exception("Registo de professor não encontrado"));

            prof.setValorHora(dto.valorHora() != null ? dto.valorHora() : prof.getValorHora());
            prof.setProfessorExterno(dto.professorExterno() != null ? dto.professorExterno() : prof.getProfessorExterno());
            prof.setNotas(dto.notasProfessor());
            professoreRepository.save(prof);

            // ─── SINCRONIZAR MODALIDADES DO PROFESSOR ───
            professorModalidadeRepository.deleteByProfessorId(idUtilizador);

            if (dto.modalidadesIds() != null && !dto.modalidadesIds().isEmpty()) {
                for (String modalidadeHash : dto.modalidadesIds()) {
                    if (modalidadeHash != null && !modalidadeHash.trim().isEmpty()) {
                        try {
                            Integer idModalidadeDecoded = idHasher.decode(modalidadeHash);
                            Modalidade modalidade = modalidadeRepository.findById(idModalidadeDecoded)
                                    .orElseThrow(() -> new Exception("Modalidade não encontrada"));

                            ProfessorModalidadeId idIntermedio = new ProfessorModalidadeId();
                            idIntermedio.setProfessorId(idUtilizador);
                            idIntermedio.setModalidadeId(modalidade.getId());

                            ProfessorModalidade vinculo = new ProfessorModalidade();
                            vinculo.setId(idIntermedio);
                            vinculo.setProfessor(prof);
                            vinculo.setModalidade(modalidade);

                            professorModalidadeRepository.save(vinculo);
                        } catch (Exception e) {
                            System.err.println("Erro ao editar modalidade hash [" + modalidadeHash + "] do professor: " + e.getMessage());
                        }
                    }
                }
                professorModalidadeRepository.flush();
            }
        }
        // 3. Fluxo específico para ALUNO (Tipo ID = 3)
        else if (tipoId == 3) {
            Aluno aluno = alunoRepository.findById(idUtilizador)
                    .orElseThrow(() -> new Exception("Registo de aluno não encontrado"));

            aluno.setNotas(dto.notasProfessor());
            alunoRepository.save(aluno);

            // ─── SINCRONIZAR TURMAS DO ALUNO ───
            turmaAlunoRepository.deleteByAlunoId(idUtilizador);

            if (dto.idTurmasIniciais() != null && !dto.idTurmasIniciais().isEmpty()) {
                for (String turmaHash : dto.idTurmasIniciais()) {
                    if (turmaHash != null && !turmaHash.trim().isEmpty()) {
                        try {
                            Integer idTurmaDecoded = idHasher.decode(turmaHash);
                            Turma turma = turmaRepository.findById(idTurmaDecoded)
                                    .orElseThrow(() -> new Exception("Turma não encontrada"));

                            TurmaAlunoId idIntermedio = new TurmaAlunoId();
                            idIntermedio.setAlunoId(idUtilizador);
                            idIntermedio.setTurmaId(turma.getId());

                            TurmaAluno inscricao = new TurmaAluno();
                            inscricao.setId(idIntermedio);
                            inscricao.setAluno(aluno);
                            inscricao.setTurma(turma);
                            inscricao.setInscritoEm(LocalDate.now());

                            turmaAlunoRepository.save(inscricao);
                        } catch (Exception e) {
                            System.err.println("Erro ao inscrever aluno na turma hash [" + turmaHash + "] durante a edição: " + e.getMessage());
                        }
                    }
                }
                turmaAlunoRepository.flush();
            }
        }
        //Fluxo específico para ENCARREGADO / Outros Tipos
        else {
            // ─── SINCRONIZAR EDUCANDOS DO ENCARREGADO ───
            encarregadoAlunoRepository.deleteByEncarregado_Id(idUtilizador); // Garante que o nome do repo está igual ao teu (ex: encarregadoAlunoRepository)

            if (dto.idEducandosIniciais() != null && !dto.idEducandosIniciais().isEmpty()) {
                for (String alunoHash : dto.idEducandosIniciais()) {
                    if (alunoHash != null && !alunoHash.trim().isEmpty()) {
                        try {
                            Integer idAlunoDecoded = idHasher.decode(alunoHash);
                            Aluno aluno = alunoRepository.findById(idAlunoDecoded)
                                    .orElseThrow(() -> new Exception("Aluno não encontrado"));

                            //Criar e definir a chave composta primeiro!
                            ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EncarregadoAlunoId idIntermedio = new ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EncarregadoAlunoId();
                            idIntermedio.setEncarregadoId(idUtilizador);
                            idIntermedio.setAlunoId(aluno.getId());

                            EncarregadoAluno associacao = new EncarregadoAluno();
                            associacao.setId(idIntermedio); // 🟢 Definir o ID composto na entidade
                            associacao.setEncarregado(utilizador);
                            associacao.setAluno(aluno);

                            encarregadoAlunoRepository.save(associacao);
                        } catch (Exception e) {
                            System.err.println("Erro ao associar educando na edição hash [" + alunoHash + "]: " + e.getMessage());
                        }
                    }
                }
                encarregadoAlunoRepository.flush();
            }
        }
        Utilizadore salvo = utilizadoreRepository.save(utilizador);

        //RETORNO DO DTO ATUALIZADO
        if (tipoId == 2) {
            Professore profAtualizado = professoreRepository.findById(idUtilizador).get();
            entityManager.refresh(profAtualizado);
            return toResponseDTO(profAtualizado);
        } else if (tipoId == 3) {
            Aluno alunoAtualizado = alunoRepository.findById(idUtilizador).get();
            entityManager.refresh(alunoAtualizado);
            return toResponseDTO(alunoAtualizado);
        }

        // Para o encarregado, damos refresh à entidade base
        entityManager.refresh(salvo);
        return toResponseDTO(salvo);
    }
    @Transactional
    public void alterarPalavraPasse(String id, AlterarPasswordDto dto) throws Exception {

        Utilizadore utilizador = utilizadoreRepository.findById(idHasher.decode(id))
                .orElseThrow(() -> new UtilizadorNaoEncontradoException("Utilizador não logado"));

        // Confirmar que a password atual está correta
        if (!passwordEncoder.matches(dto.passwordAtual(), utilizador.getPalavraPasse())) {
            throw new Exception("Palavra Passe incorreta");
        }
        // Confirmar que a nova password e a confirmação coincidem
        if (!dto.novaPassword().equals(dto.confirmarNovaPassword())) {
            throw new Exception("Palavra Passe nova não coincide");
        }

        utilizador.setPalavraPasse(passwordEncoder.encode(dto.novaPassword()));
        utilizador.setEditadoEm(LocalDateTime.now());
        utilizadoreRepository.save(utilizador);
    }

    // ─── Desativar / Ativar utilizador ────────────────────────────────────────
    public UtilizadorResponseDto toggleAtivo(String id) {
        Utilizadore utilizador = utilizadoreRepository.findById(idHasher.decode(id))
                .orElseThrow(() -> new UtilizadorNaoEncontradoException(id));

        utilizador.setAtivo(!utilizador.getAtivo());
        utilizador.setEditadoEm(LocalDateTime.now());
        utilizadoreRepository.save(utilizador);
        return toResponseDTO(utilizador);
    }

    // ─── Repor palavra-passe (coordenação repõe a de outro utilizador) ────────
    // Não envolve tokens — a coordenação define diretamente uma nova password
    // e o utilizador é obrigado a alterá-la no próximo login

    public void reporPalavraPasse(String id, ReporPasswordDto dto) throws Exception {

        Utilizadore utilizador = utilizadoreRepository.findById(idHasher.decode(id))
                .orElseThrow(() -> new UtilizadorNaoEncontradoException(id));

        // Confirmar que a nova password e a confirmação coincidem
        if (!dto.novaPassword().equals(dto.confirmarNovaPassword())) {
            throw new Exception("Passwords não coincidem");

        }

        utilizador.setPalavraPasse(passwordEncoder.encode(dto.novaPassword()));
        utilizador.setEditadoEm(LocalDateTime.now());

        utilizadoreRepository.save(utilizador);
    }

    //Apagar utilizador
    @Transactional // CRUCIAL para garantir que se uma limpeza falhar, não apaga metade
    public void apagarUtilizador(String id) {
        Utilizadore utilizador = utilizadoreRepository.findById(idHasher.decode(id))
                .orElseThrow(() -> new UtilizadorNaoEncontradoException(id));

        utilizador.setAtivo(false);
        utilizador.setEditadoEm(LocalDateTime.now());

        utilizadoreRepository.save(utilizador);
    }

    // ─── 2. REMOÇÃO FÍSICA (Apagar para sempre com limpeza de FK) ─────────────
    @Transactional // Obrigatório para o deleteByProfessorId e outras remoções funcionarem bem
    public void eliminaUtilizador(String id) throws Exception {
        Integer idDecoded = idHasher.decode(id);

        Utilizadore utilizador = utilizadoreRepository.findById(idDecoded)
                .orElseThrow(() -> new Exception("Utilizador não encontrado"));

        // ==========================================================================================
        // LIMPEZA FINANCEIRA: Remove todos os pagamentos vinculados a este utilizador
        // ==========================================================================================
        try {
            entityManager.createQuery("DELETE FROM Pagamento p WHERE p.idutilizador.id = :id")
                    .setParameter("id", idDecoded)
                    .executeUpdate();
        } catch (Exception e) {
            System.err.println("Aviso ao limpar pagamentos: " + e.getMessage());
        }
        // ==========================================================================================

        // Se for um Professor, limpa primeiro os vínculos com as modalidades
        if (utilizador instanceof Professore) {
            professorModalidadeRepository.deleteByProfessorId(idDecoded);
        }
        // 2. Se for um Aluno, limpa as inscrições em turmas e o vínculo com o encarregado
        else if (utilizador instanceof Aluno) {
            // Limpa as turmas onde o aluno está inscrito
            turmaAlunoRepository.deleteByAlunoId(idDecoded);
            encarregadoAluno.deleteAllByAluno_Id(idDecoded);
        }

        else if (utilizador.getTipo() != null && (utilizador.getTipo().getId() == 4 || "ROLE_ENCARREGADO".equals(utilizador.getTipo().getTipoUtilizador()))) {
            encarregadoAluno.deleteAllByEncarregado_Id(idDecoded);
        }

        utilizadoreRepository.delete(utilizador);
    }

    // ─── Ver próprio perfil ───────────────────────────────────────────────────
    public UtilizadorResponseDto verMeuPerfil(String id) {
        Utilizadore utilizador = utilizadoreRepository.findById(idHasher.decode( id))
                .orElseThrow(() -> new UtilizadorNaoEncontradoException("Você"));
        return toResponseDTO(utilizador);
    }



    // ─── Mapper: Entity → DTO ─────────────────────────────────────────────────
    private UtilizadorResponseDto toResponseDTO(Utilizadore u) {
        List<TurmaDto> listaTurmasDto = new java.util.ArrayList<>();
        List<ModalidadeDto> listaModalidadesDto = new java.util.ArrayList<>();
        List<UtilizadoreResumoDto> listaEducandosDto = new java.util.ArrayList<>();

        Double valorHora = null;
        Boolean professorExterno = false;
        String encarregadoNome = null;

        // Se for Aluno (Tipo ID = 3), carrega as turmas E O ENCARREGADO
        if (u.getTipo() != null && u.getTipo().getId() == 3) {
            List<TurmaAluno> inscricoes = turmaAlunoRepository.findByAlunoId(u.getId());

            if (inscricoes != null) {
                listaTurmasDto = inscricoes.stream()
                        .map(inscricao -> {
                            Turma t = inscricao.getTurma();

                            ModalidadeDto modalidadeDto = null;
                            if (t.getModalidade() != null) {
                                modalidadeDto = new ModalidadeDto(
                                        idHasher.encode(t.getModalidade().getId()),
                                        t.getModalidade().getNome(),
                                        t.getModalidade().getDescricao()
                                );
                            }

                            return new TurmaDto(
                                    idHasher.encode(t.getId()),
                                    t.getNome(),
                                    t.getMensalidade(),
                                    modalidadeDto,
                                    t.getAtivo()
                            );
                        })
                        .toList();
            }


            // Como o aluno só pode ter 1 encarregado, filtramos pelo ID do Aluno e tiramos o primeiro que aparecer
            java.util.Optional<EncarregadoAluno> associacao = encarregadoAluno.findAll()
                    .stream()
                    .filter(ea -> ea.getAluno() != null && ea.getAluno().getId().equals(u.getId()))
                    .findFirst();

            if (associacao.isPresent() && associacao.get().getEncarregado() != null) {
                encarregadoNome = associacao.get().getEncarregado().getNome();
            }
        }
        // 3. Se for Professor (Tipo ID = 2), carrega as modalidades e os campos de professor
        else if (u.getTipo() != null && u.getTipo().getId() == 2) {

            Professore prof;
            if (u instanceof Professore) {
                prof = (Professore) u;
            } else {
                prof = professoreRepository.findById(u.getId()).orElse(null);
            }

            if (prof != null) {
                valorHora = prof.getValorHora() != null ? prof.getValorHora().doubleValue() : null;
                professorExterno = prof.getProfessorExterno();
            }

            List<ProfessorModalidade> vinculos = professorModalidadeRepository.findById_ProfessorId(u.getId());
            if (vinculos != null) {
                listaModalidadesDto = vinculos.stream()
                        .map(vinculo -> {
                            Modalidade m = vinculo.getModalidade();
                            return new ModalidadeDto(
                                    idHasher.encode(m.getId()),
                                    m.getNome(),
                                    m.getDescricao()
                            );
                        })
                        .toList();
            }
        }
        // 4. Se for Encarregado, carrega os educandos associados aproveitando o findEducandosdeEducador
        else if (u.getTipo() != null && (u.getTipo().getId() == 4 || "ROLE_ENCARREGADO".equals(u.getTipo().getTipoUtilizador()))) {
            listaEducandosDto = findEducandosdeEducador(u.getId());
        }

        // 5. Devolvemos o DTO perfeitamente preenchido com os 15 parâmetros exatos exigidos pelo Record atualizado
        return new UtilizadorResponseDto(
                idHasher.encode(u.getId()),
                u.getNome(),
                u.getEmail(),
                u.getNif(),
                u.getTelefone(),
                u.getTipo() != null ? u.getTipo().getTipoUtilizador() : null,
                u.getAtivo(),
                u.getDataNascimento(),
                u.getCriadoEm(),
                valorHora,
                professorExterno,
                listaTurmasDto,
                listaModalidadesDto,
                listaEducandosDto,
                encarregadoNome
        );
    }

    @Transactional
    public void associarAlunoAEncarregado(String idAlunoHashed, String idEncarregadoHashed) throws Exception {

        Integer idAluno = idHasher.decode(idAlunoHashed);
        Integer idEncarregado = idHasher.decode(idEncarregadoHashed);

        Utilizadore encarregado = utilizadoreRepository.findById(idEncarregado)
                .orElseThrow(() -> new UtilizadorNaoEncontradoException(idEncarregadoHashed));

        Aluno aluno = alunoRepository.findById(idAluno)
                .orElseThrow(() -> new Exception("Aluno não encontrado"));

        // Verifica se a associação já existe para não duplicar
        boolean jaExiste = encarregadoAluno.existsByEncarregado_IdAndAluno_Id(idEncarregado, idAluno);
        if (jaExiste) {
            throw new Exception("Este aluno já está associado a este encarregado.");
        }

        // Mapear a chave composta para evitar o erro do Hibernate Setter
        ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EncarregadoAlunoId chaveComposta = new ipcaProjeto50.Grupo62026.SiteEntArtes.entity.EncarregadoAlunoId();
        chaveComposta.setEncarregadoId(idEncarregado);
        chaveComposta.setAlunoId(idAluno);

        EncarregadoAluno associacao = new EncarregadoAluno();
        associacao.setId(chaveComposta); // 🟢 Define a chave ID na entidade
        associacao.setEncarregado(encarregado);
        associacao.setAluno(aluno);

        encarregadoAluno.save(associacao);
    }

    // ─── Remover associação aluno-encarregado ─────────────────────────────────
    @Transactional
    public void removerAssociacaoAlunoEncarregado(String idAlunoHashed, String idEncarregadoHashed) throws Exception {

        Integer idAluno = idHasher.decode(idAlunoHashed);
        Integer idEncarregado = idHasher.decode(idEncarregadoHashed);

        EncarregadoAluno associacao = encarregadoAluno
                .findByEncarregado_IdAndAluno_Id(idEncarregado, idAluno)
                .orElseThrow(() -> new Exception("Associação não encontrada."));

        encarregadoAluno.delete(associacao);
    }

    public List<UtilizadoreResumoDto> findEducandosdeEducador(Integer idEducador) {
        return encarregadoAluno.findAllByEncarregado_Id(idEducador)
                .stream()
                .map(ea -> new UtilizadoreResumoDto(
                        idHasher.encode(ea.getAluno().getId()),
                        ea.getAluno().getNome()
                ))
                .toList();
    }

    public List<UtilizadoreResumoDto> findEducandosdeEducador(String idEducador) {
        return findEducandosdeEducador(idHasher.decode(idEducador));
    }

    private void removeTokensExpirados(){
        tokenRecuperacaoRepository.deleteAllByExpiraEmBefore(LocalDateTime.now());
    }

    public String geraToken(String email) throws Exception {
        removeTokensExpirados();
        Utilizadore utilizador = utilizadoreRepository.findByEmail(email)
                .orElseThrow(() -> new UtilizadorNaoEncontradoException("Email não encontrado"));
        String token = String.valueOf(100000 + GeneradorRandomico.nextInt(900000));
        while (tokenRecuperacaoRepository.existsByToken(token) ){
            token = String.valueOf(100000 + GeneradorRandomico.nextInt(900000));
        }
        // Aplica o BCrypt (Gera o Hash único com Salt)
        String hash = BCrypt.hashpw(token, BCrypt.gensalt());
        //ACEITA ATÉ 15 MIN
        TokenRecuperacao tokenSalvo = tokenRecuperacaoRepository.save(new TokenRecuperacao(null, utilizador, hash,LocalDateTime.now().plusMinutes(15)));

        if (tokenSalvo.getId() != null) {
            // O token foi persistido com sucesso!
            // AGORA: Envie o 'tokenOriginal' por e-mail (nunca envie o hash)
            String mensagem = "<p>Caro/a utilizador(a),</p>"
                    + "<p>Recebemos um pedido de recuperação de acesso.</p>"
                    + "<p>O seu token de recuperação é:</p>"
                    + "<h2 style='background:#f4f4f4; padding:10px; display:inline-block; border-radius:5px;'>"
                    + token +
                    "</h2>"
                    + "<p>Este código é válido por 15 minutos</p>"
                    + "<p>Se não solicitou esta operação, ignore este email.</p>"
                    + "<p>Cumprimentos,<br>Equipa de Suporte</p>";

            emailService.enviaEmail(utilizador.getEmail(), "Token de Recuperação", mensagem);
            System.out.println("Token gerado e salvo com sucesso.");
        } else {
            throw new Exception("Erro ao gerar token de recuperação.");
        }
        return token;
    }
    public void atualizaPassSemLogin(AlterarPasswordSemLoginDto dto) throws Exception {
        //Procurar o token no banco pelo ID do utilizador (ou apenas pelo hash se preferir)
        TokenRecuperacao recuperacao = tokenRecuperacaoRepository.findFirstByIdUtilizador_EmailOrderByExpiraEmDesc(dto.email()).orElseThrow(() -> new Exception("Token inválido ou inexistente"));

        //Verificar se expirou
        if (recuperacao.getExpiraEm().isBefore(LocalDateTime.now())) {
            tokenRecuperacaoRepository.delete(recuperacao);
            throw new Exception("O token expirou!");
        }

        // 3. O BCrypt NÃO permite buscar por "token" direto se for hash.
        if (!BCrypt.checkpw(dto.token(), recuperacao.getToken())) {
            throw new Exception("Token incorreto!");
        }

        //Se chegou aqui, é válido! Atualizar a senha do utilizador
        Utilizadore user = recuperacao.getIdUtilizador();
        user.setPalavraPasse(passwordEncoder.encode(dto.novaPassword()));
        utilizadoreRepository.save(user);

        //Apagar o token para não ser usado de novo
        tokenRecuperacaoRepository.delete(recuperacao);
    }
    public List<UtilizadoreResumoDto> listarContactosDisponiveis(String idLogadoHashed) {
        //Descodificamos o ID para saber quem é o utilizador atual
        Integer idRealLogado = idHasher.decode(idLogadoHashed);

        return utilizadoreRepository.findAll().stream()
                .filter(u -> !u.getId().equals(idRealLogado)) // Filtra pelo ID numérico
                .map(u -> new UtilizadoreResumoDto(
                        idHasher.encode(u.getId()),
                        u.getNome()
                ))
                .toList();
    }
    public List<Utilizadore> findAllCoordenacao() {
        return utilizadoreRepository.findAllByTipo_Id(1);
    }
    public void verificaPermissaoEducando(String educandoId, String educadorId) throws Exception {
        boolean temPermissao = findEducandosdeEducador(educadorId)
                .stream()
                .anyMatch(u -> u.id().equals(educandoId));
        if (!temPermissao) throw new Exception("Não tem permissão para aceder a este educando");
    }
    public boolean possuiEducando(String idAluno){
        return encarregadoAluno.existsByAluno_Id(idHasher.decode(idAluno));
    }

    public Optional<Utilizadore> findByEmail(@NotBlank(message = "O email não pode estar vazio") @Email(message = "Formato de email inválido") String email) {
        return utilizadoreRepository.findByEmail(email);
    }
    public List<UtilizadoreResumoDto> pesquisarPorNome(String nome) {
        if (nome == null || nome.trim().length() < 3) return List.of();
        return utilizadoreRepository.findByNomeContainingIgnoreCase(nome.trim())
                .stream()
                .map(u -> new UtilizadoreResumoDto(idHasher.encode(u.getId()), u.getNome()))
                .toList();
    }
    public List<UtilizadoreResumoDto> listarAlunosMenoresParaAssociacao(String termoPesquisa) {
        // Busca os alunos ativos. No teu repositório podes filtrar por nome e validar a idade
        List<Aluno> alunos;
        if (termoPesquisa != null && !termoPesquisa.isBlank()) {
            alunos = alunoRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(termoPesquisa);
        } else {
            alunos = alunoRepository.findAllByAtivoTrue();
        }

        // Filtra na API (ou na query) apenas os que são menores de idade
        return alunos.stream()
                .filter(Aluno::isMenorIdade) // Usa o método isMenorIdade() que já tens na entidade!
                .map(aluno -> new UtilizadoreResumoDto(
                        idHasher.encode(aluno.getId()),
                        aluno.getNome()
                ))
                .collect(Collectors.toList());
    }
}