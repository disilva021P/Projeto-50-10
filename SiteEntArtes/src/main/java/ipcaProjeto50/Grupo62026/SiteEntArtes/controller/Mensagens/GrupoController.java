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

    @GetMapping("/{grupoId}/membros")
    public ResponseEntity<?> listarMembros(@PathVariable String grupoId) {
        try {
            var membros = grupoService.listarMembrosDoGrupo(grupoId);
            return ResponseEntity.ok(membros);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<String> criarGrupo(@RequestBody GrupoRequestDto dto) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userIdHashed = auth.getName();
            // Alterado para retornar o ID se necessário, mas podes manter como está
            grupoService.criarGrupoPrivado(userIdHashed, dto.nome(), dto.membrosIds());
            return ResponseEntity.ok("Grupo criado com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{grupoId}/adicionar/{membroId}")
    public ResponseEntity<String> adicionarMembro(
            @PathVariable String grupoId,
            @PathVariable String membroId) {
        try {
            // Se o @AuthenticationPrincipal não estiver a injetar a String corretamente,
            // usa o SecurityContextHolder como fizeste no criarGrupo:
            String adminIdHashed = SecurityContextHolder.getContext().getAuthentication().getName();

            grupoService.adicionarMembro(adminIdHashed, grupoId, membroId);
            return ResponseEntity.ok("Membro adicionado com sucesso.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{grupoId}/remover/{membroId}")
    public ResponseEntity<String> removerMembro(
            @PathVariable String grupoId,
            @PathVariable String membroId) {
        try {
            String adminIdHashed = SecurityContextHolder.getContext().getAuthentication().getName();

            grupoService.removerMembro(adminIdHashed, grupoId, membroId);
            return ResponseEntity.ok("Membro removido com sucesso.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}