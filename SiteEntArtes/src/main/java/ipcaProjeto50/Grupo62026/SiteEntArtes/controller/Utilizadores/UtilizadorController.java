package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Utilizadores;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.UtilizadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/utilizadores")
@RequiredArgsConstructor
public class UtilizadorController {
    private String getUserId() {
        return (String) Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getPrincipal();
    }
    private final UtilizadorService utilizadorService;

    // ─── GET /api/utilizadores?tipo=ROLE_ALUNO ────────────────────────────────
    // Lista todos os utilizadores, com filtro opcional por tipo
    // Só coordenação tem acesso
    @GetMapping
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<List<UtilizadorResponseDto>> listarTodos(
            @RequestParam(required = false) String tipo) {
        return ResponseEntity.ok(utilizadorService.listarTodos(tipo));
    }

    // ─── GET /api/utilizadores/{id} ───────────────────────────────────────────
    // Ver detalhe de um utilizador específico
    // Só coordenação tem acesso
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<UtilizadorResponseDto> verDetalhe(@PathVariable String id) {
        return ResponseEntity.ok(utilizadorService.verDetalhe(id));
    }

    // ─── POST /api/utilizadores ───────────────────────────────────────────────
    // Criar novo utilizador
    // Só coordenação pode criar
    @PostMapping
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<UtilizadorResponseDto> criarUtilizador(
            @Valid @RequestBody CriarUtilizadorDto dto) throws Exception {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(utilizadorService.criarUtilizador(dto));
    }

    // ─── PATCH /api/utilizadores/{id}/toggle-ativo ────────────────────────────
    // Ativar ou desativar conta de um utilizador
    // Só coordenação tem acesso
    @PatchMapping("/{id}/toggle-ativo")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<UtilizadorResponseDto> toggleAtivo(@PathVariable String id) {
        return ResponseEntity.ok(utilizadorService.toggleAtivo(id));
    }

    // ─── DELETE /api/utilizadores/{id} ───────────────────────────────────────
    // Apagar utilizador (soft delete — fica inativo na BD)
    // Só coordenação tem acesso
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<Void> apagarUtilizador(@PathVariable String id) {
        utilizadorService.apagarUtilizador(id);
        return ResponseEntity.noContent().build();
    }

    // ─── GET /api/utilizadores/meu-perfil ────────────────────────────────────
    // Ver o próprio perfil
    // Qualquer utilizador autenticado tem acesso
    @GetMapping("/meu-perfil")
    public ResponseEntity<UtilizadorResponseDto> verMeuPerfil() {
        String id = getUserId();
        return ResponseEntity.ok(utilizadorService.verMeuPerfil(id));}

    // ─── PATCH /api/utilizadores/minha-password ───────────────────────────────
    // Alterar a própria palavra-passe
    // Qualquer utilizador autenticado tem acesso
    @PatchMapping("/minha-password")
    public ResponseEntity<Void> alterarPalavraPasse(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AlterarPasswordDto dto) {
        utilizadorService.alterarPalavraPasse(userDetails.getUsername(), dto);
        return ResponseEntity.noContent().build();
    }

    // ─── PATCH /api/utilizadores/{id}/repor-password ─────────────────────────
    // Repor a palavra-passe de outro utilizador
    // Só coordenação tem acesso
    @PatchMapping("/{id}/repor-password")
    @PreAuthorize("hasAuthority('COORDENACAO')")
    public ResponseEntity<Void> reporPalavraPasse(
            @PathVariable String id,
            @Valid @RequestBody ReporPasswordDto dto) {
        utilizadorService.reporPalavraPasse(id, dto);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/meus-educandos")
    @PreAuthorize("hasAuthority('ENCARREGADO')")
    public ResponseEntity<List<UtilizadoreResumoDto>> educandosEncarregado(){
        return ResponseEntity.ok(utilizadorService.findEducandosdeEducador(getUserId()));
    }

}