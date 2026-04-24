package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Faltas;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.Utils;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaResponseDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.CancelamentoService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JustificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/faltas")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CancelamentoController {

    private final CancelamentoService cancelamentoService;
    private final JustificacaoService justificacaoService;

    // --- AÇÕES DE REGISTO E GESTÃO ---

    @PostMapping("/marcar")
    @PreAuthorize("hasAnyRole( 'PROFESSOR', 'ADMIN')")
    public ResponseEntity<?> marcar(@RequestBody FaltaDto dto) {
        try {
            FaltaDto resultado = cancelamentoService.marcarFalta(dto, Utils.getAuthenticatedUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao marcar falta: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN')")
    public ResponseEntity<?> atualizar(@PathVariable String id, @RequestBody FaltaDto dto) {
        try {
            FaltaDto resultado = cancelamentoService.atualizarFalta(id, dto);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro ao atualizar: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> remover(@PathVariable String id) {
        try {
            cancelamentoService.removerFalta(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao remover: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/validar")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN')")
    public ResponseEntity<?> validar(@PathVariable String id, @RequestParam boolean aprovada) {
        try {
            justificacaoService.validarFalta(id, aprovada);
            return ResponseEntity.ok("Estado da falta atualizado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro na validação: " + e.getMessage());
        }
    }

    // --- LISTAGENS ---

    @GetMapping("/utilizador/{idHash}/detalhe")
    @PreAuthorize("hasAnyRole('ALUNO', 'PROFESSOR', 'ADMIN')")
    public ResponseEntity<List<FaltaResponseDto>> listarFaltasPorUtilizador(@PathVariable String idHash) {
        return ResponseEntity.ok(cancelamentoService.listarFaltasPorUtilizador(idHash));
    }

    @GetMapping("/aluno/{alunoId}/estatisticas")
    @PreAuthorize("hasAnyRole('ALUNO', 'PROFESSOR', 'ADMIN')")
    public ResponseEntity<FaltaResumoDto> obterResumoEstatisticas(@PathVariable String alunoId) {
        return ResponseEntity.ok(cancelamentoService.obterResumoEstatisticas(alunoId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN')")
    public ResponseEntity<List<FaltaDto>> listarTodas() {
        return ResponseEntity.ok(cancelamentoService.listarTodas());
    }

    @GetMapping("/pendentes")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN')")
    public ResponseEntity<List<FaltaDto>> listarPendentes() {
        return ResponseEntity.ok(cancelamentoService.listarPendentes());
    }
}