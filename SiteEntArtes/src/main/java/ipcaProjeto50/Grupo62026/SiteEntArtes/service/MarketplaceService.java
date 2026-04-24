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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final NotificacoesService notificacoesService;

    public Page<ArtigoDto> filtrarArtigos(String nome, Integer tipo, String tam, String cor, String cond, Double min, Double max, String donoIdHash, Pageable pageable) {
        Integer donoIdOriginal = null;
        if (donoIdHash != null && !donoIdHash.isEmpty()) {
            donoIdOriginal = idHasher.decode(donoIdHash);
        }

        return artigoRepository.filtrarMarketplace(nome, tipo, tam, cor, cond, min, max, donoIdOriginal, pageable
        ).map(this::toDto);
    }

    public Page<ArtigoDto> listarArtigos(int page, int size, String nome, Integer tipo, String donoIdHash) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("criadoEm").descending());
        return filtrarArtigos(nome, tipo, null, null, null, null, null, donoIdHash, pageable);
    }

    public List<ArtigoDto> listarArtigosPendentes() {
        return artigoRepository.findPendentesParaCoordenacao()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void alterarEstadoArtigo(String idHash, Integer novoEstadoId) throws Exception {
        Integer idReal = idHasher.decode(idHash);
        Artigo artigo = artigoRepository.findById(idReal)
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado"));

        String mensagemNotif = "";

        // CENÁRIO 1: Recusado ou Removido (Estado 5)
        if (novoEstadoId == 5) {
            artigo.setAprovado(false);
            artigo.setArquivado(true); // Sai da lista de pendentes e não aparece no marketplace
            artigoRepository.save(artigo);
            mensagemNotif = "O seu artigo '" + artigo.getNome() + "' foi recusado pela coordenação.";
        }

        // CENÁRIO 2: Aceite para o Marketplace Público (Estado 2)
        // O utilizador doou e a escola quer que outros alunos possam ficar com ele
        else if (novoEstadoId == 2) {
            artigo.setAprovado(true);  // Agora passa no filtro 'aprovado = true'
            artigo.setArquivado(false); // Garante que está visível
            artigoRepository.save(artigo);
            mensagemNotif = "O seu artigo '" + artigo.getNome() + "' foi aprovado!";
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

            mensagemNotif = "O seu artigo '" + artigo.getNome() + "' foi aceite e doado ao inventário da escola.";
        }

        if (!mensagemNotif.isEmpty()) {
            notificacoesService.criarNotificacao(
                    artigo.getDonoUtilizador().getId(),
                    null, // Remetente sistema (null porque é uma mensagem automática)
                    "Estado do Artigo Atualizado",
                    mensagemNotif,
                    "MARKETPLACE_STATUS",
                    artigo.getId().toString()
            );
        }

    }

    @Transactional
    public ArtigoDto inserirArtigo(ArtigoRequest request, List<MultipartFile> imagens, String identifier) throws Exception {
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

        enviarNotificacaoNovoArtigo(artigoGuardado);

        return toDto(artigoGuardado);
    }

    @Transactional
    public void arquivarArtigo(String idHashed) {
        Integer idOriginal = idHasher.decode(idHashed);
        Artigo artigo = artigoRepository.findById(idOriginal)
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado"));
        artigo.setArquivado(true);
        artigoRepository.save(artigo);
    }

    @Transactional
    public ArtigoDto editarArtigo(String idHashed, ArtigoRequest request) {
        Integer idOriginal = idHasher.decode(idHashed);

        Artigo artigo = artigoRepository.findById(idOriginal)
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
    public void removerImagem(String imagemIdHashed) {
        Integer idOriginal = idHasher.decode(imagemIdHashed);
        imagensUnidadeRepository.deleteById(idOriginal);
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
        // Codificamos todos os IDs das imagens
        List<String> todosImagemIdsHashed = imagens.stream()
                .map(img -> idHasher.encode(img.getId()))
                .toList();

        String imagemPrincipalIdHashed = todosImagemIdsHashed.isEmpty() ? null : todosImagemIdsHashed.get(todosImagemIdsHashed.size() - 1);

        return new ArtigoDto(
                idHasher.encode(artigo.getId()), // Encode do ID do Artigo
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
                imagemPrincipalIdHashed,
                todosImagemIdsHashed
        );
    }

    @Transactional
    public void converterUnidadeParaMarketplace(ConversaoInventarioRequest request, String emailCoordenador) {
        // 1. Verificar se a unidade existe
        var unidade = unidadeRepository.findById(request.getUnidadeId())
                .orElseThrow(() -> new RuntimeException("Item de inventário não encontrado."));

        // 2. Buscar o coordenador pelo email (vem do JWT)
        Utilizadore dono = utilizadoreRepository.findByEmail(emailCoordenador)
                .orElseThrow(() -> new RuntimeException("Coordenador não encontrado: " + emailCoordenador));

        // 3. Criar o novo Artigo
        Artigo novoArtigo = new Artigo();
        novoArtigo.setNome(request.getNome());
        novoArtigo.setDescricao(request.getDescricao());
        novoArtigo.setTamanho(request.getTamanho());
        novoArtigo.setCor(request.getCor());
        novoArtigo.setCondicao(request.getCondicao());
        novoArtigo.setIsVenda(request.getIsVenda());
        novoArtigo.setIsAluguer(request.getIsAluguer());
        novoArtigo.setIsDoacao(request.getIsDoacao());
        novoArtigo.setPrecoVenda(request.getPrecoVenda());
        novoArtigo.setPrecoAluguer(request.getPrecoAluguer());
        novoArtigo.setArquivado(false);
        novoArtigo.setAprovado(true);
        novoArtigo.setCriadoEm(Instant.now());
        novoArtigo.setDonoUtilizador(dono);

        // 4. Salvar
        Artigo artigoSalvo = artigoRepository.save(novoArtigo);

        // 5. Processar Imagens
        if (request.getImagens() != null) {
            for (MultipartFile imagem : request.getImagens()) {
                if (imagem != null && !imagem.isEmpty()) {
                    try {
                        ImagensUnidade imgEntity = new ImagensUnidade();
                        imgEntity.setArtigoId(artigoSalvo.getId());
                        imgEntity.setUrlImagem(imagem.getBytes());
                        imagensUnidadeRepository.save(imgEntity);
                    } catch (IOException e) {
                        throw new RuntimeException("Erro ao processar imagem", e);
                    }
                }
            }
        }

        // 6. Remover do inventário antigo
        unidadeRepository.delete(unidade);
    }

    private void enviarNotificacaoNovoArtigo(Artigo artigo) throws Exception {
        // 1. Procurar todos os coordenadores (ajusta a string se no banco for diferente, ex: "COORDENADOR")
        List<Utilizadore> coordenadores = utilizadoreRepository.findByTipo_TipoUtilizador("COORDENACAO");

        String titulo = Boolean.TRUE.equals(artigo.getIsDoacao()) ? "Nova Doação Pendente" : "Novo Artigo no Marketplace";
        String mensagem = "O utilizador " + artigo.getDonoUtilizador().getNome() + " inseriu o artigo: " + artigo.getNome();
        String tipo = Boolean.TRUE.equals(artigo.getIsDoacao()) ? "MARKETPLACE_PENDENTE" : "MARKETPLACE_NOVO";

        for (Utilizadore coord : coordenadores) {
            notificacoesService.criarNotificacao(
                    coord.getId(),
                    artigo.getDonoUtilizador().getId(),
                    titulo,
                    mensagem,
                    tipo,
                    artigo.getId().toString() // Referência para o ID do artigo
            );
        }
    }
}