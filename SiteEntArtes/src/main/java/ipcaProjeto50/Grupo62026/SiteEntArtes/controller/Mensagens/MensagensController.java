package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Mensagens;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagemCriarDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagemGrupoCriarDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagenDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagenPreviewDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.MensagemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/mensagens")
public class MensagensController {

    private static final Logger logger = LoggerFactory.getLogger(MensagensController.class);

    private final MensagemService mensagemService;

    public MensagensController(MensagemService mensagemService) {
        this.mensagemService = mensagemService;
    }

    @GetMapping("/previews")
    public ResponseEntity<List<MensagenPreviewDto>> getPreviewMensagens(@AuthenticationPrincipal String userEmail) {
        try {
            return ResponseEntity.ok(mensagemService.buscarPreviewMensagens(userEmail));

        } catch (NoSuchElementException e) {
            logger.warn("Utilizador não encontrado ao buscar previews: {}", userEmail);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro ao buscar previews de mensagens para {}: {}", userEmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/conversa")
    public ResponseEntity<List<MensagenDto>> getMensagensConversa(
            @AuthenticationPrincipal String userEmail,
            @RequestParam String conversaId) {
        try {
            return ResponseEntity.ok(mensagemService.mensagensConversa(userEmail, conversaId));

        } catch (NoSuchElementException e) {
            logger.warn("Conversa não encontrada, id: {}", conversaId);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("ID de conversa inválido: {}", conversaId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao buscar mensagens da conversa {}: {}", conversaId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- NOVOS ENDPOINTS PARA GRUPOS ---

    @GetMapping("/conversa-grupo")
    public ResponseEntity<?> getMensagensGrupo(
            @AuthenticationPrincipal String userIdHashed,
            @RequestParam String grupoId) {
        try {
            return ResponseEntity.ok(mensagemService.mensagensConversaGrupo(userIdHashed, grupoId));

        } catch (NoSuchElementException e) {
            logger.warn("Grupo não encontrado, id: {}", grupoId);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("ID de grupo inválido: {}", grupoId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao buscar mensagens do grupo {}: {}", grupoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/grupo")
    public ResponseEntity<MensagenDto> criarMensagemGrupo(
            @AuthenticationPrincipal String userEmail,
            @RequestBody MensagemGrupoCriarDto dto) {
        try {
            return ResponseEntity.ok(mensagemService.criarMensagemGrupo(userEmail, dto));

        } catch (NoSuchElementException e) {
            logger.warn("Grupo ou utilizador não encontrado ao criar mensagem de grupo: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos ao criar mensagem de grupo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao criar mensagem de grupo para {}: {}", userEmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- FIM DOS NOVOS ENDPOINTS ---

    @PostMapping
    public ResponseEntity<MensagenDto> criarMensagem(
            @AuthenticationPrincipal String userEmail,
            @RequestBody MensagemCriarDto mensagemCriar) {
        try {
            return ResponseEntity.ok(mensagemService.criar(userEmail, mensagemCriar));

        } catch (NoSuchElementException e) {
            logger.warn("Destinatário não encontrado ao criar mensagem: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos ao criar mensagem: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao criar mensagem para {}: {}", userEmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMensagem(@PathVariable String id) {
        try {
            mensagemService.eliminar(id);
            return ResponseEntity.noContent().build();

        } catch (NoSuchElementException e) {
            logger.warn("Mensagem não encontrada para eliminar, id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("ID de mensagem inválido para eliminar: {}", id);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao eliminar mensagem com id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
