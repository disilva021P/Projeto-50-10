package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Mensagens;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagemCriarDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagemGrupoCriarDto; // Precisas de criar este DTO
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagenDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagenPreviewDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.MensagemService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mensagens")
public class MensagensController {

    private final MensagemService mensagemService;

    public MensagensController(MensagemService mensagemService) {
        this.mensagemService = mensagemService;
    }

    @GetMapping("/previews")
    public ResponseEntity<List<MensagenPreviewDto>> getPreviewMensagens(@AuthenticationPrincipal String userEmail) {
        return ResponseEntity.ok(mensagemService.buscarPreviewMensagens(userEmail));
    }

    @GetMapping("/conversa")
    public ResponseEntity<List<MensagenDto>> getMensagensConversa(
            @AuthenticationPrincipal String userEmail,
            @RequestParam String conversaId) {
        return ResponseEntity.ok(mensagemService.mensagensConversa(userEmail, conversaId));
    }

    // --- NOVOS ENDPOINTS PARA GRUPOS ---

    @GetMapping("/conversa-grupo")
    public ResponseEntity<List<MensagenDto>> getMensagensConversaGrupo(
            @AuthenticationPrincipal String userEmail,
            @RequestParam String grupoId) {
        return ResponseEntity.ok(mensagemService.mensagensConversaGrupo(userEmail, grupoId));
    }

    @PostMapping("/grupo")
    public ResponseEntity<MensagenDto> criarMensagemGrupo(
            @AuthenticationPrincipal String userEmail,
            @RequestBody MensagemGrupoCriarDto dto) {
        return ResponseEntity.ok(mensagemService.criarMensagemGrupo(userEmail, dto));
    }

    // --- FIM DOS NOVOS ENDPOINTS ---

    @PostMapping
    public ResponseEntity<MensagenDto> criarMensagem(
            @AuthenticationPrincipal String userEmail,
            @RequestBody MensagemCriarDto mensagemCriar) {
        return ResponseEntity.ok(mensagemService.criar(userEmail, mensagemCriar));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMensagem(@PathVariable String id) {
        mensagemService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}