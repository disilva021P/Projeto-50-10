package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.exception.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilizadorService {

    private final UtilizadoreRepository utilizadoreRepository;
    private final EncarregadoAlunoRepository encarregadoAluno;
    private final TipoUtilizadorRepository tipoUtilizadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdHasher idHasher;

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
    public UtilizadorResponseDto verDetalhe(Integer id) {
        Utilizadore utilizador = utilizadoreRepository.findById(id)
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

    public void alterarPalavraPasse(String email, AlterarPasswordDto dto) {

        Utilizadore utilizador = utilizadoreRepository.findByEmail(email)
                .orElseThrow(() -> new UtilizadorNaoEncontradoException(email));

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
}