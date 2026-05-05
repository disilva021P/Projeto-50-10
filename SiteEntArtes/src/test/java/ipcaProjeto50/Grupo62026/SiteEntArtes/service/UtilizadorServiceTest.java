package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.exception.UtilizadorNaoEncontradoException;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilizadorServiceTest {

    @Mock private UtilizadoreRepository utilizadoreRepository;
    @Mock private TipoUtilizadorRepository tipoUtilizadorRepository;
    @Mock private AlunoRepository alunoRepository;
    @Mock private ProfessoreRepository professoreRepository;
    @Mock private EncarregadoAlunoRepository encarregadoAlunoRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private IdHasher idHasher;
    @Mock private EmailService emailService;

    @InjectMocks
    private UtilizadorService utilizadorService;

    private TipoUtilizador tipoAluno;
    private CriarUtilizadorDto criarDto;

    @BeforeEach
    void setUp() {
        tipoAluno = new TipoUtilizador();
        tipoAluno.setId(3);
        tipoAluno.setTipoUtilizador("ROLE_ALUNO");

        // DTO atualizado conforme o teu record
        criarDto = new CriarUtilizadorDto(
                "Carlos Aluno",
                "carlos@email.com",
                "222333444",
                "911222333",
                "hashed_tipo_3",
                LocalDate.of(2010, 5, 15),
                null // palavraPasseTemporaria (gerada no service)
        );
    }

    @Test
    void criarUtilizador_DeveCriarAlunoComSucesso() throws Exception {
        // Arrange
        when(idHasher.decode("hashed_tipo_3")).thenReturn(3);
        when(tipoUtilizadorRepository.findById(3)).thenReturn(Optional.of(tipoAluno));
        when(passwordEncoder.encode(any())).thenReturn("password_encriptada");

        // Simular o comportamento do save para Aluno
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(invocation -> {
            Aluno a = invocation.getArgument(0);
            a.setId(100); // Simula ID gerado pela BD
            return a;
        });

        when(idHasher.encode(100)).thenReturn("hashed_user_100");

        // Act
        UtilizadorResponseDto resultado = utilizadorService.criarUtilizador(criarDto);

        // Assert
        assertNotNull(resultado);
        assertEquals("Carlos Aluno", resultado.nome());
        assertEquals("ROLE_ALUNO", resultado.tipoUtilizador());

        // Verifica se o repositório correto foi chamado
        verify(alunoRepository, times(1)).save(any(Aluno.class));
        // Verifica se o email de boas-vindas foi enviado
        verify(emailService, times(1)).enviaEmail(eq("carlos@email.com"), anyString(), anyString());
    }

    @Test
    void criarUtilizador_DeveLancarExcecao_QuandoTipoNaoExiste() {
        // Arrange
        when(idHasher.decode(anyString())).thenReturn(999);
        when(tipoUtilizadorRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            utilizadorService.criarUtilizador(criarDto);
        });

        assertEquals("Tipo de utilizador não encontrado", exception.getMessage());
    }

    @Test
    void eliminarUtilizador_DeveChamarRepository() {
        // Arrange
        when(idHasher.decode("user_hash")).thenReturn(50);

        // Act
        utilizadorService.eliminaUtilizador("user_hash");

        // Assert
        verify(utilizadoreRepository, times(1)).deleteById(50);
    }

    @Test
    void associarAlunoAEncarregado_DeveLancarErro_SeJaExistir() {
        // Arrange
        when(idHasher.decode("hash_aluno")).thenReturn(1);
        when(idHasher.decode("hash_enc")).thenReturn(2);

        when(utilizadoreRepository.findById(2)).thenReturn(Optional.of(new Utilizadore()));
        when(alunoRepository.findById(1)).thenReturn(Optional.of(new Aluno()));

        // Simula que a associação já existe
        when(encarregadoAlunoRepository.existsByEncarregado_IdAndAluno_Id(2, 1)).thenReturn(true);

        // Act & Assert
        Exception ex = assertThrows(Exception.class, () -> {
            utilizadorService.associarAlunoAEncarregado("hash_aluno", "hash_enc");
        });

        assertTrue(ex.getMessage().contains("já está associado"));
    }

    @Test
    void verDetalhe_DeveLancarException_QuandoIdInexistente() {
        // Arrange
        when(idHasher.decode("nao_existe")).thenReturn(404);
        when(utilizadoreRepository.findById(404)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UtilizadorNaoEncontradoException.class, () -> {
            utilizadorService.verDetalhe("nao_existe");
        });
    }

    // ─── Testes de Edição ─────────────────────────────────────────────────────

    @Test
    void editarUtilizador_DeveAtualizarProfessorComDadosEspecificos() {
        // Arrange
        String hashId = "prof_hash";
        Integer idReal = 2;
        BigDecimal novoValorHora = new BigDecimal("25.50");

        TipoUtilizador tipoProf = new TipoUtilizador();
        tipoProf.setId(2); // ID correspondente a Professor no teu service
        tipoProf.setTipoUtilizador("ROLE_PROFESSOR");

        Professore profExistente = new Professore();
        profExistente.setId(idReal);
        profExistente.setTipo(tipoProf);
        profExistente.setValorHora(new BigDecimal("20.00")); // Valor antigo

        // Utilizando o DTO com BigDecimal e os campos corretos
        EditarUtilizadorDto editDto = new EditarUtilizadorDto(
                "Professor Editado",
                "prof@novo.pt",
                "999999999",
                "960000000",
                LocalDate.of(1980, 1, 1),
                novoValorHora,
                true,
                "Novas Notas de Professor"
        );

        when(idHasher.decode(hashId)).thenReturn(idReal);
        when(utilizadoreRepository.findById(idReal)).thenReturn(Optional.of(profExistente));

        // O service faz um findById no professoreRepository para atualizar dados específicos
        when(professoreRepository.findById(idReal)).thenReturn(Optional.of(profExistente));

        // Simula o save devolvendo o próprio objeto alterado
        when(utilizadoreRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(idHasher.encode(idReal)).thenReturn(hashId);

        // Act
        UtilizadorResponseDto resultado = utilizadorService.editarUtilizador(hashId, editDto);

        // Assert
        assertNotNull(resultado);
        assertEquals("Professor Editado", resultado.nome());
        assertEquals("prof@novo.pt", resultado.email());

        // Verifica se os dados específicos de Professor foram persistidos
        verify(professoreRepository, times(1)).save(any(Professore.class));
        assertEquals(novoValorHora, profExistente.getValorHora());
        assertEquals(true, profExistente.getProfessorExterno());
    }

    // ─── Testes de Palavra-Passe e Segurança ──────────────────────────────────

    @Test
    void reporPalavraPasse_DeveAtualizarComSucesso() throws Exception {
        // Arrange
        String hashId = "user_hash";
        Integer idReal = 10;
        Utilizadore user = new Utilizadore();
        user.setId(idReal);

        ReporPasswordDto reporDto = new ReporPasswordDto("novaPass123", "novaPass123");

        when(idHasher.decode(hashId)).thenReturn(idReal);
        when(utilizadoreRepository.findById(idReal)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("novaPass123")).thenReturn("encoded_pass");

        // Act
        utilizadorService.reporPalavraPasse(hashId, reporDto);

        // Assert
        assertEquals("encoded_pass", user.getPalavraPasse());
        verify(utilizadoreRepository).save(user);
    }

    @Test
    void reporPalavraPasse_DeveFalhar_QuandoPasswordsNaoCoincidem() {
        // Arrange
        ReporPasswordDto reporDto = new ReporPasswordDto("pass1", "pass2");
        when(idHasher.decode(anyString())).thenReturn(1);
        when(utilizadoreRepository.findById(anyInt())).thenReturn(Optional.of(new Utilizadore()));

        // Act & Assert
        Exception ex = assertThrows(Exception.class, () ->
                utilizadorService.reporPalavraPasse("hash", reporDto)
        );
        assertTrue(ex.getMessage().contains("não coincidem"));
    }

    // ─── Testes de Listagem de Contactos ──────────────────────────────────────

    @Test
    void listarContactosDisponiveis_DeveFiltrarOProprioUtilizador() {
        // Arrange
        String hashLogado = "hash_eu";
        Integer idLogado = 1;

        Utilizadore eu = new Utilizadore(); eu.setId(idLogado); eu.setNome("Eu");
        Utilizadore outro = new Utilizadore(); outro.setId(2); outro.setNome("Outro");

        when(idHasher.decode(hashLogado)).thenReturn(idLogado);
        when(utilizadoreRepository.findAll()).thenReturn(List.of(eu, outro));
        when(idHasher.encode(2)).thenReturn("hash_outro");

        // Act
        List<UtilizadoreResumoDto> resultado = utilizadorService.listarContactosDisponiveis(hashLogado);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Outro", resultado.get(0).nome());
        assertNotEquals("hash_eu", resultado.get(0).id());
    }
}