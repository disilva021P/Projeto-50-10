package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.TransacaoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Artigo;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Transacao;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.ArtigoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.TransacaoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ArtigoRepository artigoRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final NotificacoesService notificacoesService;
    private final IdHasher idHasher;

    @Transactional
    public void realizarTransacao(TransacaoRequest request) throws Exception {
        // 1. Buscar o Artigo
        Integer idRealArtigo = idHasher.decode(request.artigoId());

        Artigo artigo = artigoRepository.findById(idRealArtigo)
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado"));

        // 2. Verificar se já está arquivado (Boolean check)
        if (Boolean.TRUE.equals(artigo.getArquivado())) {
            throw new RuntimeException("Este artigo já não está disponível.");
        }

        // 3. Descodificar o ID do Comprador
        Integer idRealComprador = idHasher.decode(request.compradorId());
        Utilizadore comprador = utilizadoreRepository.findById(idRealComprador)
                .orElseThrow(() -> new RuntimeException("Comprador não encontrado"));

        // 4. Criar a Transação normalmente usando o objeto 'comprador'
        Transacao t = new Transacao();
        t.setArtigo(artigo);
        t.setComprador(comprador);
        t.setVendedor(artigo.getDonoUtilizador());
        t.setTipo(request.tipo());
        t.setValorFinal(request.valorFinal());
        t.setDataInicio(request.dataInicio());
        t.setDataFimPrevista(request.dataFimPrevista());

        transacaoRepository.save(t);

        // 5. Arquivar o artigo
        artigo.setArquivado(true);
        artigoRepository.save(artigo);

        // 6. ENVIAR NOTIFICAÇÃO AO PROPRIETÁRIO (Vendedor)
        enviarNotificacaoVenda(t);
    }

    private void enviarNotificacaoVenda(Transacao t) throws Exception {
        String tipoAcao = t.getTipo().toUpperCase();
        String titulo = "";
        String mensagem = "";
        Integer destinatarioId;

        // REGRA: Se for doação, o destinatário é a coordenação.
        // Caso contrário (Venda/Aluguer), é o vendedor original.
        if ("DOACAO".equals(tipoAcao)) {
            // 1. O destinatário será a coordenação (procuramos o primeiro ou todos)
            List<Utilizadore> coordenadores = utilizadoreRepository.findByTipo_TipoUtilizador("COORDENACAO");

            titulo = "Doação Levantada!";
            mensagem = "O item '" + t.getArtigo().getNome() + "' foi levantado/doado a " + t.getComprador().getNome() + ".";

            // Enviamos para todos os coordenadores
            for (Utilizadore coord : coordenadores) {
                notificacoesService.criarNotificacao(
                        coord.getId(),
                        t.getComprador().getId(),
                        titulo,
                        mensagem,
                        "MARKETPLACE_TRANSACAO",
                        t.getArtigo().getId().toString()
                );
            }
            return;
        }

        // CENÁRIO NORMAL: Venda ou Aluguer (Notifica o proprietário original)
        destinatarioId = t.getVendedor().getId();

        if ("VENDA".equals(tipoAcao)) {
            titulo = "Artigo Vendido!";
            mensagem = "O seu artigo '" + t.getArtigo().getNome() + "' foi comprado por " + t.getComprador().getNome() + ".";
        } else {
            titulo = "Novo Aluguer!";
            mensagem = "O seu artigo '" + t.getArtigo().getNome() + "' foi alugado por " + t.getComprador().getNome() + ".";
        }

        notificacoesService.criarNotificacao(
                destinatarioId,
                t.getComprador().getId(),
                titulo,
                mensagem,
                "MARKETPLACE_TRANSACAO",
                t.getArtigo().getId().toString()
        );
    }
}