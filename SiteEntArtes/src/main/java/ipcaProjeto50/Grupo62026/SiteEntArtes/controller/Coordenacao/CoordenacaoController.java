package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Coordenacao;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.MarketplaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coordenacao")
@PreAuthorize("hasAuthority('COORDENACAO')")
public class CoordenacaoController {

    private final MarketplaceService marketplaceService;

    @Autowired
    public CoordenacaoController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    /**
     * Lista todos os artigos que estão com estado 8 (Pendente)
     */
    @GetMapping("/pendentes")
    public ResponseEntity<?> listarPendentes() {
        try {
            List<ArtigoDto> pendentes = marketplaceService.listarArtigosPendentes();
            return ResponseEntity.ok(pendentes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao listar artigos pendentes: " + e.getMessage());
        }
    }

    /**
     * Aceita uma doação (Muda estado de 8 para 2)
     */
    @PostMapping("/aceitar/{artigoId}")
    public ResponseEntity<?> aceitarDoacao(@PathVariable String artigoId) {
        try {
            marketplaceService.alterarEstadoArtigo(artigoId, 2);
            return ResponseEntity.ok("Doação aceite com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artigo não encontrado: " + artigoId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao aceitar doação: " + e.getMessage());
        }
    }

    /**
     * Recusa uma doação (Muda estado de 8 para 5 - Removido)
     */
    @PostMapping("/recusar/{artigoId}")
    public ResponseEntity<?> recusarDoacao(@PathVariable String artigoId) {
        try {
            marketplaceService.alterarEstadoArtigo(artigoId, 5);
            return ResponseEntity.ok("Doação recusada com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artigo não encontrado: " + artigoId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao recusar doação: " + e.getMessage());
        }
    }
}