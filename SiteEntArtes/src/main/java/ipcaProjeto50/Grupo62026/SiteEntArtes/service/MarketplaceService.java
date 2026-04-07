package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Artigo;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.InventarioUnidade;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ArtigoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class MarketplaceService {

    private final ArtigoRepository artigoRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final IdHasher idHasher;

    public Page<ArtigoDto> listarArtigos(Pageable pageable) {
        return artigoRepository.findByArquivadoFalse(pageable)
                .map(this::toDto);
    }

    public Page<ArtigoDto> listarArtigosPorEstado(Integer estadoId, Pageable pageable) {
        return artigoRepository.findByArquivadoFalseAndEstadoUnidade(estadoId, pageable)
                .map(this::toDto);
    }

    @Transactional
    public ArtigoDto inserirArtigo(ArtigoRequest request, String id) {
        Utilizadore dono = utilizadoreRepository.findById(idHasher.decode(id) )
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        Artigo artigo = new Artigo();
        artigo.setNome(request.nome());
        artigo.setDescricao(request.descricao());
        artigo.setTamanho(request.tamanho());
        artigo.setParaVenda(request.paraVenda());
        artigo.setPreco(request.preco());
        artigo.setArquivado(false);
        artigo.setDonoUtilizador(dono);
        artigo.setCriadoEm(Instant.now());

        Artigo guardado = artigoRepository.save(artigo);
        return toDto(guardado);
    }

    private ArtigoDto toDto(Artigo artigo) {
        // Vai buscar a primeira unidade disponível, se existir
        InventarioUnidade unidade = null;

        if (artigo.getUnidades() != null) {
            unidade = artigo.getUnidades().stream()
                    .filter(InventarioUnidade::getDisponivel)
                    .findFirst()
                    .orElse(null);
        }

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