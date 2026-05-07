package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Utilizadores;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ProfessoreDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.ProfessorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfessorControllerTest {

    @Mock
    private ProfessorService professorService;

    @InjectMocks
    private ProfessorController professorController;

    // ─── TESTE: LISTAR PROFESSORES (PAGINADO) ────────────────────────────────
    @Test
    @DisplayName("getProfessores - Deve retornar 200 com página de professores")
    void getProfessores_DeveRetornarPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        ProfessoreDto dto = new ProfessoreDto(new UtilizadoreResumoDto("hash", "Prof"), new BigDecimal("30.0"), false);
        Page<ProfessoreDto> page = new PageImpl<>(List.of(dto));

        when(professorService.findAllPageable(pageable)).thenReturn(page);

        ResponseEntity<Page<ProfessoreDto>> response = professorController.getProfessores(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(professorService).findAllPageable(pageable);
    }

    // ─── TESTE: LISTAR PARA SELECIONAR ───────────────────────────────────────
    @Test
    @DisplayName("getProfessoresSelecionar - Deve retornar lista de resumos")
    void getProfessoresSelecionar_DeveRetornarLista() {
        UtilizadoreResumoDto resumo = new UtilizadoreResumoDto("hash", "Nome");
        when(professorService.findAllUtilizador()).thenReturn(List.of(resumo));

        ResponseEntity<List<UtilizadoreResumoDto>> response = professorController.getProfessoresSelecionar(PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // ─── TESTE: ADICIONAR MODALIDADE ─────────────────────────────────────────
    @Test
    @DisplayName("adicionarModalidade - Deve retornar 200 em caso de sucesso")
    void adicionarModalidade_Sucesso() throws Exception {
        String pId = "profHash";
        String mId = "modHash";

        // 1. Criar o objeto que o Service REALMENTE retorna
        ProfessoreDto professorSimulado = new ProfessoreDto(
                new UtilizadoreResumoDto(pId, "Professor Teste"),
                new java.math.BigDecimal("35.00"),
                false
        );

        // 2. Configurar o Mock para retornar esse DTO
        // Podes usar o when(...).thenReturn(...) agora que o tipo está correto
        when(professorService.adicionarModalidade(pId, mId)).thenReturn(professorSimulado);

        // 3. Executar
        ResponseEntity<?> response = professorController.adicionarModalidade(pId, mId);

        // 4. Asserções
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(professorSimulado, response.getBody());
        verify(professorService).adicionarModalidade(pId, mId);
    }

    @Test
    @DisplayName("adicionarModalidade - Deve retornar 400 em caso de erro")
    void adicionarModalidade_Erro() throws Exception {
        when(professorService.adicionarModalidade(anyString(), anyString()))
                .thenThrow(new RuntimeException("Erro de teste"));

        ResponseEntity<?> response = professorController.adicionarModalidade("id1", "id2");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Erro ao associar modalidade"));
    }

    // ─── TESTE: REMOVER MODALIDADE ───────────────────────────────────────────
    @Test
    @DisplayName("removerModalidade - Deve retornar 204")
    void removerModalidade_Sucesso() throws Exception {
        doNothing().when(professorService).removerModalidade("p1", "m1");

        ResponseEntity<?> response = professorController.removerModalidade("p1", "m1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    // ─── TESTE: BUSCA POR MODALIDADE (COM PARAM) ─────────────────────────────
    @Test
    @DisplayName("getProfessores por modalidade - Deve retornar 200")
    void getProfessoresPorModalidade_Sucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        when(professorService.findByModalidade(eq("mod1"), any())).thenReturn(Page.empty());

        ResponseEntity<Page<ProfessoreDto>> response = professorController.getProfessores("mod1", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("getProfessores por modalidade - Deve retornar 400 se modalidade for vazia")
    void getProfessoresPorModalidade_Vazio() {
        ResponseEntity<Page<ProfessoreDto>> response = professorController.getProfessores("", PageRequest.of(0, 10));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}