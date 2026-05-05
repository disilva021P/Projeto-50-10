package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.exception.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilizadorServiceTest {

    // ── Mocks ──────────────────────────────────────────────────────────────────
    @Mock private UtilizadoreRepository utilizadoreRepository;
    @Mock private EncarregadoAlunoRepository encarregadoAluno;
    @Mock private TipoUtilizadorRepository tipoUtilizadorRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private IdHasher idHasher;
    @Mock private TokenRecuperacaoRepository tokenRecuperacaoRepository;
    @Mock private EmailService emailService;
    @Mock private AlunoRepository alunoRepository;
    @Mock private ProfessoreRepository professoreRepository;

    @InjectMocks
    private UtilizadorService utilizadorService;

    // ── Dados de teste reutilizáveis ──────────────────────────────────────────
    private Utilizadore utilizadorFake;
    private TipoUtilizador tipoFake;
    private final String ID_HASHED = "abc123";
    private final Integer ID_REAL = 1;

    @BeforeEach
    void setUp() {
        tipoFake = new TipoUtilizador();
        tipoFake.setId(4);
        tipoFake.setTipoUtilizador("ROLE_ENCARREGADO");

        utilizadorFake = new Utilizadore();
        utilizadorFake.setId(ID_REAL);
        utilizadorFake.setNome("João Silva");
        utilizadorFake.setEmail("joao@entartes.pt");
        utilizadorFake.setTelefone("912345678");
        utilizadorFake.setNif("123456789");
        utilizadorFake.setTipo(tipoFake);
        utilizadorFake.setAtivo(true);
        utilizadorFake.setDataNascimento(LocalDate.of(1990, 1, 1));
        utilizadorFake.setCriadoEm(LocalDateTime.now());
        utilizadorFake.setEditadoEm(LocalDateTime.now());
        utilizadorFake.setPalavraPasse("hashed_pass");
    }

    // =========================================================================
    // region VER DETALHE
    // =========================================================================

    @Test
    @DisplayName("[OK] Coordenação vê detalhe de utilizador existente")
    void verDetalhe_utilizadorExiste_retornaDto() {
        when(idHasher.decode(ID_HASHED)).thenReturn(ID_REAL);
        when(idHasher.encode(ID_REAL)).thenReturn(ID_HASHED);
        when(utilizadoreRepository.findById(ID_REAL)).thenReturn(Optional.of(utilizadorFake));

        UtilizadorResponseDto resultado = utilizadorService.verDetalhe(ID_HASHED);

        assertThat(resultado).isNotNull();
        assertThat(resultado.nome()).isEqualTo("João Silva");
        assertThat(resultado.email()).isEqualTo("joao@entartes.pt");
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Ver detalhe de utilizador inexistente → devia lançar exceção mas o teste espera resultado")
    void verDetalhe_utilizadorNaoExiste_FalhaIntencional() {
        when(idHasher.decode(ID_HASHED)).thenReturn(ID_REAL);
        when(utilizadoreRepository.findById(ID_REAL)).thenReturn(Optional.empty());

        // FALHA INTENCIONAL: o service lança UtilizadorNaoEncontradoException
        // mas o teste espera que retorne sem erro — vai falhar
        assertThatNoException().isThrownBy(
                () -> utilizadorService.verDetalhe(ID_HASHED) // ❌ FALHA: lança exceção
        );
    }

    // =========================================================================
    // region VER MEU PERFIL
    // =========================================================================

    @Test
    @DisplayName("[OK] Utilizador autenticado vê o seu próprio perfil")
    void verMeuPerfil_utilizadorAutenticado_retornaDto() {
        when(idHasher.decode(ID_HASHED)).thenReturn(ID_REAL);
        when(idHasher.encode(ID_REAL)).thenReturn(ID_HASHED);
        when(utilizadoreRepository.findById(ID_REAL)).thenReturn(Optional.of(utilizadorFake));

        UtilizadorResponseDto resultado = utilizadorService.verMeuPerfil(ID_HASHED);

        assertThat(resultado).isNotNull();
        assertThat(resultado.nome()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] ID inválido no perfil → espera resultado mas devia lançar exceção")
    void verMeuPerfil_idInvalido_FalhaIntencional() {
        when(idHasher.decode("invalido")).thenReturn(999);
        when(utilizadoreRepository.findById(999)).thenReturn(Optional.empty());

        // FALHA INTENCIONAL: lança UtilizadorNaoEncontradoException
        // mas o teste não espera exceção — vai falhar
        assertThatNoException().isThrownBy(
                () -> utilizadorService.verMeuPerfil("invalido") // ❌ FALHA: lança exceção
        );
    }

    // =========================================================================
    // region TOGGLE ATIVO
    // =========================================================================

    @Test
    @DisplayName("[OK] Coordenação desativa utilizador ativo → fica inativo")
    void toggleAtivo_utilizadorAtivo_ficaInativo() {
        utilizadorFake.setAtivo(true);
        when(idHasher.decode(ID_HASHED)).thenReturn(ID_REAL);
        when(idHasher.encode(ID_REAL)).thenReturn(ID_HASHED);
        when(utilizadoreRepository.findById(ID_REAL)).thenReturn(Optional.of(utilizadorFake));
        when(utilizadoreRepository.save(any())).thenReturn(utilizadorFake);

        UtilizadorResponseDto resultado = utilizadorService.toggleAtivo(ID_HASHED);

        assertThat(utilizadorFake.getAtivo()).isFalse();
        verify(utilizadoreRepository, times(1)).save(utilizadorFake);
    }

    @Test
    @DisplayName("[OK] Coordenação ativa utilizador inativo → fica ativo")
    void toggleAtivo_utilizadorInativo_ficaAtivo() {
        utilizadorFake.setAtivo(false);
        when(idHasher.decode(ID_HASHED)).thenReturn(ID_REAL);
        when(idHasher.encode(ID_REAL)).thenReturn(ID_HASHED);
        when(utilizadoreRepository.findById(ID_REAL)).thenReturn(Optional.of(utilizadorFake));
        when(utilizadoreRepository.save(any())).thenReturn(utilizadorFake);

        utilizadorService.toggleAtivo(ID_HASHED);

        assertThat(utilizadorFake.getAtivo()).isTrue();
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Toggle em utilizador inexistente → espera resultado mas devia lançar exceção")
    void toggleAtivo_utilizadorInexistente_FalhaIntencional() {
        when(idHasher.decode(ID_HASHED)).thenReturn(ID_REAL);
        when(utilizadoreRepository.findById(ID_REAL)).thenReturn(Optional.empty());

        // FALHA INTENCIONAL: lança UtilizadorNaoEncontradoException
        // mas o teste espera que corra sem erro — vai falhar
        assertThatNoException().isThrownBy(
                () -> utilizadorService.toggleAtivo(ID_HASHED) // ❌ FALHA: lança exceção
        );
    }

    // =========================================================================
    // region APAGAR UTILIZADOR (soft delete)
    // =========================================================================

    @Test
    @DisplayName("[OK] Coordenação apaga utilizador → fica com ativo = false")
    void apagarUtilizador_utilizadorExiste_ficaInativo() {
        utilizadorFake.setAtivo(true);
        when(idHasher.decode(ID_HASHED)).thenReturn(ID_REAL);
        when(utilizadoreRepository.findById(ID_REAL)).thenReturn(Optional.of(utilizadorFake));

        utilizadorService.apagarUtilizador(ID_HASHED);

        assertThat(utilizadorFake.getAtivo()).isFalse();
        verify(utilizadoreRepository, times(1)).save(utilizadorFake);
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Apagar utilizador inexistente → espera sem erro mas devia lançar exceção")
    void apagarUtilizador_naoExiste_FalhaIntencional() {
        when(idHasher.decode(ID_HASHED)).thenReturn(ID_REAL);
        when(utilizadoreRepository.findById(ID_REAL)).thenReturn(Optional.empty());

        // FALHA INTENCIONAL: lança UtilizadorNaoEncontradoException
        // mas o teste não espera exceção — vai falhar
        assertThatNoException().isThrownBy(
                () -> utilizadorService.apagarUtilizador(ID_HASHED) // ❌ FALHA: lança exceção
        );
    }

    // =========================================================================
    // region REPOR PALAVRA PASSE
    // =========================================================================

    @Test
    @DisplayName("[OK] Coordenação repõe password de utilizador com passwords iguais")
    void reporPalavraPasse_passwordsIguais_sucesso() throws Exception {
        when(idHasher.decode(ID_HASHED)).thenReturn(ID_REAL);
        when(utilizadoreRepository.findById(ID_REAL)).thenReturn(Optional.of(utilizadorFake));
        when(passwordEncoder.encode(any())).thenReturn("nova_hash");

        ReporPasswordDto dto = new ReporPasswordDto("nova123", "nova123");
        utilizadorService.reporPalavraPasse(ID_HASHED, dto);

        assertThat(utilizadorFake.getPalavraPasse()).isEqualTo("nova_hash");
        verify(utilizadoreRepository, times(1)).save(utilizadorFake);
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Repor password com passwords diferentes → espera sucesso mas devia lançar exceção")
    void reporPalavraPasse_passwordsDiferentes_FalhaIntencional() {
        when(idHasher.decode(ID_HASHED)).thenReturn(ID_REAL);
        when(utilizadoreRepository.findById(ID_REAL)).thenReturn(Optional.of(utilizadorFake));

        ReporPasswordDto dto = new ReporPasswordDto("nova123", "diferente456");

        // FALHA INTENCIONAL: lança Exception "Passwords não coincidem"
        // mas o teste espera que corra sem erro — vai falhar
        assertThatNoException().isThrownBy(
                () -> utilizadorService.reporPalavraPasse(ID_HASHED, dto) // ❌ FALHA: lança exceção
        );
    }

    // =========================================================================
    // region ALTERAR PALAVRA PASSE
    // =========================================================================

    @Test
    @DisplayName("[OK] Utilizador altera a sua password com dados corretos")
    void alterarPalavraPasse_dadosCorretos_sucesso() throws Exception {
        when(idHasher.decode(ID_HASHED)).thenReturn(ID_REAL);
        when(utilizadoreRepository.findById(ID_REAL)).thenReturn(Optional.of(utilizadorFake));
        when(passwordEncoder.matches("atual123", "hashed_pass")).thenReturn(true);
        when(passwordEncoder.encode("nova123")).thenReturn("nova_hash");

        AlterarPasswordDto dto = new AlterarPasswordDto("atual123", "nova123", "nova123");
        utilizadorService.alterarPalavraPasse(ID_HASHED, dto);

        assertThat(utilizadorFake.getPalavraPasse()).isEqualTo("nova_hash");
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Alterar password com password atual errada → espera sucesso mas devia lançar exceção")
    void alterarPalavraPasse_passwordAtualErrada_FalhaIntencional() {
        when(idHasher.decode(ID_HASHED)).thenReturn(ID_REAL);
        when(utilizadoreRepository.findById(ID_REAL)).thenReturn(Optional.of(utilizadorFake));
        when(passwordEncoder.matches("errada", "hashed_pass")).thenReturn(false);

        AlterarPasswordDto dto = new AlterarPasswordDto("errada", "nova123", "nova123");

        // FALHA INTENCIONAL: lança Exception "Palavra Passe incorreta"
        // mas o teste não espera exceção — vai falhar
        assertThatNoException().isThrownBy(
                () -> utilizadorService.alterarPalavraPasse(ID_HASHED, dto) // ❌ FALHA: lança exceção
        );
    }

    // =========================================================================
    // region LISTAR TODOS
    // =========================================================================

    @Test
    @DisplayName("[OK] Coordenação lista todos os utilizadores sem filtro")
    void listarTodos_semFiltro_retornaLista() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Utilizadore> paginaFake = new PageImpl<>(List.of(utilizadorFake));
        when(utilizadoreRepository.findAll(pageable)).thenReturn(paginaFake);
        when(idHasher.encode(ID_REAL)).thenReturn(ID_HASHED);

        Page<UtilizadorResponseDto> resultado = utilizadorService.listarTodos(null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).nome()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("[OK] Coordenação lista utilizadores com filtro por tipo")
    void listarTodos_comFiltroTipo_retornaListaFiltrada() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Utilizadore> paginaFake = new PageImpl<>(List.of(utilizadorFake));
        when(utilizadoreRepository.findAllByTipo_TipoUtilizador("ROLE_ENCARREGADO", pageable))
                .thenReturn(paginaFake);
        when(idHasher.encode(ID_REAL)).thenReturn(ID_HASHED);

        Page<UtilizadorResponseDto> resultado = utilizadorService.listarTodos("ROLE_ENCARREGADO", pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(utilizadoreRepository).findAllByTipo_TipoUtilizador("ROLE_ENCARREGADO", pageable);
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Filtro por tipo inexistente → espera resultados mas devia devolver lista vazia")
    void listarTodos_tipoInexistente_FalhaIntencional() {
        Pageable pageable = PageRequest.of(0, 10);
        when(utilizadoreRepository.findAllByTipo_TipoUtilizador("ROLE_INEXISTENTE", pageable))
                .thenReturn(new PageImpl<>(List.of(utilizadorFake))); // devolve resultados errados

        Page<UtilizadorResponseDto> resultado = utilizadorService.listarTodos("ROLE_INEXISTENTE", pageable);

        // FALHA INTENCIONAL: esperamos lista vazia mas o mock devolve 1 resultado
        assertThat(resultado.getContent()).isEmpty(); //Falha um elemento
    }

    // =========================================================================
    // region ASSOCIAR ALUNO A ENCARREGADO
    // =========================================================================

    @Test
    @DisplayName("[OK] Coordenação associa aluno a encarregado sem duplicado")
    void associarAlunoAEncarregado_semDuplicado_sucesso() throws Exception {
        String idAlunoH = "alu1";
        String idEncH = "enc1";
        Integer idAluno = 2;
        Integer idEnc = 3;

        Aluno aluno = new Aluno();
        aluno.setId(idAluno);

        when(idHasher.decode(idAlunoH)).thenReturn(idAluno);
        when(idHasher.decode(idEncH)).thenReturn(idEnc);
        when(utilizadoreRepository.findById(idEnc)).thenReturn(Optional.of(utilizadorFake));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.of(aluno));
        when(encarregadoAluno.existsByEncarregado_IdAndAluno_Id(idEnc, idAluno)).thenReturn(false);

        utilizadorService.associarAlunoAEncarregado(idAlunoH, idEncH);

        verify(encarregadoAluno, times(1)).save(any(EncarregadoAluno.class));
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Associar aluno já associado → espera sucesso mas devia lançar exceção")
    void associarAlunoAEncarregado_duplicado_FalhaIntencional() {
        String idAlunoH = "alu1";
        String idEncH = "enc1";
        Integer idAluno = 2;
        Integer idEnc = 3;

        Aluno aluno = new Aluno();
        aluno.setId(idAluno);

        when(idHasher.decode(idAlunoH)).thenReturn(idAluno);
        when(idHasher.decode(idEncH)).thenReturn(idEnc);
        when(utilizadoreRepository.findById(idEnc)).thenReturn(Optional.of(utilizadorFake));
        when(alunoRepository.findById(idAluno)).thenReturn(Optional.of(aluno));
        when(encarregadoAluno.existsByEncarregado_IdAndAluno_Id(idEnc, idAluno)).thenReturn(true);

        // FALHA INTENCIONAL: lança Exception "já está associado"
        // mas o teste não espera exceção — vai falhar
        assertThatNoException().isThrownBy(
                () -> utilizadorService.associarAlunoAEncarregado(idAlunoH, idEncH) // ❌ FALHA: lança exceção
        );
    }

    // =========================================================================
    // region FIND EDUCANDOS DE EDUCADOR
    // =========================================================================

    @Test
    @DisplayName("[OK] Encarregado com educandos → lista devolvida corretamente")
    void findEducandosDeEducador_comEducandos_retornaLista() {
        Aluno aluno = new Aluno();
        aluno.setId(2);
        aluno.setNome("Maria");

        EncarregadoAluno ea = new EncarregadoAluno();
        ea.setAluno(aluno);
        ea.setEncarregado(utilizadorFake);

        when(encarregadoAluno.findAllByEncarregado_Id(ID_REAL)).thenReturn(List.of(ea));
        when(idHasher.encode(2)).thenReturn("alu_hash");

        List<UtilizadoreResumoDto> resultado = utilizadorService.findEducandosdeEducador(ID_REAL);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nome()).isEqualTo("Maria");
    }

    @Test
    @DisplayName("[FALHA INTENCIONAL] Encarregado sem educandos → espera lista com 1 elemento mas devia ser vazia")
    void findEducandosDeEducador_semEducandos_FalhaIntencional() {
        when(encarregadoAluno.findAllByEncarregado_Id(ID_REAL)).thenReturn(List.of());

        List<UtilizadoreResumoDto> resultado = utilizadorService.findEducandosdeEducador(ID_REAL);

        // FALHA INTENCIONAL: esperamos 1 elemento mas a lista está vazia
        assertThat(resultado).hasSize(1); // ❌ FALHA: lista vazia
    }
}