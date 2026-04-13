package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Marketplace;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ImagensUnidadeRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.MarketplaceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/marketplace")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;
    private final ImagensUnidadeRepository imagensUnidadeRepository;

    public MarketplaceController(MarketplaceService marketplaceService, ImagensUnidadeRepository imagensUnidadeRepository) {
        this.marketplaceService = marketplaceService;
        this.imagensUnidadeRepository = imagensUnidadeRepository;
    }

    /**
     * Listagem com filtros dinâmicos: Tipo Negócio (0,1,2), Tamanho e Range de Preço.
     */
    @GetMapping
        public ResponseEntity<Page<ArtigoDto>> listarArtigos(
                @RequestParam(defaultValue = "0")        int page,
                @RequestParam(defaultValue = "12")       int size,
                @RequestParam(defaultValue = "criadoEm") String sortBy,
                @RequestParam(defaultValue = "desc")     String direction,
                @RequestParam(required = false)          String nome,
                @RequestParam(required = false)          Integer tipoId,
                @RequestParam(required = false)          String tamanho,
                @RequestParam(required = false)          String cor,
                @RequestParam(required = false)          String condicao,
                @RequestParam(required = false)          Double min,
                @RequestParam(required = false)          Double max,
                @RequestParam(required = false)          Integer donoId
        ) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ArtigoDto> resultado = marketplaceService.filtrarArtigos(
                nome, tipoId, tamanho, cor, condicao, min, max, donoId, pageable
        );

        return ResponseEntity.ok(resultado);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArtigoDto> inserirArtigo(
            @RequestParam("nome")          String nome,
            @RequestParam("descricao")     String descricao,
            @RequestParam(value = "tamanho",      required = false) String tamanho,
            @RequestParam(value = "cor",          required = false) String cor,
            @RequestParam(value = "condicao",     required = false) String condicao,
            @RequestParam(value = "isVenda",      defaultValue = "false") Boolean isVenda,
            @RequestParam(value = "isAluguer",    defaultValue = "false") Boolean isAluguer,
            @RequestParam(value = "isDoacao",     defaultValue = "false") Boolean isDoacao,
            @RequestParam(value = "precoVenda",   required = false) BigDecimal precoVenda,
            @RequestParam(value = "precoAluguer", required = false) BigDecimal precoAluguer,
            @RequestParam("imagem")        MultipartFile imagem,
            Authentication authentication
    ) {
        try {
            ArtigoRequest request = new ArtigoRequest(
                    nome, descricao, tamanho, cor, condicao,
                    isVenda, isAluguer, isDoacao, precoVenda, precoAluguer
            );
            String utilizadorEmailOuUsername = authentication.getName();
            ArtigoDto criado = marketplaceService.inserirArtigo(request, imagem, utilizadorEmailOuUsername);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/imagem/{id}")
    public ResponseEntity<byte[]> getImagem(@PathVariable Integer id) {
        return imagensUnidadeRepository.findById(id)
                .map(img -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(img.getUrlImagem()))
                .orElse(ResponseEntity.notFound().build());
    }
}