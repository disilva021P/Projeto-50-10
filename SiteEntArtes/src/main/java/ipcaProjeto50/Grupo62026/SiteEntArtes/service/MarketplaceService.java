package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ArtigoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.ConversaoInventarioRequest;
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
    private final InventarioUnidadeRepository unidadeRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final InventarioUnidadeRepository inventarioUnidadeRepository;
    private final ImagensUnidadeRepository imagensUnidadeRepository;
    private final IdHasher idHasher;
    private final jakarta.persistence.EntityManager entityManager;

    public Page<ArtigoDto> filtrarArtigos(String nome, Integer tipo, String tam, String cor, String cond, Double min, Double max, Integer donoId, Pageable pageable) {
        return artigoRepository.filtrarMarketplace(nome, tipo, tam, cor, cond, min, max, donoId, pageable)
                .map(this::toDto);
    }

    public Page<ArtigoDto> listarArtigos(Pageable pageable) {
        return artigoRepository.findByArquivadoFalse(pageable)
                .map(this::toDto);
    }

    public List<ArtigoDto> listarArtigosPendentes() {
        return artigoRepository.findPendentesParaCoordenacao()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * IMPORTANTE: Como já não há relação direta via BD, aqui controlamos apenas o Artigo.
     * Quando um artigo é aceite como doação, ele deve ser "clonado" para a tabela de inventário.
     */
    @Transactional
    public void alterarEstadoArtigo(Integer artigoId, Integer novoEstadoId) {
        Artigo artigo = artigoRepository.findById(artigoId)
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado"));

        // CENÁRIO 1: Recusado ou Removido (Estado 5)
        if (novoEstadoId == 5) {
            artigo.setAprovado(false);
            artigo.setArquivado(true); // Sai da lista de pendentes e não aparece no marketplace
            artigoRepository.save(artigo);
        }

        // CENÁRIO 2: Aceite para o Marketplace Público (Estado 2)
        // O utilizador doou e a escola quer que outros alunos possam ficar com ele
        else if (novoEstadoId == 2) {
            artigo.setAprovado(true);  // Agora passa no filtro 'aprovado = true'
            artigo.setArquivado(false); // Garante que está visível
            artigoRepository.save(artigo);
        }

        // CENÁRIO 3: Aceite para Inventário Interno da Escola (Estado 9)
        // A escola decide ficar com o item para o seu próprio stock
        else if (novoEstadoId == 9) {
            // 1. Atualizamos o artigo original
            artigo.setAprovado(true);
            artigo.setArquivado(true); // Arquivamos no Marketplace (já não está para "doação pública")
            artigoRepository.save(artigo);

            // 2. Criamos a unidade real na tabela de inventário independente
            InventarioUnidade novaUnidade = new InventarioUnidade();
            novaUnidade.setNome(artigo.getNome());
            novaUnidade.setDescricao(artigo.getDescricao());
            novaUnidade.setEstado(entityManager.getReference(EstadoUnidade.class, 9));
            novaUnidade.setDisponivel(true);
            novaUnidade.setLocalizacao("Armazém de Doações");
            novaUnidade.setCriadoEm(Instant.now());

            inventarioUnidadeRepository.save(novaUnidade);
        }
    }

    @Transactional
    public ArtigoDto inserirArtigo(ArtigoRequest request, List<MultipartFile> imagens, String identifier) throws IOException {
        Utilizadore dono;
        if (identifier.contains("@")) {
            dono = utilizadoreRepository.findByEmail(identifier)
                    .orElseThrow(() -> new RuntimeException("Utilizador nao encontrado: " + identifier));
        } else {
            dono = utilizadoreRepository.findById(idHasher.decode(identifier))
                    .orElseThrow(() -> new RuntimeException("Utilizador nao encontrado: " + identifier));
        }

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
        artigo.setIsDoacao(request.isDoacao());

        // REGRA: Se for doação, precisa de aprovação. Se for venda/aluguer, entra direto.
        if (Boolean.TRUE.equals(request.isDoacao())) {
            artigo.setAprovado(false);
        } else {
            artigo.setAprovado(true);
        }

        Artigo artigoGuardado = artigoRepository.save(artigo);

        // Se for doação, ele fica apenas na tabela Artigos com isDoacao=true.
        // A unidade de inventário só será criada em 'alterarEstadoArtigo' quando a coordenação aceitar.

        if (imagens != null) {
            for (MultipartFile imagem : imagens) {
                if (imagem != null && !imagem.isEmpty()) {
                    ImagensUnidade imgEntity = new ImagensUnidade();
                    imgEntity.setArtigoId(artigoGuardado.getId());
                    imgEntity.setUrlImagem(imagem.getBytes());
                    imagensUnidadeRepository.save(imgEntity);
                }
            }
        }



        return toDto(artigoGuardado);
    }

    @Transactional
    public void arquivarArtigo(Integer artigoId) {
        Artigo artigo = artigoRepository.findById(artigoId)
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado"));
        artigo.setArquivado(true);
        artigoRepository.save(artigo);
    }

    @Transactional
    public ArtigoDto editarArtigo(Integer id, ArtigoRequest request) {
        Artigo artigo = artigoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado"));

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

        if (request.imagens() != null) {
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

    @Transactional
    public void removerImagem(Integer imagemId) {
        imagensUnidadeRepository.deleteById(imagemId);
    }

    private ArtigoDto toDto(Artigo artigo) {
        // Como removemos a relação direta, o estado no Marketplace para artigos novos
        // ou de venda/aluguer pode ser fixo como "2" (Publicado) se não estiver arquivado.

        Integer estadoId = 2;
        String estadoNome = "Publicado";

        // Se o artigo for doação e estiver arquivado, podemos assumir que já foi processado
        if (Boolean.TRUE.equals(artigo.getArquivado())) {
            estadoId = 5;
            estadoNome = "Arquivado/Removido";
        }

        List<ImagensUnidade> imagens = imagensUnidadeRepository.findByArtigoId(artigo.getId());
        List<Integer> todosImagemIds = imagens.stream().map(ImagensUnidade::getId).toList();
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

    @Transactional
    public void converterUnidadeParaMarketplace(ConversaoInventarioRequest request, Integer coordenadorId) {
        // 1. Verificar se a unidade existe (Usa getUnidadeId)
        var unidade = unidadeRepository.findById(request.getUnidadeId())
                .orElseThrow(() -> new RuntimeException("Item de inventário não encontrado."));

        // 2. Criar o novo Artigo no Marketplace (Usa os Getters)
        Artigo novoArtigo = new Artigo();
        novoArtigo.setNome(request.getNome());
        novoArtigo.setDescricao(request.getDescricao());
        novoArtigo.setTamanho(request.getTamanho());
        novoArtigo.setCor(request.getCor());
        novoArtigo.setCondicao(request.getCondicao());

        // Configurações de negócio
        novoArtigo.setIsVenda(request.getIsVenda());
        novoArtigo.setIsAluguer(request.getIsAluguer());
        novoArtigo.setIsDoacao(request.getIsDoacao());
        novoArtigo.setPrecoVenda(request.getPrecoVenda());
        novoArtigo.setPrecoAluguer(request.getPrecoAluguer());

        novoArtigo.setArquivado(false);
        novoArtigo.setAprovado(true);
        novoArtigo.setCriadoEm(Instant.now());

        Utilizadore dono = utilizadoreRepository.findById(coordenadorId)
                .orElseThrow(() -> new RuntimeException("Coordenador não encontrado."));
        novoArtigo.setDonoUtilizador(dono);

        artigoRepository.save(novoArtigo);

        // 3. Salvar o Artigo para gerar o ID
        Artigo artigoSalvo = artigoRepository.save(novoArtigo);

        // 4. Processar e salvar as imagens que vieram no request (CORREÇÃO AQUI)
        if (request.getImagens() != null) {
            for (MultipartFile imagem : request.getImagens()) {
                if (imagem != null && !imagem.isEmpty()) {
                    try {
                        ImagensUnidade imgEntity = new ImagensUnidade();
                        imgEntity.setArtigoId(artigoSalvo.getId());
                        imgEntity.setUrlImagem(imagem.getBytes()); // Converte o ficheiro para bytes
                        imagensUnidadeRepository.save(imgEntity);
                    } catch (IOException e) {
                        throw new RuntimeException("Erro ao processar imagem vinda do inventário", e);
                    }
                }
            }
        }

        unidadeRepository.delete(unidade);
    }
}