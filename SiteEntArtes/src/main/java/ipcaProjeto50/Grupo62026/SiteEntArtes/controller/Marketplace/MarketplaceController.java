package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Marketplace;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.MarketplaceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/marketplace")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    public MarketplaceController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @GetMapping
    public ResponseEntity<Page<ArtigoDto>> listarArtigos(
            @RequestParam(defaultValue = "0")        int page,
            @RequestParam(defaultValue = "12")       int size,
            @RequestParam(defaultValue = "criadoEm") String sortBy,
            @RequestParam(defaultValue = "desc")     String direction,
            @RequestParam(required = false)          Integer estado   // <-- era "tipo"
    ) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ArtigoDto> resultado = (estado != null)
                ? marketplaceService.listarArtigosPorEstado(estado, pageable)
                : marketplaceService.listarArtigos(pageable);

        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    public ResponseEntity<ArtigoDto> inserirArtigo(
            @RequestBody ArtigoRequest request,
            Authentication authentication
    ) {
        System.out.println(">>> Nome no token: " + authentication.getName());
        String id = authentication.getName();
        ArtigoDto criado = marketplaceService.inserirArtigo(request, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }
}