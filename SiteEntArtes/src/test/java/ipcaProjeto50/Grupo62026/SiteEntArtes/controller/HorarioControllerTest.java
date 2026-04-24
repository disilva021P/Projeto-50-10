package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Horarios;

// Imports Java e Jackson
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// Imports JUnit e Mockito
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

// Imports Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

// Imports Spring Security Test
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

// Imports MockMvc (Estáticos)
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Imports do teu Projeto
import ipcaProjeto50.Grupo62026.SiteEntArtes.config.SecurityConfig;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaCoachingRequestDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.DisponibilidadeProfessorDtoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.*;

@WebMvcTest(HorarioController.class)
@Import(SecurityConfig.class)
public class HorarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Mocks dos Serviços do Controller ---
    @MockitoBean
    private AulaService aulaService;
    @MockitoBean private AulaCoachingService aulaCoachingService;
    @MockitoBean private DisponibilidadeService disponibilidadeService;
    @MockitoBean private AulaFixaService aulaFixaService;
    @MockitoBean private UtilizadorService utilizadorService;

    // --- Mocks para Segurança e Contexto ---
    @MockitoBean private JwtService jwtService;
    @MockitoBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    @DisplayName("Teste: Professor insere as suas disponibilidades")
    @WithMockUser(username = "prof1", authorities = "PROFESSOR")
    void professorInsereDisponibilidade() throws Exception {
        // Dados baseados no JSON que enviou anteriormente
        var dto = new DisponibilidadeProfessorDtoRequest(
                "prof1",
                5, // dia da semana
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                LocalDate.now(),
                LocalDate.now().plusDays(30)
        );

        mockMvc.perform(post("/api/horario/insereDisponibilidade")
                        .with(csrf()) // Garante que passa pelo filtro mesmo com CSRF disable
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Teste: Encarregado marca aula de coaching ao seu educando")
    @WithMockUser(username = "pai1", authorities = "ENCARREGADO")
    void encarregadoMarcaCoaching() throws Exception {
        String educandoId = "aluno123";

        // Simulação da lista de educandos para passar na validação private do controller
        Mockito.when(utilizadorService.findEducandosdeEducador("pai1"))
                .thenReturn(List.of(new UtilizadoreResumoDto(educandoId, "Nome Aluno")));

        var dto = new AulaCoachingRequestDto(
                "prof1",
                "estudio1",
                LocalDate.now().plusDays(5),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                8,
                "mod_piano"
        );

        mockMvc.perform(post("/api/horario/marcarcoaching/educando/{educandoId}", educandoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("Erro: Encarregado tenta marcar coaching fora do horário de disponibilidade do professor")
    @WithMockUser(username = "pai1", authorities = "ENCARREGADO")
    void erroHorarioForaDaDisponibilidade() throws Exception {
        String educandoId = "aluno123";

        // 1. Simular permissão
        Mockito.when(utilizadorService.findEducandosdeEducador("pai1"))
                .thenReturn(List.of(new UtilizadoreResumoDto(educandoId, "Nome")));

        // 2. CONFIGURAR O MOCK PARA FALHAR:
        // Quando o service for chamado, simulamos que ele valida a disponibilidade e lança erro
        Mockito.when(aulaCoachingService.salvarMarcarCoaching(any(AulaCoachingRequestDto.class), eq(educandoId)))
                .thenThrow(new RuntimeException("O professor não tem disponibilidade neste horário (Disponível apenas das 10h às 12h)"));

        // 3. Criar o DTO com horário INVÁLIDO (14h às 15h)
        var dto = new AulaCoachingRequestDto(
                "prof1",
                "est1",
                LocalDate.now().plusDays(1),
                LocalTime.of(14, 0), // Fora do intervalo 10-12
                LocalTime.of(15, 0),
                1,
                "PIANO"
        );

        // 4. Executar e esperar 400 Bad Request
        mockMvc.perform(post("/api/horario/marcarcoaching/educando/{educandoId}", educandoId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("Teste: Professor confirma e rejeita uma aula de coaching")
    @WithMockUser(username = "prof1", authorities = "PROFESSOR")
    void professorConfirmaRejeitaCoaching() throws Exception {
        String aulaId = "aula456";

        // 1. Testar Confirmar
        mockMvc.perform(put("/api/horario/professor/coaching/{aulaId}/confirmar", aulaId)
                        .with(csrf()))
                .andExpect(status().isOk());

        // 2. Testar Rejeitar
        mockMvc.perform(put("/api/horario/professor/coaching/rejeitar/{id}", aulaId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}