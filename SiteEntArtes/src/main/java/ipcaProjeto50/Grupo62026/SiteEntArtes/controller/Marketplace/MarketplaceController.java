package ipcaProjeto50.Grupo62026.SiteEntArtes.controller.Marketplace;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.Utils;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ConversaoInventarioRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ImagensUnidadeRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.service.MarketplaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/marketplace")
public class MarketplaceController {

    private static final Logger logger = LoggerFactory.getLogger(MarketplaceController.class);

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
        try {
            Sort sort = direction.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            Page<ArtigoDto> resultado = marketplaceService.filtrarArtigos(
                    nome, tipoId, tamanho, cor, condicao, min, max, donoId, pageable
            );

            return ResponseEntity.ok(resultado);

        } catch (IllegalArgumentException e) {
            logger.warn("Parâmetros de filtragem inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao listar artigos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

        } catch (MaxUploadSizeExceededException e) {
            logger.warn("Tamanho das imagens excede o limite permitido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos ao inserir artigo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao inserir artigo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/imagem/{id}")
    public ResponseEntity<byte[]> getImagem(@PathVariable String id) {
        try {
            Integer idOriginal = idHasher.decode(id);
            return imagensUnidadeRepository.findById(idOriginal)
                    .map(img -> ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .body(img.getUrlImagem()))
                    .orElse(ResponseEntity.notFound().build());

        } catch (IllegalArgumentException e) {
            logger.warn("ID de imagem inválido: {}", id);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao obter imagem com id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Remover artigo (Arquivar)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> arquivar(@PathVariable String id) {
        try {
            marketplaceService.arquivarArtigo(id);
            return ResponseEntity.noContent().build();

        } catch (NoSuchElementException e) {
            logger.warn("Artigo não encontrado para arquivar, id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("ID de artigo inválido para arquivar: {}", id);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao arquivar artigo com id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Apagar imagem
    @DeleteMapping("/imagem/{imagemId}")
    public ResponseEntity<Void> apagarImagem(@PathVariable String imagemId) {
        try {
            marketplaceService.removerImagem(imagemId);
            return ResponseEntity.noContent().build();

        } catch (NoSuchElementException e) {
            logger.warn("Imagem não encontrada para apagar, id: {}", imagemId);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("ID de imagem inválido para apagar: {}", imagemId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao apagar imagem com id {}: {}", imagemId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Editar artigo
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ArtigoDto> editar(
            @PathVariable String id,
            @ModelAttribute ArtigoRequest request
    ) {
        try {
            ArtigoDto atualizado = marketplaceService.editarArtigo(id, request);
            return ResponseEntity.ok(atualizado);

        } catch (NoSuchElementException e) {
            logger.warn("Artigo não encontrado para editar, id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos ao editar artigo com id {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao editar artigo com id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para a coordenação aprovar, recusar ou enviar para inventário.
     * PUT /api/marketplace/artigos/{id}/estado/{novoEstadoId}
     */
    @PutMapping("/artigos/{id}/estado/{novoEstadoId}")
    public ResponseEntity<Void> alterarEstado(
            @PathVariable String id,
            @PathVariable Integer novoEstadoId
    ) {
        try {
            marketplaceService.alterarEstadoArtigo(id, novoEstadoId);
            return ResponseEntity.ok().build();

        } catch (NoSuchElementException e) {
            logger.warn("Artigo não encontrado para alterar estado, id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Estado inválido '{}' para artigo com id {}: {}", novoEstadoId, id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao alterar estado do artigo com id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/importar-inventario", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importarDoInventario(
            @ModelAttribute ConversaoInventarioRequest request
    ) {
        try {

            marketplaceService.converterUnidadeParaMarketplace(request, Utils.getAuthenticatedUserId());
            return ResponseEntity.ok("Artigo importado com sucesso!");

        } catch (NoSuchElementException e) {
            logger.warn("Unidade não encontrada para importar para marketplace: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unidade de inventário não encontrada.");
        } catch (IllegalArgumentException e) {
            logger.warn("Dados inválidos ao importar do inventário: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Pedido inválido: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Erro ao importar do inventário: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao importar artigo.");
        }
    }
}