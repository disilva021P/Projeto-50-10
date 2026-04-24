package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.DisponibilidadeProfessor;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.AulaProfessoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.DisponibilidadeProfessorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Inicializa os Mocks do Mockito
class DisponibilidadeServiceTest {

    @Mock
    private DisponibilidadeProfessorRepository disponibilidadeProfessorRepository;

    @Mock
    private IdHasher idHasher;

    @Mock
    private AulaProfessoreRepository aulaProfessoreRepository;

    @Mock
    private ProfessorService professorService;

    @InjectMocks
    private DisponibilidadeService disponibilidadeService; // Injeta os mocks acima aqui

    @Test
    @DisplayName("Deve retornar true quando a marcação for válida (tem disponibilidade e não tem conflito)")
    void verificaMarcacaoValida_Sucesso() {
        // GIVEN (Dado que...)
        String idHash = "abc-123";
        Integer idReal = 10;
        LocalDate data = LocalDate.now();
        LocalTime inicio = LocalTime.of(14, 0);
        LocalTime fim = LocalTime.of(15, 0);
        int diaSemana = data.getDayOfWeek().getValue();

        when(idHasher.decode(idHash)).thenReturn(idReal);

        // Simula que existe disponibilidade
        when(disponibilidadeProfessorRepository.verificarDisponibilidade(idReal, diaSemana, data, inicio, fim))
                .thenReturn(Optional.of(new DisponibilidadeProfessor()));

        // Simula que o professor NÃO possui aula nesse horário (false)
        when(aulaProfessoreRepository.professorJaPossuiAula(idReal, data, inicio, fim))
                .thenReturn(false);

        // WHEN (Quando executar...)
        boolean resultado = disponibilidadeService.verificaMarcacaoValida(idHash, data, inicio, fim);

        // THEN (Então verifique...)
        assertTrue(resultado);
        verify(disponibilidadeProfessorRepository, times(1)).verificarDisponibilidade(any(), anyInt(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a disponibilidade não for encontrada pelo ID")
    void findById_DeveLancarExcecao() {
        // GIVEN
        String idHash = "invalido";
        when(idHasher.decode(idHash)).thenReturn(99);
        when(disponibilidadeProfessorRepository.findById(99)).thenReturn(Optional.empty());

        // WHEN & THEN
        Exception exception = assertThrows(Exception.class, () -> {
            disponibilidadeService.findById(idHash);
        });

        assertEquals("Disponibilidade não encontrada", exception.getMessage());
    }
}