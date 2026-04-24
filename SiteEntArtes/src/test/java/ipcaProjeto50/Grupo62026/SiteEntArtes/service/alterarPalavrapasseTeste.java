package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AlterarPasswordDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
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
class UtilizadorServicePasswordTest {

    @InjectMocks
    private UtilizadorService service;

    @Mock private UtilizadoreRepository repo;
    @Mock private PasswordEncoder encoder;
    @Mock private IdHasher idHasher;

    @Test
    void deveAlterarPassword() throws Exception {
        when(idHasher.decode("abc")).thenReturn(1);

        Utilizadore u = new Utilizadore();
        u.setPalavraPasse("old");

        when(repo.findById(1)).thenReturn(Optional.of(u));
        when(encoder.matches("oldPass", "old")).thenReturn(true);
        when(encoder.encode(any())).thenReturn("new");

        AlterarPasswordDto dto = mock(AlterarPasswordDto.class);
        when(dto.passwordAtual()).thenReturn("oldPass");
        when(dto.novaPassword()).thenReturn("123");
        when(dto.confirmarNovaPassword()).thenReturn("123");

        service.alterarPalavraPasse("abc", dto);

        verify(repo).save(u);
    }

    @Test
    void deveFalharPasswordErrada() {
        when(idHasher.decode("abc")).thenReturn(1);

        Utilizadore u = new Utilizadore();

        when(repo.findById(1)).thenReturn(Optional.of(u));
        when(encoder.matches(any(), any())).thenReturn(false);

        AlterarPasswordDto dto = mock(AlterarPasswordDto.class);

        assertThrows(Exception.class,
                () -> service.alterarPalavraPasse("abc", dto));
    }
}