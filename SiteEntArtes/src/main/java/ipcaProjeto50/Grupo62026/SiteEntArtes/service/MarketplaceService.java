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
    public Page<ArtigoDto> filtrarArtigos(String nome, Integer tipo, String tam, String cor, String cond, Double min, Double max, Integer donoId, Pageable pageable) {
        return artigoRepository.filtrarMarketplace(nome, tipo, tam, cor, cond, min, max, donoId, pageable)
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
     * Altera o estado de todas as unidades de um artigo (Doacao de artigo, coordencacao aceita/recusa)
     */
    @Transactional
    public void alterarEstadoArtigo(Integer artigoId, Integer novoEstadoId) {
        Artigo artigo = artigoRepository.findById(artigoId)
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado"));

        // 1. Se o estado for 5 (Removido/Recusado), arquivamos o artigo principal
        if (novoEstadoId == 5) {
            artigo.setArquivado(true);
            artigoRepository.save(artigo);
        }

        // Se for 9 (Inventário Interno), aceitamos, mas mantemos 'disponivel = false'
        // para não aparecer no marketplace público
        else if (novoEstadoId == 9) {
            artigo.setArquivado(false);
        }

        // 2. Atualizar as unidades
        artigo.getUnidades().forEach(unidade -> {
            // Define o novo estado (Ex: 2 para Aceite, 5 para Recusado)
            unidade.setEstado(entityManager.getReference(EstadoUnidade.class, novoEstadoId));

            // Lógica de Disponibilidade:
            if (novoEstadoId == 2) {
                // Se foi aceite/publicado, fica disponível (1) para aparecer no site
                unidade.setDisponivel(true);
            } else {
                // Para outros estados (como 5 - Removido), fica indisponível (0)
                unidade.setDisponivel(false);
            }

            inventarioUnidadeRepository.save(unidade);
        });
    }

    @Transactional
    public ArtigoDto inserirArtigo(ArtigoRequest request, List<MultipartFile> imagens, String identifier) throws IOException {
        // 1. Encontrar o Dono (Mantém igual)
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
        artigo.setIsVenda(request.isVenda());
        artigo.setIsAluguer(request.isAluguer());
        artigo.setIsDoacao(request.isDoacao());
        artigo.setPrecoVenda(request.precoVenda());
        artigo.setPrecoAluguer(request.precoAluguer());
        artigo.setDonoUtilizador(dono);
        artigo.setCriadoEm(Instant.now());
        artigo.setArquivado(false);

        Artigo artigoGuardado = artigoRepository.save(artigo);

        // 3. Lógica de Inventário (APENAS SE FOR DOAÇÃO)
        if (Boolean.TRUE.equals(request.isDoacao())) {
            InventarioUnidade unidade = new InventarioUnidade();
            unidade.setArtigo(artigoGuardado);
            unidade.setDisponivel(false); // Fica falso até a coordenação aprovar
            unidade.setCriadoEm(Instant.now());

            // Estado 8 = Pendente
            unidade.setEstado(entityManager.getReference(EstadoUnidade.class, 8));

            inventarioUnidadeRepository.save(unidade);
        }

        // 4. Guardar as IMAGENS (Ligadas diretamente ao ARTIGO)
        if (imagens != null) {
            for (MultipartFile imagem : imagens) {
                // MUITO IMPORTANTE: Verificar se não está vazio
                if (imagem != null && !imagem.isEmpty() && imagem.getSize() > 0) {
                    ImagensUnidade imgEntity = new ImagensUnidade();
                    imgEntity.setArtigoId(artigoGuardado.getId());
                    imgEntity.setUrlImagem(imagem.getBytes());
                    imagensUnidadeRepository.save(imgEntity);
                }
            }
        }

        return toDto(artigoGuardado);
    }

    /**
     * Arquiva um artigo (Remoção lógica)
     */
    @Transactional
    public void arquivarArtigo(Integer artigoId) {
        Artigo artigo = artigoRepository.findById(artigoId)
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado"));

        // 1. Arquivar o artigo
        artigo.setArquivado(true);
        artigoRepository.save(artigo);

        // 2. Tornar todas as unidades associadas indisponíveis
        if (artigo.getUnidades() != null) {
            artigo.getUnidades().forEach(unidade -> {
                unidade.setDisponivel(false);
                inventarioUnidadeRepository.save(unidade);
            });
        }
    }

    /**
     * Edita os dados de um artigo existente e adiciona novas imagens
     */
    @Transactional
    public ArtigoDto editarArtigo(Integer id, ArtigoRequest request) {
        Artigo artigo = artigoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado"));

        // 1. Validar existência de imagens (Regra de Negócio)
        InventarioUnidade unidade = (artigo.getUnidades() != null && !artigo.getUnidades().isEmpty())
                ? artigo.getUnidades().getFirst()
                : null;

        // Contamos as imagens que já existem na BD
        long imagensExistentes = imagensUnidadeRepository.countByArtigoId(artigo.getId());

        // Contamos quantas imagens válidas estão a chegar no request
        long novasImagensValidas = (request.imagens() != null)
                ? request.imagens().stream().filter(img -> !img.isEmpty()).count()
                : 0;

        // Se o total for zero, bloqueamos a edição
        if (imagensExistentes == 0 && novasImagensValidas == 0) {
            throw new RuntimeException("O artigo deve ter pelo menos uma imagem.");
        }

        // 2. Guardar o estado anterior para comparar a doação
        boolean jaEraDoacao = artigo.getIsDoacao() != null && artigo.getIsDoacao();

        // 3. Atualizar campos básicos e lógica de negócio
        artigo.setNome(request.nome());
        artigo.setDescricao(request.descricao());
        artigo.setTamanho(request.tamanho());
        artigo.setCor(request.cor());
        artigo.setCondicao(request.condicao());
        artigo.setIsVenda(request.isVenda());
        artigo.setIsAluguer(request.isAluguer());
        artigo.setIsDoacao(request.isDoacao());
        artigo.setPrecoVenda(request.precoVenda());
        artigo.setPrecoAluguer(request.precoAluguer());

        // 4. Lógica de Aprovação de Doação
        if (request.isDoacao() != null && request.isDoacao() && !jaEraDoacao) {
            unidade.setEstado(entityManager.getReference(EstadoUnidade.class, 8));
            unidade.setDisponivel(false);
            inventarioUnidadeRepository.save(unidade);
        }

        // 5. Processar novas imagens
        if (novasImagensValidas > 0) {
            for (MultipartFile imagem : request.imagens()) {
                if (!imagem.isEmpty()) {
                    try {
                        ImagensUnidade imgEntity = new ImagensUnidade();
                        imgEntity.setArtigoId(artigo.getId());
                        imgEntity.setUrlImagem(imagem.getBytes());
                        imagensUnidadeRepository.save(imgEntity);
                    } catch (IOException e) {
                        throw new RuntimeException("Erro ao processar imagem", e);
                    }
                }
            }
        }

        Artigo atualizado = artigoRepository.save(artigo);
        return toDto(atualizado);
    }

    /**
     * Remove uma imagem específica permanentemente
     */
    @Transactional
    public void removerImagem(Integer imagemId) {
        if (imagensUnidadeRepository.existsById(imagemId)) {
            imagensUnidadeRepository.deleteById(imagemId);
        } else {
            throw new RuntimeException("Imagem não encontrada");
        }
    }

    private ArtigoDto toDto(Artigo artigo) {
        // 1. Estado (Mantém a tua lógica que está correta)
        InventarioUnidade unidade = null;
        if (artigo.getUnidades() != null && !artigo.getUnidades().isEmpty()) {
            unidade = artigo.getUnidades().stream()
                    .filter(u -> u.getDisponivel() != null && u.getDisponivel())
                    .findFirst()
                    .orElse(artigo.getUnidades().get(0));
        }

        Integer estadoId = (unidade != null && unidade.getEstado() != null)
                ? unidade.getEstado().getId() : 2;
        String estadoNome = (unidade != null && unidade.getEstado() != null)
                ? unidade.getEstado().getEstado() : "Publicado";

        // 2. Imagens (Independente da Unidade)
        List<ImagensUnidade> imagens = imagensUnidadeRepository.findByArtigoId(artigo.getId());

        List<Integer> todosImagemIds = imagens.stream()
                .map(ImagensUnidade::getId)
                .toList();

        // Ajuste de segurança para evitar NoSuchElementException
        Integer imagemPrincipalId = todosImagemIds.isEmpty() ? null : todosImagemIds.get(todosImagemIds.size() - 1);

        return new ArtigoDto(
                artigo.getId(),
                artigo.getNome(),
                artigo.getDescricao(),
                artigo.getTamanho(),
                artigo.getCor(),
                artigo.getCondicao(),
                idHasher.encode(artigo.getDonoUtilizador().getId()),
                artigo.getDonoUtilizador().getNome(),
                artigo.getIsVenda(),
                artigo.getIsAluguer(),
                artigo.getIsDoacao(),
                artigo.getPrecoVenda(),
                artigo.getPrecoAluguer(),
                artigo.getCriadoEm(),
                estadoId,
                estadoNome,
                imagemPrincipalId,
                todosImagemIds
        );
    }
}