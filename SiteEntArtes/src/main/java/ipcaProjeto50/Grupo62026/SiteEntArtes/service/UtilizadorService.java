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

        } else { // Outro tipo genérico
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
            // ───────────────────────────────────────────────────────────────────────────────────

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
                            // NOTA: Se o teu ModalidadeRepository usar Long, faz a conversão: Long.valueOf(idModalidadeDecoded)
                            Modalidade modalidade = modalidadeRepository.findById(idModalidadeDecoded)
                                    .orElseThrow(() -> new Exception("Modalidade não encontrada com o ID: " + idModalidadeDecoded));

                            // 3. Criar a chave composta (Garantir correspondência de tipos Integer)
                            ProfessorModalidadeId identificadorIntermedio = new ProfessorModalidadeId();
                            identificadorIntermedio.setProfessorId(profSalvo.getId()); // Verifica se o getId() do utilizador é Integer
                            identificadorIntermedio.setModalidadeId(modalidade.getId());

                            // 4. Criar a entidade intermédia e associar os objetos
                            ProfessorModalidade vinculo = new ProfessorModalidade();
                            vinculo.setId(identificadorIntermedio);
                            vinculo.setProfessor(profSalvo);
                            vinculo.setModalidade(modalidade);

                            // 5. Guardar definitivamente na tabela professor_modalidade
                            professorModalidadeRepository.save(vinculo);

                        } catch (Exception e) {
                            // ISTO VAI MOSTRAR NO TEU CMD O ERRO REAL CASO ALGO FALHE internamente
                            System.err.println("🚨 ERRO REAL ao associar professor à modalidade hash [" + modalidadeHash + "]:");
                            e.printStackTrace();
                        }
                    }
                }
                // 6. CRUCIAL: Força o Spring a descarregar tudo para a BD ANTES de converter para o ResponseDto
                professorModalidadeRepository.flush();
            }
            // ─────────────────────────────────────────────────────────────────────────
        } else {
            utilizadorSalvo = utilizadoreRepository.save(utilizador);
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

        // Envio do e-mail de forma isolada para evitar que falhas no servidor de mail deitem a transação abaixo
        try {
            emailService.enviaEmail(utilizadorSalvo.getEmail(), "Bem-vindo à Escola EntArtes - Dados de Acesso", mensagem);
        } catch (Exception mailEx) {
            // Log discreto no terminal caso o servidor de e-mail local falhe
            System.err.println("Aviso: Não foi possível enviar o e-mail de boas-vindas: " + mailEx.getMessage());
        }

        // Converter e devolver o DTO oficial
        return toResponseDTO(utilizadorSalvo);
    }
    @Transactional
    public UtilizadorResponseDto editarUtilizador(String idHashed, EditarUtilizadorDto dto) {
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

        // 2. Atualizar dados específicos (Sem interromper o fluxo)
        Integer tipoId = utilizador.getTipo().getId();

        if (tipoId == 2) { // Professor
            professoreRepository.findById(idUtilizador).ifPresent(p -> {
                p.setValorHora(dto.valorHora() != null ? dto.valorHora() : p.getValorHora());
                p.setProfessorExterno(dto.professorExterno() != null ? dto.professorExterno() : p.getProfessorExterno());
                p.setNotas(dto.notasProfessor());
                professoreRepository.save(p);
            });
        } else if (tipoId == 3) { // Aluno
            alunoRepository.findById(idUtilizador).ifPresent(a -> {
                a.setNotas(dto.notasProfessor());
                alunoRepository.save(a);
            });
        }

        // 3. Um único save e um único return para tudo
        // O save aqui garante que as alterações no Utilizadore (nome, email, etc) são persistidas
        Utilizadore salvo = utilizadoreRepository.save(utilizador);
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

    //Apagar Utilizador
    public void apagarUtilizador(String id) {

        Utilizadore utilizador = utilizadoreRepository.findById(idHasher.decode(id))
                .orElseThrow(() -> new UtilizadorNaoEncontradoException(id));

        utilizador.setAtivo(false);
        utilizador.setEditadoEm(LocalDateTime.now());

        utilizadoreRepository.save(utilizador);
    }
    public void eliminaUtilizador(String id){
        utilizadoreRepository.deleteById(idHasher.decode(id));
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
                                    modalidadeDto,
                                    t.getAtivo()
                            );
                        })
                        .toList();
            }
        }
        // 3. [NOVO]: Se for Professor (Tipo ID = 2), carrega as modalidades da tabela intermédia
        else if (u.getTipo() != null && u.getTipo().getId() == 2) {
            List<ProfessorModalidade> vinculos = professorModalidadeRepository.findById_ProfessorId(u.getId());

            if (vinculos != null) {
                listaModalidadesDto = vinculos.stream()
                        .map(vinculo -> {
                            Modalidade m = vinculo.getModalidade();

                            // Constrói o ModalidadeDto com o ID codificado em Hash e o nome
                            return new ModalidadeDto(
                                    idHasher.encode(m.getId()),
                                    m.getNome()
                            );
                        })
                        .toList();
            }
        }

        // 4. Devolvemos o DTO perfeitamente preenchido incluindo o novo campo de modalidades
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
                listaTurmasDto,     // Vai preenchida se for aluno, vazia se for professor
                listaModalidadesDto // Vai preenchida se for professor, vazia se for aluno
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
}