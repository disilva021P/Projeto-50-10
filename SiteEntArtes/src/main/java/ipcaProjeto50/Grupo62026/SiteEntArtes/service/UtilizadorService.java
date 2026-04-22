package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.exception.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilizadorService {
    private final SecureRandom GeneradorRandomico = new SecureRandom();
    private final UtilizadoreRepository utilizadoreRepository;
    private final EncarregadoAlunoRepository encarregadoAluno;
    private final TipoUtilizadorRepository tipoUtilizadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdHasher idHasher;
    private final TokenRecuperacaoRepository tokenRecuperacaoRepository;
    private final EmailService emailService;
    // ─── Listar todos, com filtro opcional por tipo ───────────────────────────
    public List<UtilizadorResponseDto> listarTodos(String tipoFiltro) {
        List<Utilizadore> lista;

        if (tipoFiltro != null && !tipoFiltro.isBlank()) {
            String tipoNormalizado = tipoFiltro;
            lista = utilizadoreRepository.findByTipo_TipoUtilizador(tipoNormalizado);
        } else {
            lista = utilizadoreRepository.findAll();
        }

        return lista.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ─── Ver detalhe de um utilizador ────────────────────────────────────────
    public UtilizadorResponseDto verDetalhe(String id) {
        Utilizadore utilizador = utilizadoreRepository.findById(idHasher.decode(id))
                .orElseThrow(() -> new UtilizadorNaoEncontradoException(id));
        return toResponseDTO(utilizador);
    }

    // ─── Criar utilizador (só coordenação) ───────────────────────────────────
    public UtilizadorResponseDto criarUtilizador(CriarUtilizadorDto dto) throws Exception {

        // Normalizar tipo
        String tipoNome = dto.id_tipoUtilizador().toUpperCase();

        // Buscar tipo na base de dados (ex: ROLE_COORDENACAO)
        TipoUtilizador tipo = tipoUtilizadorRepository
                .findAllByTipoUtilizador(tipoNome)
                .orElseThrow(() -> new Exception("Tipo de utilizador não encontrado"));

        // Criar entidade Utilizador
        Utilizadore utilizador;
        utilizador = new Utilizadore();
        utilizador.setNome(dto.nome());
        utilizador.setEmail(dto.email());
        utilizador.setTelefone(dto.telefone());
        utilizador.setTipo(tipo);
        utilizador.setAtivo(true);
        utilizador.setDataNascimento(dto.dataNascimento());
        utilizador.setCriadoEm(LocalDateTime.now());
        utilizador.setPalavraPasse(passwordEncoder.encode(dto.palavraPasseTemporaria()));

        // Guardar na base de dados
        utilizadoreRepository.save(utilizador);

        // Converter e devolver DTO
        return toResponseDTO(utilizador);
    }

    // ─── Alterar palavra-passe (utilizador autenticado) ──────────────────────
    // O utilizador fornece a password atual para confirmar que é ele
    // e define uma nova password

    public void alterarPalavraPasse(String id, AlterarPasswordDto dto) {

        Utilizadore utilizador = utilizadoreRepository.findById(idHasher.decode(id))
                .orElseThrow(() -> new UtilizadorNaoEncontradoException("Utilizador não logado"));

        // Confirmar que a password atual está correta
        if (!passwordEncoder.matches(dto.passwordAtual(), utilizador.getPalavraPasse())) {
            throw new RuntimeException();
        }
        // Confirmar que a nova password e a confirmação coincidem
        if (!dto.novaPassword().equals(dto.confirmarNovaPassword())) {
            throw new RuntimeException();
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

    public void reporPalavraPasse(String id, ReporPasswordDto dto) {

        Utilizadore utilizador = utilizadoreRepository.findById(idHasher.decode(id))
                .orElseThrow(() -> new UtilizadorNaoEncontradoException(id));

        // Confirmar que a nova password e a confirmação coincidem
        if (!dto.novaPassword().equals(dto.confirmarNovaPassword())) {
            throw new RuntimeException();

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


    // ─── Ver próprio perfil ───────────────────────────────────────────────────
    public UtilizadorResponseDto verMeuPerfil(String email) {
        Utilizadore utilizador = utilizadoreRepository.findByEmail(email)
                .orElseThrow(() -> new UtilizadorNaoEncontradoException(email));
        return toResponseDTO(utilizador);
    }

    // ─── Mapper: Entity → DTO ─────────────────────────────────────────────────
    private UtilizadorResponseDto toResponseDTO(Utilizadore u) {
        return new UtilizadorResponseDto(
                idHasher.encode(u.getId()),
                u.getNome(),
                u.getEmail(),
                u.getTelefone(),
                u.getTipo().getTipoUtilizador(),
                u.getAtivo(),
                u.getDataNascimento(),
                u.getCriadoEm()
        );
    }

    private TipoUtilizadorDto FindTipoById(String id) {
        TipoUtilizador tipo = tipoUtilizadorRepository
                .findById(idHasher.decode(id))
                .orElseThrow(() -> new RuntimeException("Tipo de utilizador não encontrado"));
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
            throw new RuntimeException("O token expirou!");
        }

        // 3. O BCrypt NÃO permite buscar por "token" direto se for hash.
        // Você deve buscar o registro e usar o checkpw:

        if (!BCrypt.checkpw(dto.token(), recuperacao.getToken())) {
            throw new RuntimeException("Token incorreto!");
        }

        // 4. Se chegou aqui, é válido! Atualizar a senha do utilizador
        Utilizadore user = recuperacao.getIdUtilizador();
        user.setPalavraPasse(passwordEncoder.encode(dto.novaPassword()));
        utilizadoreRepository.save(user);

        // 5. Apagar o token para não ser usado de novo
        tokenRecuperacaoRepository.delete(recuperacao);
    }

}