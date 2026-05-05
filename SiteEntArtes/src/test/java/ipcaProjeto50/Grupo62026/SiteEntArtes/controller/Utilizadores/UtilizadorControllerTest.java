package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Utilizadores;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.Utils;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.EncarregadoAlunoService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.UtilizadorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilizadorControllerTest {

    @Mock
    private UtilizadorService utilizadorService;

    @Mock
    private EncarregadoAlunoService encarregadoAlunoService;

    @InjectMocks
    private UtilizadorController utilizadorController;

    private UtilizadorResponseDto utilizadorDto;

    @BeforeEach
    void setUp() {
        utilizadorDto = new UtilizadorResponseDto(
                "hash-123",
                "Teste Utilizador",
                "teste@email.pt",
                "123456789",
                "912345678",
                "ROLE_ALUNO",
                true,
                LocalDate.of(2000, 1, 1),
                LocalDateTime.now()
        );
    }

    @Test
    void geraTokenEmail_retorna200_quandoSucesso() throws Exception {
        when(utilizadorService.geraToken(anyString())).thenReturn(any());

        ResponseEntity<?> response = utilizadorController.geraTokenEmail("teste@email.pt");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Token enviado para o e-mail", response.getBody());
    }

    @Test
    void esqueceuPassword_retorna200_quandoSucesso() throws Exception {
        // Utilizando o DTO conforme a tua estrutura de record
        AlterarPasswordSemLoginDto dto = new AlterarPasswordSemLoginDto(
                "pass123",
                "pass123",
                "token-valid",
                "teste@email.pt"
        );

        doNothing().when(utilizadorService).atualizaPassSemLogin(any(AlterarPasswordSemLoginDto.class));

        ResponseEntity<?> response = utilizadorController.esqueceuPassword(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Palavra-passe alterada com sucesso!", response.getBody());
    }

    @Test
    void listarTodos_retorna200_comPagina() throws Exception {
        Page<UtilizadorResponseDto> page = new PageImpl<>(List.of(utilizadorDto));
        when(utilizadorService.listarTodos(any(), any())).thenReturn(page);

        ResponseEntity<Page<UtilizadorResponseDto>> response = utilizadorController.listarTodos("ROLE_ALUNO", PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void verDetalhe_retorna200_quandoExiste() throws Exception {
        when(utilizadorService.verDetalhe("hash-123")).thenReturn(utilizadorDto);

        ResponseEntity<UtilizadorResponseDto> response = utilizadorController.verDetalhe("hash-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void verMeuPerfil_retorna200_quandoLogado() throws Exception {
        try (MockedStatic<Utils> utilsMock = mockStatic(Utils.class)) {
            utilsMock.when(Utils::getAuthenticatedUserId).thenReturn("hash-123");
            when(utilizadorService.verMeuPerfil("hash-123")).thenReturn(utilizadorDto);

            ResponseEntity<UtilizadorResponseDto> response = utilizadorController.verMeuPerfil();

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Test
    void criarUtilizador_retorna201_quandoSucesso() throws Exception {
        CriarUtilizadorDto dto = mock(CriarUtilizadorDto.class);
        when(utilizadorService.criarUtilizador(any())).thenReturn(utilizadorDto);

        ResponseEntity<UtilizadorResponseDto> response = utilizadorController.criarUtilizador(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void toggleAtivo_retorna200_quandoSucesso() throws Exception {
        when(utilizadorService.toggleAtivo("hash-123")).thenReturn(utilizadorDto);

        ResponseEntity<UtilizadorResponseDto> response = utilizadorController.toggleAtivo("hash-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void adicionarEducando_retorna200_quandoSucesso() throws Exception {
        doNothing().when(encarregadoAlunoService).adicionarEducando("enc-1", "alu-1");

        ResponseEntity<Void> response = utilizadorController.adicionarEducando("enc-1", "alu-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void associarAlunoAEncarregado_retorna200_quandoSucesso() throws Exception {
        doNothing().when(utilizadorService).associarAlunoAEncarregado("alu-1", "enc-1");

        ResponseEntity<?> response = utilizadorController.associarAlunoAEncarregado("alu-1", "enc-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Associação criada com sucesso.", response.getBody());
    }
}