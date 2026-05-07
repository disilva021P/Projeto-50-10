package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Faltas;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.Utils;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.CancelamentoService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JustificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelamentoControllerTest {

    @Mock private CancelamentoService cancelamentoService;
    @Mock private JustificacaoService justificacaoService;

    @InjectMocks
    private CancelamentoController cancelamentoController;

    private FaltaDto faltaDto;
    private FaltaResumoDto resumoDto;

    @BeforeEach
    void setUp() {
        faltaDto = new FaltaDto("hash1", "aula1", "user1", false, null, "PENDENTE");
        resumoDto = new FaltaResumoDto(10, 3, 4, 3);
    }

    // ─── marcar ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /marcar com dados válidos deve retornar 201 com FaltaDto")
    void marcar_comDadosValidos_retorna201() throws Exception {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("prof@escola.com");
            when(cancelamentoService.marcarFalta(any(FaltaDto.class), eq("prof@escola.com")))
                    .thenReturn(faltaDto);

            ResponseEntity<?> response = cancelamentoController.marcar(faltaDto);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(faltaDto, response.getBody());
        }
    }

    @Test
    @DisplayName("POST /marcar quando service lança excepção deve retornar 400")
    void marcar_quandoServiceLancaExcecao_retorna400() throws Exception {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("prof@escola.com");
            when(cancelamentoService.marcarFalta(any(), any()))
                    .thenThrow(new Exception("Aula não existe"));

            ResponseEntity<?> response = cancelamentoController.marcar(faltaDto);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().toString().contains("Aula não existe"));
        }
    }

// ─── atualizar ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /{id} com dados válidos deve retornar 200 com FaltaDto atualizado")
    void atualizar_comDadosValidos_retorna200() throws Exception {
        when(cancelamentoService.atualizarFalta("hash1", faltaDto)).thenReturn(faltaDto);

        ResponseEntity<?> response = cancelamentoController.atualizar("hash1", faltaDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(faltaDto, response.getBody());
    }

    @Test
    @DisplayName("PUT /{id} com ID inexistente deve retornar 404")
    void atualizar_comIdInexistente_retorna404() throws Exception {
        when(cancelamentoService.atualizarFalta(eq("naoExiste"), any()))
                .thenThrow(new Exception("Falta não encontrada"));

        ResponseEntity<?> response = cancelamentoController.atualizar("naoExiste", faltaDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Falta não encontrada"));
    }

// ─── remover ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /{id} com ID válido deve retornar 204")
    void remover_comIdValido_retorna204() throws Exception {
        doNothing().when(justificacaoService).removerJustificacao("hash1");
        doNothing().when(cancelamentoService).removerFalta("hash1");

        ResponseEntity<?> response = cancelamentoController.remover("hash1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(justificacaoService).removerJustificacao("hash1");
        verify(cancelamentoService).removerFalta("hash1");
    }

    @Test
    @DisplayName("DELETE /{id} quando service lança excepção deve retornar 500")
    void remover_quandoErro_retorna500() throws Exception {
        doNothing().when(justificacaoService).removerJustificacao("hash1");
        doThrow(new Exception("Erro ao remover"))
                .when(cancelamentoService).removerFalta("hash1");

        ResponseEntity<?> response = cancelamentoController.remover("hash1");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Erro ao remover"));
    }

// ─── validar ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /{id}/validar aprovada=true deve retornar 200")
    void validar_aprovada_retorna200() throws Exception {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("coord@escola.com");
            doNothing().when(justificacaoService).validarFalta("hash1", true, "coord@escola.com");

            ResponseEntity<?> response = cancelamentoController.validar("hash1", true);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(justificacaoService).validarFalta("hash1", true, "coord@escola.com");
        }
    }

    @Test
    @DisplayName("PATCH /{id}/validar quando service lança excepção deve retornar 400")
    void validar_quandoErro_retorna400() throws Exception {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("coord@escola.com");
            doThrow(new Exception("Falta já validada"))
                    .when(justificacaoService).validarFalta(any(), anyBoolean(), any());

            ResponseEntity<?> response = cancelamentoController.validar("hash1", true);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().toString().contains("Falta já validada"));
        }
    }

    // ─── listarMinhasFaltas ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /meu-perfil/detalhe deve retornar 200 com lista de faltas")
    void listarMinhasFaltas_retorna200ComLista() {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("aluno@escola.com");
            when(cancelamentoService.listarFaltasPorUtilizador("aluno@escola.com"))
                    .thenReturn(List.of(faltaDto));

            ResponseEntity<List<FaltaDto>> response = cancelamentoController.listarMinhasFaltas();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
        }
    }

    @Test
    @DisplayName("GET /meu-perfil/detalhe com lista vazia deve retornar 200")
    void listarMinhasFaltas_listaVazia_retorna200() {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("aluno@escola.com");
            when(cancelamentoService.listarFaltasPorUtilizador("aluno@escola.com"))
                    .thenReturn(List.of());

            ResponseEntity<List<FaltaDto>> response = cancelamentoController.listarMinhasFaltas();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isEmpty());
        }
    }

    // ─── obterMinhasEstatisticas ──────────────────────────────────────────────

    @Test
    @DisplayName("GET /meu-perfil/estatisticas deve retornar 200 com resumo")
    void obterMinhasEstatisticas_retorna200ComResumo() {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("aluno@escola.com");
            when(cancelamentoService.obterResumoEstatisticas("aluno@escola.com"))
                    .thenReturn(resumoDto);

            ResponseEntity<FaltaResumoDto> response = cancelamentoController.obterMinhasEstatisticas();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(10, response.getBody().total());
            assertEquals(3, response.getBody().justificadas());
            assertEquals(4, response.getBody().pendentes());
            assertEquals(3, response.getBody().injustificadas());
        }
    }

    // ─── obterEstatisticasDosEducandos ────────────────────────────────────────

    @Test
    @DisplayName("GET /encarregado/educandos/estatisticas deve retornar 200 com resumo")
    void obterEstatisticasDosEducandos_retorna200() {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("enc@escola.com");
            when(cancelamentoService.obterResumoEstatisticasEducandos("enc@escola.com"))
                    .thenReturn(resumoDto);

            ResponseEntity<FaltaResumoDto> response = cancelamentoController.obterEstatisticasDosEducandos();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    // ─── listarFaltasDaMinhasAula ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /professor/{aulaId}/faltas deve retornar 200 com lista")
    void listarFaltasDaMinhasAula_retorna200() {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("prof@escola.com");
            when(cancelamentoService.listarFaltasPorProfessorAula("prof@escola.com", "aula1"))
                    .thenReturn(List.of(faltaDto));

            ResponseEntity<List<FaltaDto>> response =
                    cancelamentoController.listarFaltasDaMinhasAula("aula1");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
        }
    }

    // ─── listarFaltasPorUtilizador (coordenação) ──────────────────────────────

    @Test
    @DisplayName("GET /utilizador/{idHash}/detalhe deve retornar 200 com lista")
    void listarFaltasPorUtilizador_retorna200() {
        when(cancelamentoService.listarFaltasPorUtilizador("hash1"))
                .thenReturn(List.of(faltaDto));

        ResponseEntity<List<FaltaDto>> response =
                cancelamentoController.listarFaltasPorUtilizador("hash1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // ─── listarTodas ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /faltas deve retornar 200 com todas as faltas")
    void listarTodas_retorna200() {
        when(cancelamentoService.listarTodas()).thenReturn(List.of(faltaDto, faltaDto));

        ResponseEntity<List<FaltaDto>> response = cancelamentoController.listarTodas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    // ─── listarPendentes ──────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /pendentes deve retornar 200 com faltas pendentes")
    void listarPendentes_retorna200() {
        when(cancelamentoService.listarPendentes()).thenReturn(List.of(faltaDto));

        ResponseEntity<List<FaltaDto>> response = cancelamentoController.listarPendentes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // ─── listarFaltasDosMeusEducandos ─────────────────────────────────────────

    @Test
    @DisplayName("GET /encarregado/educandos/faltas deve retornar 200 com lista")
    void listarFaltasDosMeusEducandos_retorna200() {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("enc@escola.com");
            when(cancelamentoService.listarFaltasDosEducandos("enc@escola.com"))
                    .thenReturn(List.of(faltaDto));

            ResponseEntity<List<FaltaDto>> response =
                    cancelamentoController.listarFaltasDosMeusEducandos();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
        }
    }

    // ─── submeterJustificacao ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST /{id}/justificar com PDF válido deve retornar 200")
    void submeterJustificacao_comPdfValido_retorna200() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile(
                "pdf", "justificacao.pdf", "application/pdf", new byte[]{1, 2, 3}
        );
        doNothing().when(justificacaoService)
                .submeterJustificacao("hash1", pdf.getBytes(), "Doença");

        ResponseEntity<?> response =
                cancelamentoController.submeterJustificacao("hash1", pdf, "Doença");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Justificação submetida com sucesso.", response.getBody());
    }

    @Test
    @DisplayName("POST /{id}/justificar quando service lança excepção deve retornar 400")
    void submeterJustificacao_quandoErro_retorna400() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile(
                "pdf", "justificacao.pdf", "application/pdf", new byte[]{1, 2, 3}
        );
        doThrow(new Exception("Falta já justificada"))
                .when(justificacaoService).submeterJustificacao(any(), any(), any());

        ResponseEntity<?> response =
                cancelamentoController.submeterJustificacao("hash1", pdf, "Doença");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Falta já justificada"));
    }

    // ─── verPdf ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /{id}/pdf deve retornar 200 com bytes do PDF")
    void verPdf_comIdValido_retorna200ComBytes() {
        byte[] pdfBytes = {10, 20, 30};
        when(justificacaoService.verConteudoPdf("hash1")).thenReturn(pdfBytes);

        ResponseEntity<byte[]> response = cancelamentoController.verPdf("hash1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(pdfBytes, response.getBody());
    }

    @Test
    @DisplayName("GET /{id}/pdf com ID inexistente deve retornar 404")
    void verPdf_comIdInexistente_retorna404() {
        when(justificacaoService.verConteudoPdf("naoExiste"))
                .thenThrow(new RuntimeException("PDF não encontrado"));

        ResponseEntity<byte[]> response = cancelamentoController.verPdf("naoExiste");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}