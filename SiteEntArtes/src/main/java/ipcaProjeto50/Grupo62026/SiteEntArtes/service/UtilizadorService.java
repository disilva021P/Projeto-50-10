package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.exception.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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

    @Transactional
    // ─── Criar utilizador (só coordenação) ───────────────────────────────────
    public UtilizadorResponseDto criarUtilizador(CriarUtilizadorDto dto) throws Exception {

        // Normalizar tipo
        String tipoid = dto.id_tipoUtilizador();
        Integer idTipoDecoded = idHasher.decode(tipoid);
        // Buscar tipo na base de dados (ex: ROLE_COORDENACAO)
        TipoUtilizador tipo = tipoUtilizadorRepository
                .findById(idHasher.decode(tipoid))
                .orElseThrow(() -> new Exception("Tipo de utilizador não encontrado"));
        // Criar entidade Utilizador
        Utilizadore utilizador;
        if (idTipoDecoded == 3) { // Assumindo que 3 é Aluno
            utilizador = new Aluno();
            // O campo 'notas' é específico de Aluno, podes inicializá-lo aqui se necessário
            ((Aluno) utilizador).setNotas("");
        }else if(idTipoDecoded == 2){
            utilizador = new Professore();
        }
        else {
            utilizador = new Utilizadore();
        }
        utilizador.setNome(dto.nome());
        utilizador.setEmail(dto.email());
        utilizador.setNif(dto.nif());
        utilizador.setTelefone(dto.telefone());
        utilizador.setTipo(tipo);
        if(tipo.getId()==3 && utilizador.isMenorIdade()){
            utilizador.setAtivo(false);
        }
        else{
            utilizador.setAtivo(true);

        }
        utilizador.setDataNascimento(dto.dataNascimento());
        utilizador.setCriadoEm(LocalDateTime.now());
        utilizador.setEditadoEm(LocalDateTime.now());
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            int index = GeneradorRandomico.nextInt(LETRAS.length());
            sb.append(LETRAS.charAt(index));
        }
        utilizador.setPalavraPasse(passwordEncoder.encode(sb));
        if (utilizador instanceof Aluno) {
            alunoRepository.save((Aluno) utilizador);
        } else if (utilizador instanceof Professore) {
            professoreRepository.save((Professore) utilizador);
        } else {
            utilizadoreRepository.save(utilizador);
        }
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

        // Envio do e-mail com o assunto de Boas-vindas
        emailService.enviaEmail(utilizador.getEmail(), "Bem-vindo à Escola EntArtes - Dados de Acesso", mensagem);
        // Converter e devolver DTO
        return toResponseDTO(utilizador);
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
    private UtilizadorResponseDto toResponseDTO(Utilizadore u) {
        return new UtilizadorResponseDto(
                idHasher.encode(u.getId()),
                u.getNome(),
                u.getEmail(),
                u.getNif(),
                u.getTelefone(),
                u.getTipo().getTipoUtilizador(),
                u.getAtivo(),
                u.getDataNascimento(),
                u.getCriadoEm()
        );
    }

    private TipoUtilizadorDto FindTipoById(String id) throws Exception {
        TipoUtilizador tipo = tipoUtilizadorRepository
                .findById(idHasher.decode(id))
                .orElseThrow(() -> new Exception("Tipo de utilizador não encontrado"));
        return new TipoUtilizadorDto(id, tipo.getTipoUtilizador());

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

}