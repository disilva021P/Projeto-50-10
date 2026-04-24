package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Faltas;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.Utils;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaResponseDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.CancelamentoService;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.JustificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PreAuthorize("hasAnyRole( 'PROFESSOR', 'COORDENACAO')")
    public ResponseEntity<?> marcar(@RequestBody FaltaDto dto) {
        try {
            FaltaDto resultado = cancelamentoService.marcarFalta(dto, Utils.getAuthenticatedUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao marcar falta: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENACAO')")
    public ResponseEntity<?> atualizar(@PathVariable String id, @RequestBody FaltaDto dto) {
        try {
            FaltaDto resultado = cancelamentoService.atualizarFalta(id, dto);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro ao atualizar: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COORDENACAO')")
    public ResponseEntity<?> remover(@PathVariable String id) {
        try {
            cancelamentoService.removerFalta(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao remover: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/validar")
    @PreAuthorize("hasAnyRole('COORDENACAO')")
    public ResponseEntity<?> validar(@PathVariable String id, @RequestParam boolean aprovada) {
        try {
            justificacaoService.validarFalta(id, aprovada,Utils.getAuthenticatedUserId());
            return ResponseEntity.ok("Estado da falta atualizado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro na validação: " + e.getMessage());
        }
    }

    // --- LISTAGENS ---

    @GetMapping("/utilizador/{idHash}/detalhe")
    @PreAuthorize("hasAnyRole('ALUNO', 'PROFESSOR', 'COORDENACAO')")
    public ResponseEntity<List<FaltaResponseDto>> listarFaltasPorUtilizador(@PathVariable String idHash) {
        return ResponseEntity.ok(cancelamentoService.listarFaltasPorUtilizador(idHash));
    }

    @GetMapping("/aluno/{alunoId}/estatisticas")
    @PreAuthorize("hasAnyRole('ALUNO', 'PROFESSOR', 'COORDENACAO')")
    public ResponseEntity<FaltaResumoDto> obterResumoEstatisticas(@PathVariable String alunoId) {
        return ResponseEntity.ok(cancelamentoService.obterResumoEstatisticas(alunoId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENACAO')")
    public ResponseEntity<List<FaltaDto>> listarTodas() {
        return ResponseEntity.ok(cancelamentoService.listarTodas());
    }

    @GetMapping("/pendentes")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENACAO')")
    public ResponseEntity<List<FaltaDto>> listarPendentes() {
        return ResponseEntity.ok(cancelamentoService.listarPendentes());
    }
    // Encarregado submete justificação com PDF
    @PostMapping("/{id}/justificar")
    @PreAuthorize("hasAnyRole('ENCARREGADO', 'PROFESSOR')")
    public ResponseEntity<?> submeterJustificacao(
            @PathVariable String id,
            @RequestParam("pdf") MultipartFile pdf,
            @RequestParam("motivo") String motivo) {
        try {
            justificacaoService.submeterJustificacao(id, pdf.getBytes(), motivo);
            return ResponseEntity.ok("Justificação submetida com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Coordenação consulta o PDF
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<byte[]> verPdf(@PathVariable String id) {
        try {
            byte[] pdf = justificacaoService.verConteudoPdf(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}