package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.CriarUtilizadorDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TipoUtilizador;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilizadorServiceCriarTest {

    @InjectMocks
    private UtilizadorService service;

    @Mock private UtilizadoreRepository utilizadoreRepository;
    @Mock private TipoUtilizadorRepository tipoUtilizadorRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private IdHasher idHasher;
    @Mock private EmailService emailService;
    @Mock private AlunoRepository alunoRepository;
    @Mock private ProfessoreRepository professoreRepository;

    @Test
    void deveCriarAluno() throws Exception {
        CriarUtilizadorDto dto = mock(CriarUtilizadorDto.class);

        when(dto.id_tipoUtilizador()).thenReturn("hash");
        when(idHasher.decode("hash")).thenReturn(3);

        TipoUtilizador tipo = new TipoUtilizador();
        tipo.setId(3);

        when(tipoUtilizadorRepository.findById(3)).thenReturn(Optional.of(tipo));
        when(passwordEncoder.encode(any())).thenReturn("pass");

        service.criarUtilizador(dto);

        verify(alunoRepository).save(any());
        verify(emailService).enviaEmail(any(), any(), any());
    }

    @Test
    void deveFalharSeTipoNaoExiste() {
        CriarUtilizadorDto dto = mock(CriarUtilizadorDto.class);

        when(dto.id_tipoUtilizador()).thenReturn("x");
        when(idHasher.decode("x")).thenReturn(99);
        when(tipoUtilizadorRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> service.criarUtilizador(dto));
    }
}