package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ArtigoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ImagensUnidadeRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.InventarioUnidadeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MarketplaceService {

    private final ArtigoRepository artigoRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final InventarioUnidadeRepository inventarioUnidadeRepository;
    private final ImagensUnidadeRepository imagensUnidadeRepository;
    private final IdHasher idHasher;
    private final jakarta.persistence.EntityManager entityManager;

    /**
     * Filtra artigos com todos os critérios novos, incluindo o donoId para "Meus Artigos"
     */
    public Page<ArtigoDto> filtrarArtigos(Integer tipo, String tam, String cor, String cond, Double min, Double max, Integer donoId, Pageable pageable) {
        return artigoRepository.filtrarMarketplace(tipo, tam, cor, cond, min, max, donoId, pageable)
                .map(this::toDto);
    }

    public Page<ArtigoDto> listarArtigos(Pageable pageable) {
        return artigoRepository.findByArquivadoFalse(pageable)
                .map(this::toDto);
    }

    /**
     * Lista artigos para a coordenação (Estado 8)
     */
    public List<ArtigoDto> listarArtigosPendentes() {
        return artigoRepository.findPendentesParaCoordenacao()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Altera o estado de todas as unidades de um artigo
     */
    @Transactional
    public void alterarEstadoArtigo(Integer artigoId, Integer novoEstadoId) {
        Artigo artigo = artigoRepository.findById(artigoId)
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado"));

        // Se o estado for 5 (Removido/Recusado), marcamos o artigo como arquivado
        if (novoEstadoId == 5) {
            artigo.setArquivado(true);
            artigoRepository.save(artigo);
        }

        // Atualiza também o estado de todas as unidades para manter a consistência
        artigo.getUnidades().forEach(unidade -> {
            unidade.setEstado(entityManager.getReference(EstadoUnidade.class, novoEstadoId));
            unidade.setDisponivel(false); // Se foi removido, não está disponível
            inventarioUnidadeRepository.save(unidade);
        });
    }

    @Transactional
    public ArtigoDto inserirArtigo(ArtigoRequest request, MultipartFile imagem, String identifier) throws IOException {
        // 1. Encontrar o Dono
        Utilizadore dono;
        if (identifier.contains("@")) {
            dono = utilizadoreRepository.findByEmail(identifier)
                    .orElseThrow(() -> new RuntimeException("Utilizador nao encontrado (Email): " + identifier));
        } else {
            try {
                Integer idReal = idHasher.decode(identifier);
                dono = utilizadoreRepository.findById(idReal)
                        .orElseThrow(() -> new RuntimeException("Utilizador nao encontrado (ID): " + identifier));
            } catch (Exception e) {
                throw new RuntimeException("Erro ao descodificar ID do utilizador: " + identifier);
            }
        }

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

        // 3. Criar e Guardar a UNIDADE com lógica de Aprovação
        InventarioUnidade unidade = new InventarioUnidade();
        unidade.setArtigo(artigoGuardado);
        unidade.setDisponivel(true);
        unidade.setCriadoEm(Instant.now());

        // --- LÓGICA DE ESTADO DINÂMICO ---
        // Se tipoNegocio for 0 (Doação), entra como 8 (Pendente)
        // Se for outro (Venda/Aluguer), entra como 2 (Publicado) direto
        int estadoInicial = (request.tipoNegocio() == 0) ? 8 : 2;
        unidade.setEstado(entityManager.getReference(EstadoUnidade.class, estadoInicial));

        InventarioUnidade unidadeGuardada = inventarioUnidadeRepository.save(unidade);

        // 4. Criar e Guardar a IMAGEM (Mantém-se igual)
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
                    .filter(u -> u.getDisponivel() != null && u.getDisponivel())
                    .findFirst()
                    .orElse(null);
        }

        Integer estadoId = (unidade != null && unidade.getEstado() != null) ? unidade.getEstado().getId() : null;
        String estadoNome = (unidade != null && unidade.getEstado() != null) ? unidade.getEstado().getEstado() : null;

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