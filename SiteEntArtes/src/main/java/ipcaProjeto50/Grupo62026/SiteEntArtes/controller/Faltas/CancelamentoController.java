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
            justificacaoService.removerJustificacao(id);
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

    // --- ENDPOINTS PARA O PRÓPRIO UTILIZADOR (ALUNO/PROFESSOR) ---

        @GetMapping("/meu-perfil/detalhe")
    @PreAuthorize("hasAnyRole('ALUNO', 'PROFESSOR')")
    public ResponseEntity<List<FaltaDto>> listarMinhasFaltas() {
        // Extrai o ID diretamente do Token de quem está logado
        return ResponseEntity.ok(cancelamentoService.listarFaltasPorUtilizador(Utils.getAuthenticatedUserId()));
    }
    @GetMapping("/encarregado/educandos/estatisticas")
    @PreAuthorize("hasRole('ENCARREGADO')")
    public ResponseEntity<FaltaResumoDto> obterEstatisticasDosEducandos() {
        // Obtém o resumo somado de todos os educandos do encarregado logado
        FaltaResumoDto resumo = cancelamentoService.obterResumoEstatisticasEducandos(Utils.getAuthenticatedUserId());
        return ResponseEntity.ok(resumo);
    }
    @GetMapping("/meu-perfil/estatisticas")
    @PreAuthorize("hasAnyRole('ALUNO', 'PROFESSOR')")
    public ResponseEntity<FaltaResumoDto> obterMinhasEstatisticas() {
        // Extrai o ID diretamente do Token de quem está logado

        return ResponseEntity.ok(cancelamentoService.obterResumoEstatisticas(Utils.getAuthenticatedUserId()));
    }
    @GetMapping("/professor/{aulaId}/faltas")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<List<FaltaDto>> listarFaltasDaMinhasAula(@PathVariable("aulaId") String aulaId) {
        // 1. Obtém o professor autenticado
        // 2. Chama o serviço passando o ID ou o objeto do professor
        return ResponseEntity.ok(cancelamentoService.listarFaltasPorProfessorAula(Utils.getAuthenticatedUserId(),aulaId));
    }
// --- ENDPOINTS ADMINISTRATIVOS (COORDENAÇÃO) ---
// Mantemos os originais, mas agora restritos apenas à COORDENACAO
// (ou PROFESSOR se ele tiver permissão de ver de outros alunos)

    @GetMapping("/utilizador/{idHash}/detalhe")
    @PreAuthorize("hasRole('COORDENACAO')")
    public ResponseEntity<List<FaltaDto>> listarFaltasPorUtilizador(@PathVariable String idHash) {
        return ResponseEntity.ok(cancelamentoService.listarFaltasPorUtilizador(idHash));
    }

    @GetMapping("/aluno/{alunoId}/estatisticas")
    @PreAuthorize("hasRole('COORDENACAO')")
    public ResponseEntity<FaltaResumoDto> obterResumoEstatisticas(@PathVariable String alunoId) {
        return ResponseEntity.ok(cancelamentoService.obterResumoEstatisticas(alunoId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COORDENACAO')")
    public ResponseEntity<List<FaltaDto>> listarTodas() {
        return ResponseEntity.ok(cancelamentoService.listarTodas());
    }

    @GetMapping("/pendentes")
    @PreAuthorize("hasAnyRole('COORDENACAO')")
    public ResponseEntity<List<FaltaDto>> listarPendentes() {
        return ResponseEntity.ok(cancelamentoService.listarPendentes());
    }
    @GetMapping("/encarregado/educandos/faltas")
    @PreAuthorize("hasRole('ENCARREGADO')")
    public ResponseEntity<List<FaltaDto>> listarFaltasDosMeusEducandos() {
        // O serviço deve buscar os IDs dos alunos associados ao ID do Encarregado logado
        return ResponseEntity.ok(cancelamentoService.listarFaltasDosEducandos(Utils.getAuthenticatedUserId()));
    }
    // Encarregado submete justificação com PDF
    @PostMapping("/{id}/justificar")
    @PreAuthorize("hasAnyRole('ALUNO','ENCARREGADO', 'PROFESSOR','COORDENCAO')")
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