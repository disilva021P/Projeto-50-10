package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Marketplace;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ConversaoInventarioRequest;
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
import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;
    private final ImagensUnidadeRepository imagensUnidadeRepository;
    private final IdHasher idHasher;

    public MarketplaceController(MarketplaceService marketplaceService,
                                 ImagensUnidadeRepository imagensUnidadeRepository,
                                 IdHasher idHasher) {
        this.marketplaceService = marketplaceService;
        this.imagensUnidadeRepository = imagensUnidadeRepository;
        this.idHasher = idHasher;
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
                @RequestParam(required = false)          String donoId
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
            @RequestParam("imagens") List<MultipartFile> imagens,
            Authentication authentication
    ) {
        try {
            ArtigoRequest request = new ArtigoRequest(
                    nome, descricao, tamanho, cor, condicao,
                    isVenda, isAluguer, isDoacao, precoVenda, precoAluguer, imagens
            );
            String utilizadorEmailOuUsername = authentication.getName();
            ArtigoDto criado = marketplaceService.inserirArtigo(request, imagens, utilizadorEmailOuUsername);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/imagem/{id}")
    public ResponseEntity<byte[]> getImagem(@PathVariable String id) {
        Integer idOriginal = idHasher.decode(id);
        return imagensUnidadeRepository.findById(idOriginal)
                .map(img -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(img.getUrlImagem()))
                .orElse(ResponseEntity.notFound().build());
    }

    //Remover artigo (Arquivar)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> arquivar(@PathVariable String id) { // Recebe String
        marketplaceService.arquivarArtigo(id);
        return ResponseEntity.noContent().build();
    }

    //Apagar imagem
    @DeleteMapping("/imagem/{imagemId}")
    public ResponseEntity<Void> apagarImagem(@PathVariable String imagemId) {
        marketplaceService.removerImagem(imagemId);
        return ResponseEntity.noContent().build();
    }

    //Editar artigo
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ArtigoDto> editar(
            @PathVariable String id,
            @ModelAttribute ArtigoRequest request
    ) {
        return ResponseEntity.ok(marketplaceService.editarArtigo(id, request));
    }


    /**
     * Endpoint para a coordenação aprovar, recusar ou enviar para inventário.
     * PUT /api/marketplace/artigos/{id}/estado/{novoEstadoId}
     */
    @PutMapping("/artigos/{id}/estado/{novoEstadoId}")
    public ResponseEntity<Void> alterarEstado(
            @PathVariable Integer id,
            @PathVariable Integer novoEstadoId
    ) {
        marketplaceService.alterarEstadoArtigo(id, novoEstadoId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/importar-inventario", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importarDoInventario(
            @ModelAttribute ConversaoInventarioRequest request
    ) {
        // 1. Obter o ID do coordenador (Exemplo: fixo ou via Security)
        // Se tiveres Security: Integer coordenadorId = ((Utilizadore) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        Integer coordenadorId = 1; // Temporário para testes

        // 2. Passar os DOIS argumentos como o Service espera
        marketplaceService.converterUnidadeParaMarketplace(request, coordenadorId);

        return ResponseEntity.ok("Artigo importado com sucesso!");
    }
}