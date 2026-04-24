package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Mensagens;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagemCriarDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagenDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagenPreviewDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.MensagemService;
import org.springframework.http.HttpStatus;
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

    // GET /api/mensagens/previews/{userId}
    // Retorna lista de previews de conversas do utilizador
    @GetMapping("/previews")
    public ResponseEntity<List<MensagenPreviewDto>> getPreviewMensagens(@AuthenticationPrincipal String userEmail) {

        return ResponseEntity.ok(mensagemService.buscarPreviewMensagens(userEmail));
    }

    // GET /api/mensagens/conversa?userId=xxx&conversaId=yyy
    // Retorna o histórico completo de uma conversa
    @GetMapping("/conversa")
    public ResponseEntity<List<MensagenDto>> getMensagensConversa(
            @AuthenticationPrincipal String userEmail,
            @RequestParam String conversaId) {
        return ResponseEntity.ok(mensagemService.mensagensConversa(userEmail, conversaId));
    }

    // POST /api/mensagens
    // Cria e envia uma nova mensagem
    @PostMapping
    public ResponseEntity<MensagenDto> criarMensagem(
            @AuthenticationPrincipal String userEmail,
            @RequestBody MensagemCriarDto mensagemCriar) {
        return ResponseEntity.ok(mensagemService.criar(userEmail, mensagemCriar));
    }

    // DELETE /api/mensagens/{id}
    // Elimina uma mensagem pelo seu ID (hasheado)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMensagem(@PathVariable String id) {
        mensagemService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}