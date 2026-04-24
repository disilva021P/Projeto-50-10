package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadorResponseDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilizadorServiceListarTest {

    @InjectMocks
    private UtilizadorService service;

    @Mock
    private UtilizadoreRepository utilizadoreRepository;

    @Mock
    private IdHasher idHasher;

    @Test
    void deveListarTodosSemFiltro() {
        Pageable pageable = PageRequest.of(0, 10);

        Utilizadore u = new Utilizadore();
        u.setId(1);
        u.setNome("Teste");

        Page<Utilizadore> page = new PageImpl<>(List.of(u));

        when(utilizadoreRepository.findAll(pageable)).thenReturn(page);
        when(idHasher.encode(anyInt())).thenReturn("hashed");

        Page<UtilizadorResponseDto> result = service.listarTodos(null, pageable);

        assertEquals(1, result.getContent().size());
    }
}