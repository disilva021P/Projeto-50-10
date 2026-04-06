package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Artigo;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.InventarioUnidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ArtigoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MarketplaceService {

    private final ArtigoRepository artigoRepository;

    public MarketplaceService(ArtigoRepository artigoRepository) {
        this.artigoRepository = artigoRepository;
    }

    public Page<ArtigoDto> listarArtigos(Pageable pageable) {
        return artigoRepository.findByArquivadoFalse(pageable)
                .map(this::toDto);
    }

    public Page<ArtigoDto> listarArtigosPorEstado(Integer estadoId, Pageable pageable) {
        return artigoRepository.findByArquivadoFalseAndEstadoUnidade(estadoId, pageable)
                .map(this::toDto);
    }

    private ArtigoDto toDto(Artigo artigo) {
        // Vai buscar a primeira unidade disponível, se existir
        InventarioUnidade unidade = artigo.getUnidades().stream()
                .filter(InventarioUnidade::getDisponivel)
                .findFirst()
                .orElse(null);

        Integer estadoId = unidade != null ? unidade.getEstado().getId() : null;
        String estadoNome = unidade != null ? unidade.getEstado().getEstado() : null;

        return new ArtigoDto(
                artigo.getId(),
                artigo.getNome(),
                artigo.getDescricao(),
                artigo.getTamanho(),
                artigo.getDonoUtilizador().getNome(),
                artigo.getParaVenda(),
                artigo.getPreco(),
                artigo.getCriadoEm(),
                estadoId,
                estadoNome
        );
    }
}