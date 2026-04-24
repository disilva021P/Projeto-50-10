package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.exception.UtilizadorNaoEncontradoException;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilizadorServiceDetalheTest {

    @InjectMocks
    private UtilizadorService service;

    @Mock
    private UtilizadoreRepository repository;

    @Mock
    private IdHasher idHasher;

    @Test
    void deveRetornarUtilizador() {
        when(idHasher.decode("abc")).thenReturn(1);

        Utilizadore u = new Utilizadore();
        u.setId(1);

        when(repository.findById(1)).thenReturn(Optional.of(u));
        when(idHasher.encode(anyInt())).thenReturn("hashed");

        assertNotNull(service.verDetalhe("abc"));
    }

    @Test
    void deveLancarErroSeNaoExiste() {
        when(idHasher.decode("abc")).thenReturn(1);
        when(repository.findById(1)).thenReturn(Optional.empty());

        assertThrows(UtilizadorNaoEncontradoException.class,
                () -> service.verDetalhe("abc"));
    }
}