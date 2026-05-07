package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Utilizadores;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ModalidadeDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ProfessoreDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/professores") // Ajustado para ser mais semântico
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProfessorController {

    private final ProfessorService professorService;
    @GetMapping
    public ResponseEntity<Page<ProfessoreDto>> getProfessores(
            @PageableDefault(page = 0, size = 10) Pageable pageable // @ParameterObject ajuda na documentação Swagger/OpenAPI
    ) {
        return ResponseEntity.ok(professorService.findAllPageable(pageable));
    }
    @GetMapping("/selecionar")
    public ResponseEntity<List<UtilizadoreResumoDto>> getProfessoresSelecionar(
            @PageableDefault(page = 0, size = 10) Pageable pageable // @ParameterObject ajuda na documentação Swagger/OpenAPI
    ) {
        return ResponseEntity.ok(professorService.findAllUtilizador());
    }
    @GetMapping("/{modalidadeId}")
    public ResponseEntity<Page<ProfessoreDto>> getProfessores(
            @RequestParam() String modalidadeId,
            @PageableDefault(page = 0, size = 10) Pageable pageable // @ParameterObject ajuda na documentação Swagger/OpenAPI
    ) {
        if (modalidadeId != null && !modalidadeId.isEmpty()) {
            return ResponseEntity.ok(professorService.findByModalidade(modalidadeId, pageable));
        }
        return ResponseEntity.badRequest().build();
    }
    @PostMapping("/{professorId}/modalidade/{modalidadeId}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> adicionarModalidade(
            @PathVariable String professorId,
            @PathVariable String modalidadeId) {
        try {
            return ResponseEntity.ok(professorService.adicionarModalidade(professorId, modalidadeId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao associar modalidade: " + e.getMessage());
        }
    }

    @DeleteMapping("/{professorId}/modalidade/{modalidadeId}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<?> removerModalidade(
            @PathVariable String professorId,
            @PathVariable String modalidadeId) {
        try {
            professorService.removerModalidade(professorId, modalidadeId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao remover associação: " + e.getMessage());
        }
    }
}