package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Mensagens;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.GrupoRequestDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.GrupoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;

    @PostMapping
    public ResponseEntity<String> criarGrupo(@RequestBody GrupoRequestDto dto) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // No teu caso, auth.getName() vai devolver o "sub" do token (o ID Hasheado)
            String userIdHashed = auth.getName();

            grupoService.criarGrupoPrivado(userIdHashed, dto.nome(), dto.membrosIds());
            return ResponseEntity.ok("Grupo criado com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{grupoId}/adicionar/{membroId}")
    public ResponseEntity<String> adicionarMembro(
            @AuthenticationPrincipal String adminIdHashed,
            @PathVariable String grupoId,
            @PathVariable String membroId) {
        try {
            grupoService.adicionarMembro(adminIdHashed, grupoId, membroId);
            return ResponseEntity.ok("Membro adicionado com sucesso.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{grupoId}/remover/{membroId}")
    public ResponseEntity<String> removerMembro(
            @AuthenticationPrincipal String adminIdHashed,
            @PathVariable String grupoId,
            @PathVariable String membroId) {
        try {
            grupoService.removerMembro(adminIdHashed, grupoId, membroId);
            return ResponseEntity.ok("Membro removido com sucesso.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
