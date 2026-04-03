package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Artigo;
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

    public Page<ArtigoDto> listarArtigosPorTipo(Integer tipo, Pageable pageable) {
        return artigoRepository.findByArquivadoFalseAndParaVenda(tipo, pageable)
                .map(this::toDto);
    }

    private ArtigoDto toDto(Artigo artigo) {
        return new ArtigoDto(
                artigo.getId(),
                artigo.getNome(),
                artigo.getDescricao(),
                artigo.getTamanho(),
                artigo.getDonoUtilizador().getNome(),
                artigo.getParaVenda(),
                artigo.getPreco(),
                artigo.getCriadoEm()
        );
    }
}