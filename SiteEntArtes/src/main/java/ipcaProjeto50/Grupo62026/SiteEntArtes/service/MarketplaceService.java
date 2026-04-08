package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ArtigoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ImagensUnidadeRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.InventarioUnidadeRepository; // Adicionado
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@RequiredArgsConstructor
@Service
public class MarketplaceService {

    private final ArtigoRepository artigoRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final InventarioUnidadeRepository inventarioUnidadeRepository;
    private final ImagensUnidadeRepository imagensUnidadeRepository;
    private final IdHasher idHasher;
    private final jakarta.persistence.EntityManager entityManager;

    public Page<ArtigoDto> filtrarArtigos(Integer tipo, String tam, String cor, String cond, Double min, Double max, Pageable pageable) {
        return artigoRepository.filtrarMarketplace(tipo, tam, cor, cond, min, max, pageable)
                .map(this::toDto);
    }

    public Page<ArtigoDto> listarArtigos(Pageable pageable) {
        return artigoRepository.findByArquivadoFalse(pageable)
                .map(this::toDto);
    }

    @Transactional
    public ArtigoDto inserirArtigo(ArtigoRequest request, MultipartFile imagem, String userId) throws IOException {
        // 1. Encontrar o Dono
        Utilizadore dono = utilizadoreRepository.findById(idHasher.decode(userId))
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        // 2. Criar e Guardar o ARTIGO
        Artigo artigo = new Artigo();
        artigo.setNome(request.nome());
        artigo.setDescricao(request.descricao());
        artigo.setTamanho(request.tamanho());
        artigo.setCor(request.cor());
        artigo.setCondicao(request.condicao());
        artigo.setTipoNegocio(request.tipoNegocio());
        artigo.setPreco(request.preco());
        artigo.setDonoUtilizador(dono);
        artigo.setCriadoEm(Instant.now());
        artigo.setArquivado(false);

        Artigo artigoGuardado = artigoRepository.save(artigo);

        // 3. Criar e Guardar a UNIDADE
        InventarioUnidade unidade = new InventarioUnidade();
        unidade.setArtigo(artigoGuardado);
        unidade.setDisponivel(true);
        unidade.setCriadoEm(Instant.now());

// Em vez de setEstadoUnidade(2), fazes isto:
        unidade.setEstado(entityManager.getReference(EstadoUnidade.class, 2));

        InventarioUnidade unidadeGuardada = inventarioUnidadeRepository.save(unidade);

        // 4. Criar e Guardar a IMAGEM ligada à UNIDADE
        if (imagem != null && !imagem.isEmpty()) {
            ImagensUnidade imgEntity = new ImagensUnidade();
            imgEntity.setUnidadeId(unidadeGuardada.getId());
            imgEntity.setUrlImagem(imagem.getBytes());

            imagensUnidadeRepository.save(imgEntity);
        }

        return toDto(artigoGuardado);
    }

    private ArtigoDto toDto(Artigo artigo) {
        InventarioUnidade unidade = null;

        if (artigo.getUnidades() != null) {
            unidade = artigo.getUnidades().stream()
                    .filter(InventarioUnidade::getDisponivel)
                    .findFirst()
                    .orElse(null);
        }

        Integer estadoId = (unidade != null && unidade.getEstado() != null) ? unidade.getEstado().getId() : null;
        String estadoNome = (unidade != null && unidade.getEstado() != null) ? unidade.getEstado().getEstado() : null;

        // 5. Procurar o ID da imagem associada à unidade para o DTO
        Integer imagemId = null;
        if (unidade != null) {
            imagemId = imagensUnidadeRepository.findFirstByUnidadeId(unidade.getId())
                    .map(ImagensUnidade::getId)
                    .orElse(null);
        }

        return new ArtigoDto(
                artigo.getId(),
                artigo.getNome(),
                artigo.getDescricao(),
                artigo.getTamanho(),
                artigo.getCor(),
                artigo.getCondicao(),
                artigo.getDonoUtilizador().getNome(),
                artigo.getTipoNegocio(),
                artigo.getPreco(),
                artigo.getCriadoEm(),
                estadoId,
                estadoNome,
                imagemId
        );
    }
}