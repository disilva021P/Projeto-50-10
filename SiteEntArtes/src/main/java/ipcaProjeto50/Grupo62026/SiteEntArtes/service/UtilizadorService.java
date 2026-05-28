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

            // CRUCIAL: Passa o professor para a variável comum 'utilizador'
            utilizador = prof;

        } else { // Outro tipo genérico (ex: Encarregado de Educação)
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

        // Gerador original da Palavra-Passe Temporária (12 caracteres)
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

            if (dto.idTurmasIniciais() != null && !dto.idTurmasIniciais().isEmpty()) {
                // Iterar por todas as hashes de turmas enviadas pelo Frontend
                for (String turmaHash : dto.idTurmasIniciais()) {
                    if (turmaHash != null && !turmaHash.trim().isEmpty()) {
                        try {
                            // 1. Decifrar o ID de cada turma individualmente
                            Integer idTurmaDecoded = idHasher.decode(turmaHash);

                            // 2. Procurar a turma correspondente na base de dados
                            Turma turma = turmaRepository.findById(idTurmaDecoded)
                                    .orElseThrow(() -> new Exception("Turma não encontrada"));

                            // 3. Criar a chave composta (ID Aluno + ID Turma)
                            TurmaAlunoId identificadorIntermedio = new TurmaAlunoId();
                            identificadorIntermedio.setAlunoId(utilizadorSalvo.getId());
                            identificadorIntermedio.setTurmaId(turma.getId());

                            // 4. Criar a entidade intermédia e associar os objetos
                            TurmaAluno inscricao = new TurmaAluno();
                            inscricao.setId(identificadorIntermedio);
                            inscricao.setAluno((Aluno) utilizadorSalvo);
                            inscricao.setTurma(turma);

                            inscricao.setInscritoEm(java.time.LocalDate.now());

                            // 5. Guardar na tabela turma_alunos
                            turmaAlunoRepository.save(inscricao);
                        } catch (Exception e) {
                            System.err.println("Erro ao inscrever aluno na turma hash [" + turmaHash + "]: " + e.getMessage());
                        }
                    }
                }
            }

        } else if (utilizador instanceof Professore) {
            // 1. Gravar o professor na BD para gerar o ID original dele
            utilizadorSalvo = professoreRepository.save((Professore) utilizador);
            Professore profSalvo = (Professore) utilizadorSalvo;

            // ─── BLOCO DE GRAVAÇÃO DAS MODALIDADES DO PROFESSOR ──────────────────────
            if (dto.modalidadesIds() != null && !dto.modalidadesIds().isEmpty()) {
                for (String modalidadeHash : dto.modalidadesIds()) {
                    if (modalidadeHash != null && !modalidadeHash.trim().isEmpty()) {
                        try {
                            // 1. Decifrar o ID da modalidade que veio do Frontend (Garante que é Integer)
                            Integer idModalidadeDecoded = idHasher.decode(modalidadeHash);

                            // 2. Procurar a modalidade na base de dados
                            Modalidade modalidade = modalidadeRepository.findById(idModalidadeDecoded)
                                    .orElseThrow(() -> new Exception("Modalidade não encontrada com o ID: " + idModalidadeDecoded));

                            // 3. Criar a chave composta
                            ProfessorModalidadeId identificadorIntermedio = new ProfessorModalidadeId();
                            identificadorIntermedio.setProfessorId(profSalvo.getId());
                            identificadorIntermedio.setModalidadeId(modalidade.getId());

                            // 4. Criar a entidade intermédia e associar os objetos
                            ProfessorModalidade vinculo = new ProfessorModalidade();
                            vinculo.setId(identificadorIntermedio);
                            vinculo.setProfessor(profSalvo);
                            vinculo.setModalidade(modalidade);

                            // 5. Guardar definitivamente na tabela professor_modalidade
                            professorModalidadeRepository.save(vinculo);

                        } catch (Exception e) {
                            System.err.println("🚨 ERRO REAL ao associar professor à modalidade hash [" + modalidadeHash + "]:");
                            e.printStackTrace();
                        }
                    }
                }
                // 6. CRUCIAL: Força o Spring a descarregar tudo para a BD ANTES de converter para o ResponseDto
                professorModalidadeRepository.flush();
            }

        } else {
            // Guarda o utilizador genérico (Encarregado) primeiro para gerar o ID dele
            utilizadorSalvo = utilizadoreRepository.save(utilizador);

            // ─── BLOCO DE GRAVAÇÃO DOS EDUCANDOS INICIAIS DO ENCARREGADO ──────
            if (dto.idEducandosIniciais() != null && !dto.idEducandosIniciais().isEmpty()) {
                for (String alunoHash : dto.idEducandosIniciais()) {
                    if (alunoHash != null && !alunoHash.trim().isEmpty()) {
                        try {
                            // 1. Decifrar a Hash do ID do Aluno
                            Integer idAlunoDecoded = idHasher.decode(alunoHash);

                            // 2. Buscar a entidade do Aluno
                            Aluno aluno = alunoRepository.findById(idAlunoDecoded)
                                    .orElseThrow(() -> new Exception("Aluno não encontrado"));

                            // 3. Construir a entidade intermédia EncarregadoAluno
                            EncarregadoAluno associacao = new EncarregadoAluno();
                            associacao.setEncarregado(utilizadorSalvo);
                            associacao.setAluno(aluno);

                            // 4. Guardar na tabela intermédia encarregado_aluno
                            encarregadoAluno.save(associacao);

                        } catch (Exception e) {
                            System.err.println("Erro ao associar educando inicial hash [" + alunoHash + "] ao encarregado: " + e.getMessage());
                        }
                    }
                }
                // Garante a persistência imediata das ligações na base de dados
                encarregadoAluno.flush();
            }
            // ─────────────────────────────────────────────────────────────────────────
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

        // 1. Atualizar dados comuns (Utilizadore)
        utilizador.setNome(dto.nome());
        utilizador.setEmail(dto.email());
        utilizador.setNif(dto.nif());
        utilizador.setTelefone(dto.telefone());
        utilizador.setDataNascimento(dto.dataNascimento());
        utilizador.setEditadoEm(LocalDateTime.now());

        Integer tipoId = utilizador.getTipo().getId();

        // 2. Fluxo específico para PROFESSOR (Tipo ID = 2)
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
        // 4. [NOVO] Fluxo específico para ENCARREGADO / Outros Tipos
        else {
            // ─── SINCRONIZAR EDUCANDOS DO ENCARREGADO ───
            encarregadoAluno.deleteByEncarregado_Id(idUtilizador);

            if (dto.idEducandosIniciais() != null && !dto.idEducandosIniciais().isEmpty()) {
                for (String alunoHash : dto.idEducandosIniciais()) {
                    if (alunoHash != null && !alunoHash.trim().isEmpty()) {
                        try {
                            Integer idAlunoDecoded = idHasher.decode(alunoHash);
                            Aluno aluno = alunoRepository.findById(idAlunoDecoded)
                                    .orElseThrow(() -> new Exception("Aluno não encontrado"));

                            EncarregadoAluno associacao = new EncarregadoAluno();
                            associacao.setEncarregado(utilizador);
                            associacao.setAluno(aluno);

                            encarregadoAluno.save(associacao);
                        } catch (Exception e) {
                            System.err.println("Erro ao associar educando na edição hash [" + alunoHash + "]: " + e.getMessage());
                        }
                    }
                }
                encarregadoAluno.flush(); // Garante que a BD atualiza antes do refresh do EntityManager
            }
        }

        Utilizadore salvo = utilizadoreRepository.save(utilizador);

        // ══ RETORNO DO DTO ATUALIZADO EM CACHE ══
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
    @Transactional // CRÍTICO: Garante que tudo corre numa única transação
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
    @Transactional // Obrigatório para o deleteByProfessorId funcionar bem
    public void eliminaUtilizador(String id) throws Exception {
        Integer idDecoded = idHasher.decode(id);

        Utilizadore utilizador = utilizadoreRepository.findById(idDecoded)
                .orElseThrow(() -> new Exception("Utilizador não encontrado"));

        // Se for um Professor, limpa primeiro os vínculos com as modalidades
        if (utilizador instanceof Professore) {
            professorModalidadeRepository.deleteByProfessorId(idDecoded);
        } else if (utilizador instanceof Aluno) {
            // Se no futuro o Aluno tiver tabelas intermédias, limpas aqui
            // de igual forma (ex: turmaAlunoRepository.deleteByAlunoId(idDecoded);)
        }

        // Agora que a tabela intermédia está limpa, o SQL deixa apagar o utilizador!
        utilizadoreRepository.delete(utilizador);
    }

    // ─── Ver próprio perfil ───────────────────────────────────────────────────
    public UtilizadorResponseDto verMeuPerfil(String id) {
        Utilizadore utilizador = utilizadoreRepository.findById(idHasher.decode( id))
                .orElseThrow(() -> new UtilizadorNaoEncontradoException("Você"));
        return toResponseDTO(utilizador);
    }

    // ─── Mapper: Entity → DTO ─────────────────────────────────────────────────
    // ─── Mapper: Entity → DTO ─────────────────────────────────────────────────
    // ─── Mapper: Entity → DTO ─────────────────────────────────────────────────
    private UtilizadorResponseDto toResponseDTO(Utilizadore u) {
        // 1. Criamos as listas vazias prontas a receber dados
        List<TurmaDto> listaTurmasDto = new java.util.ArrayList<>();
        List<ModalidadeDto> listaModalidadesDto = new java.util.ArrayList<>();

        // [NOVO] Criar variáveis para o valorHora e regime, a começar vazias (null / false)
        Double valorHora = null;
        Boolean professorExterno = false;

        // 2. Se for Aluno (Tipo ID = 3), carrega as turmas
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
                                        t.getModalidade().getNome()
                                );
                            }

                            return new TurmaDto(
                                    idHasher.encode(t.getId()),
                                    t.getNome(),
                                    t.getMensalidade(),
                                    modalidadeDto
                            );
                        })
                        .toList();
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
                // 👇 LINHA CORRIGIDA AQUI 👇
                valorHora = prof.getValorHora() != null ? prof.getValorHora().doubleValue() : null;
                professorExterno = prof.getProfessorExterno();
            }

            List<ProfessorModalidade> vinculos = professorModalidadeRepository.findById_ProfessorId(u.getId());
            // ... resto do código igual ...
            if (vinculos != null) {
                listaModalidadesDto = vinculos.stream()
                        .map(vinculo -> {
                            Modalidade m = vinculo.getModalidade();
                            return new ModalidadeDto(
                                    idHasher.encode(m.getId()),
                                    m.getNome()
                            );
                        })
                        .toList();
            }
        }

        // 4. Devolvemos o DTO perfeitamente preenchido com as variáveis corretas
        return new UtilizadorResponseDto(
                idHasher.encode(u.getId()),
                u.getNome(),
                u.getEmail(),
                u.getNif(),
                u.getTelefone(),
                u.getTipo().getTipoUtilizador(),
                u.getAtivo(),
                u.getDataNascimento(),
                u.getCriadoEm(),
                valorHora,          // 👈 Passa a variável local corrigida
                professorExterno,    // 👈 Passa a variável local corrigida
                listaTurmasDto,
                listaModalidadesDto
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

        EncarregadoAluno associacao = new EncarregadoAluno();
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
        // 1. Procurar o token no banco pelo ID do utilizador (ou apenas pelo hash se preferir)
        // Aqui assumo que o DTO traz o token digitado e a nova senha
        TokenRecuperacao recuperacao = tokenRecuperacaoRepository.findFirstByIdUtilizador_EmailOrderByExpiraEmDesc(dto.email()).orElseThrow(() -> new Exception("Token inválido ou inexistente"));

        // 2. Verificar se expirou
        if (recuperacao.getExpiraEm().isBefore(LocalDateTime.now())) {
            tokenRecuperacaoRepository.delete(recuperacao);
            throw new Exception("O token expirou!");
        }

        // 3. O BCrypt NÃO permite buscar por "token" direto se for hash.
        // Você deve buscar o registro e usar o checkpw:

        if (!BCrypt.checkpw(dto.token(), recuperacao.getToken())) {
            throw new Exception("Token incorreto!");
        }

        // 4. Se chegou aqui, é válido! Atualizar a senha do utilizador
        Utilizadore user = recuperacao.getIdUtilizador();
        user.setPalavraPasse(passwordEncoder.encode(dto.novaPassword()));
        utilizadoreRepository.save(user);

        // 5. Apagar o token para não ser usado de novo
        tokenRecuperacaoRepository.delete(recuperacao);
    }
    public List<UtilizadoreResumoDto> listarContactosDisponiveis(String idLogadoHashed) {
        // Descodificamos o ID para saber quem é o utilizador atual
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
        // Aqui fazemos um filtro simples por nome se o termo for enviado
        List<Aluno> alunos;
        if (termoPesquisa != null && !termoPesquisa.isBlank()) {
            alunos = alunoRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(termoPesquisa);
        } else {
            alunos = alunoRepository.findAllByAtivoTrue();
        }

        // Filtramos na API (ou na query) apenas os que são menores de idade
        return alunos.stream()
                .filter(Aluno::isMenorIdade) // Usa o método isMenorIdade() que já tens na entidade!
                .map(aluno -> new UtilizadoreResumoDto(
                        idHasher.encode(aluno.getId()),
                        aluno.getNome()
                ))
                .collect(Collectors.toList());
    }
}

