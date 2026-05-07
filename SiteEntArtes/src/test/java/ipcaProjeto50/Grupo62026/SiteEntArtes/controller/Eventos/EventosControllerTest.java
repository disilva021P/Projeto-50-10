package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Eventos;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.Utils;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.CriarEventosDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.EventoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ParticipanteDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.EventoService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventosControllerTest {

    @Mock private EventoService eventoService;

    @InjectMocks private EventosController eventosController;

    private EventoDto eventoDto;
    private CriarEventosDto criarDto;

    @BeforeEach
    void setUp() {
        eventoDto = new EventoDto(
                "hashEvento",
                "Workshop de Arte",
                "Descrição do workshop",
                LocalDate.now().plusDays(5),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                "Sala A",
                "5",
                "20",
                new UtilizadoreResumoDto("hashCriador", "Coordenador")
        );

        criarDto = new CriarEventosDto(
                "Workshop de Arte", "Descrição",
                LocalDate.now().plusDays(5),
                LocalTime.of(9, 0), LocalTime.of(11, 0),
                "Sala A", BigDecimal.ZERO, 20, null
        );
    }

    // ─── listarEventosFuturos ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /eventos deve retornar 200 com lista de eventos futuros")
    void listarEventosFuturos_retorna200ComLista() {
        when(eventoService.findEventosFuturos()).thenReturn(List.of(eventoDto));

        ResponseEntity<List<EventoDto>> response = eventosController.listarEventosFuturos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Workshop de Arte", response.getBody().get(0).nome());
    }

    @Test
    @DisplayName("GET /eventos deve retornar 200 com lista vazia quando não há eventos")
    void listarEventosFuturos_listaVazia_retorna200() {
        when(eventoService.findEventosFuturos()).thenReturn(List.of());

        ResponseEntity<List<EventoDto>> response = eventosController.listarEventosFuturos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ─── getEvento ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /eventos/{id} deve retornar 200 com evento")
    void getEvento_existente_retorna200() throws Exception {
        when(eventoService.findById("hashEvento")).thenReturn(eventoDto);

        ResponseEntity<EventoDto> response = eventosController.getEvento("hashEvento");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("hashEvento", response.getBody().id());
    }

    @Test
    @DisplayName("GET /eventos/{id} deve retornar 404 quando não existe")
    void getEvento_inexistente_retorna404() throws Exception {
        when(eventoService.findById("naoExiste")).thenThrow(new Exception("Não encontrado"));

        ResponseEntity<EventoDto> response = eventosController.getEvento("naoExiste");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── criarEvento ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /eventos deve retornar 201 com evento criado")
    void criarEvento_comDadosValidos_retorna201() throws Exception {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("hashCoordenador");
            when(eventoService.criarEvento("hashCoordenador", criarDto)).thenReturn(eventoDto);

            ResponseEntity<EventoDto> response = eventosController.criarEvento(criarDto);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals("Workshop de Arte", response.getBody().nome());
        }
    }

    @Test
    @DisplayName("POST /eventos deve retornar 400 quando service lança excepção")
    void criarEvento_quandoErro_retorna400() throws Exception {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class)) {
            utils.when(Utils::getAuthenticatedUserId).thenReturn("hashCoordenador");
            when(eventoService.criarEvento(any(), any())).thenThrow(new Exception("Erro"));

            ResponseEntity<EventoDto> response = eventosController.criarEvento(criarDto);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    // ─── editarEvento ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /eventos/{id} deve retornar 200 com evento actualizado")
    void editarEvento_comDadosValidos_retorna200() throws Exception {
        when(eventoService.update("hashEvento", criarDto)).thenReturn(eventoDto);

        ResponseEntity<EventoDto> response = eventosController.editarEvento("hashEvento", criarDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("hashEvento", response.getBody().id());
    }

    @Test
    @DisplayName("PUT /eventos/{id} deve retornar 404 quando evento não existe")
    void editarEvento_inexistente_retorna404() throws Exception {
        when(eventoService.update(eq("naoExiste"), any())).thenThrow(new Exception("Não encontrado"));

        ResponseEntity<EventoDto> response = eventosController.editarEvento("naoExiste", criarDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── apagarEvento ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /eventos/{id} deve retornar 204 quando apagado com sucesso")
    void apagarEvento_existente_retorna204() throws Exception {
        doNothing().when(eventoService).delete("hashEvento");

        ResponseEntity<Void> response = eventosController.apagarEvento("hashEvento");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(eventoService).delete("hashEvento");
    }

    @Test
    @DisplayName("DELETE /eventos/{id} deve retornar 404 quando não existe")
    void apagarEvento_inexistente_retorna404() throws Exception {
        doThrow(new Exception("Não encontrado")).when(eventoService).delete("naoExiste");

        ResponseEntity<Void> response = eventosController.apagarEvento("naoExiste");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── adicionarParticipante ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /eventos/{id}/participantes/{utilizadorId} deve retornar 201")
    void adicionarParticipante_valido_retorna201() throws Exception {
        doNothing().when(eventoService).adicionarParticipante("hashEvento", "hashUser");

        ResponseEntity<Void> response = eventosController.adicionarParticipante("hashEvento", "hashUser");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(eventoService).adicionarParticipante("hashEvento", "hashUser");
    }

    @Test
    @DisplayName("POST /eventos/{id}/participantes/{utilizadorId} deve retornar 400 se já inscrito")
    void adicionarParticipante_jaInscrito_retorna400() throws Exception {
        doThrow(new Exception("Já é participante"))
                .when(eventoService).adicionarParticipante("hashEvento", "hashUser");

        ResponseEntity<Void> response = eventosController.adicionarParticipante("hashEvento", "hashUser");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ─── removerParticipante ──────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /eventos/{id}/participantes/{utilizadorId} deve retornar 204")
    void removerParticipante_valido_retorna204() throws Exception {
        doNothing().when(eventoService).removerParticipante("hashEvento", "hashUser");

        ResponseEntity<Void> response = eventosController.removerParticipante("hashEvento", "hashUser");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("DELETE /eventos/{id}/participantes/{utilizadorId} deve retornar 404 se não inscrito")
    void removerParticipante_naoInscrito_retorna404() throws Exception {
        doThrow(new Exception("Não é participante"))
                .when(eventoService).removerParticipante("hashEvento", "hashUser");

        ResponseEntity<Void> response = eventosController.removerParticipante("hashEvento", "hashUser");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── inscrever ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /eventos/{eventoId}/inscrever deve retornar 200")
    void inscrever_valido_retorna200() throws Exception {
        doNothing().when(eventoService).inscreverParticipante("hashEvento", "hashUser");

        ResponseEntity<Void> response = eventosController.inscrever("hashEvento", "hashUser");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ─── cancelarInscricao ────────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /{id}/participantes/{utilizadorId}/cancelar deve retornar 200")
    void cancelarInscricao_valido_retorna200() throws Exception {
        doNothing().when(eventoService).cancelarInscricao("hashEvento", "hashUser");

        ResponseEntity<Void> response = eventosController.cancelarInscricao("hashEvento", "hashUser");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("PATCH /{id}/participantes/{utilizadorId}/cancelar deve retornar 400 se erro")
    void cancelarInscricao_quandoErro_retorna400() throws Exception {
        doThrow(new Exception("Inscrição não encontrada"))
                .when(eventoService).cancelarInscricao("hashEvento", "hashUser");

        ResponseEntity<Void> response = eventosController.cancelarInscricao("hashEvento", "hashUser");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ─── editarEstado ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /{id}/estado/{novoEstadoId} deve retornar 200")
    void editarEstado_valido_retorna200() throws Exception {
        doNothing().when(eventoService).editarEstado("hashEvento", 2);

        ResponseEntity<Void> response = eventosController.editarEstado("hashEvento", 2);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("PATCH /{id}/estado/{novoEstadoId} deve retornar 404 se evento não existe")
    void editarEstado_inexistente_retorna404() throws Exception {
        doThrow(new Exception("Não encontrado")).when(eventoService).editarEstado("naoExiste", 2);

        ResponseEntity<Void> response = eventosController.editarEstado("naoExiste", 2);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ─── listarPorUtilizador ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /eventos/utilizador/{utilizadorId} deve retornar 200 com lista")
    void listarPorUtilizador_retorna200() {
        when(eventoService.findEventosPorUtilizador("hashUser")).thenReturn(List.of(eventoDto));

        ResponseEntity<List<EventoDto>> response = eventosController.listarPorUtilizador("hashUser");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // ─── getParticipantes ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /eventos/{id}/participantes deve retornar 200 com lista")
    void getParticipantes_retorna200ComLista() {
        ParticipanteDto p = new ParticipanteDto("Ana", "ana@escola.com", true, false);
        when(eventoService.listarParticipantes("hashEvento")).thenReturn(List.of(p));

        ResponseEntity<List<ParticipanteDto>> response = eventosController.getParticipantes("hashEvento");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Ana", response.getBody().get(0).utilizadorNome());
    }
}