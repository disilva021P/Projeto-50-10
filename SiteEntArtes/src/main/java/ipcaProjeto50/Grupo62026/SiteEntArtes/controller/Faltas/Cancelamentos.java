package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Faltas;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaResponseDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.CancelamentoService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JustificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/faltas")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CancelamentoController {

    private final CancelamentoService cancelamentoService;
    private final JustificacaoService justificacaoService;

    // Helper para obter o ID do utilizador autenticado
    private String getUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Proteção para testes sem token: se não houver user, devolve um ID de Admin fixo ou nulo
        if (principal instanceof String) return (String) principal;
        return "ID_ADMIN_TESTE";
    }

    // --- AÇÕES DE REGISTO E GESTÃO ---

    @PostMapping("/marcar")
    public FaltaDto marcar(@RequestBody FaltaDto dto) {
        return cancelamentoService.marcarFalta(dto, getUserId());
    }

    @PutMapping("/{id}")
    public FaltaDto atualizar(@PathVariable String id, @RequestBody FaltaDto dto) {
        return cancelamentoService.atualizarFalta(id, dto);
    }

    @DeleteMapping("/{id}")
    public void remover(@PathVariable String id) {
        cancelamentoService.removerFalta(id);
    }

    @PatchMapping("/{id}/validar")
    public void validar(@PathVariable String id, @RequestParam boolean aprovada) {
        justificacaoService.validarFalta(id, aprovada);
    }

    // --- LISTAGENS FORMATADAS PARA O FRONT-END ---

    // NOVO: Este endpoint serve para o ecrã de Gestão de Faltas (Alunos ou Professores)
    @GetMapping("/utilizador/{idHash}/detalhe")
    public List<FaltaResponseDto> listarFaltasPorUtilizador(@PathVariable String idHash) {
        return cancelamentoService.listarFaltasPorUtilizador(idHash);
    }

    // --- ESTATÍSTICAS ---

    @GetMapping("/aluno/{alunoId}/estatisticas")
    public FaltaResumoDto obterResumoEstatisticas(@PathVariable String alunoId) {
        return cancelamentoService.obterResumoEstatisticas(alunoId);
    }

    // --- CONSULTAS SIMPLIFICAIDAS ---

    @GetMapping
    public List<FaltaDto> listarTodas() {
        return cancelamentoService.listarTodas();
    }

    @GetMapping("/pendentes")
    public List<FaltaDto> listarPendentes() {
        return cancelamentoService.listarPendentes();
    }
}