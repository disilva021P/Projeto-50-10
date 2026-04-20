package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Coordenacao;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.MarketplaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coordenacao")
// Garante que apenas utilizadores com a autoridade COORDENACAO podem aceder a este controller
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
    public ResponseEntity<List<ArtigoDto>> listarPendentes() {
        List<ArtigoDto> pendentes = marketplaceService.listarArtigosPendentes();
        return ResponseEntity.ok(pendentes);
    }

    /**
     * Aceita uma doação (Muda estado de 8 para 2)
     */
    @PostMapping("/aceitar/{artigoId}")
    public ResponseEntity<Void> aceitarDoacao(@PathVariable Integer artigoId) {
        marketplaceService.alterarEstadoArtigo(artigoId, 2);
        return ResponseEntity.ok().build();
    }

    /**
     * Recusa uma doação (Muda estado de 8 para 5 - Removido)
     */
    @PostMapping("/recusar/{artigoId}")
    public ResponseEntity<Void> recusarDoacao(@PathVariable Integer artigoId) {
        marketplaceService.alterarEstadoArtigo(artigoId, 5);
        return ResponseEntity.ok().build();
    }
}