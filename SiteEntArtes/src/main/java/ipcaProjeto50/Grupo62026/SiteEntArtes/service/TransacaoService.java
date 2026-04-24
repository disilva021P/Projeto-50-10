package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.TransacaoRequest;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.TransacaoResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ArtigoRepository artigoRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final NotificacoesService notificacoesService;
    private final IdHasher idHasher;
    private final PagamentoRepository pagamentoRepository;
    private final TipoPagamentoRepository tipoPagamentoRepository;

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

        // VALIDAÇÃO: Aluguer obriga a data de fim
        if ("ALUGUER".equalsIgnoreCase(request.tipo()) && request.dataFimPrevista() == null) {
            throw new IllegalArgumentException("Data de fim prevista é obrigatória para alugueres.");
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

        String tipoUpper = request.tipo().toUpperCase();
        if ("VENDA".equals(tipoUpper) || "ALUGUER".equals(tipoUpper)) {
            TipoPagamento tipoMaterial = tipoPagamentoRepository.findById(5)
                    .orElseThrow(() -> new RuntimeException("Tipo de pagamento 'Material' não encontrado"));

            Pagamento pagamento = new Pagamento();
            pagamento.setIdutilizador(comprador);
            pagamento.setValorPagamento(request.valorFinal());
            pagamento.setIdTipoPagamento(tipoMaterial);
            pagamento.setPago(false);
            pagamento.setDataPagamento(LocalDate.now());
            pagamento.setDescricao(
                    ("VENDA".equals(tipoUpper) ? "Compra" : "Aluguer") +
                            " do artigo '" + artigo.getNome() + "'"
            );
            pagamento.setAula(null); // Não está associado a uma aula

            pagamentoRepository.save(pagamento);
        }

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

    public List<TransacaoResumoDto> listarAlugueresAtivos(String compradorIdHash) {
        Integer idReal = idHasher.decode(compradorIdHash);
        return transacaoRepository.findAlugueresAtivosByComprador(idReal)
                .stream()
                .map(this::toResumoDto)
                .toList();
    }

    @Transactional
    public void devolverArtigo(String transacaoIdHash) {
        Integer idReal = idHasher.decode(transacaoIdHash);
        Transacao t = transacaoRepository.findById(idReal)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));

        if (!"ALUGUER".equals(t.getTipo())) {
            throw new IllegalArgumentException("Só é possível devolver alugueres.");
        }
        if (t.getDataDevolucaoReal() != null) {
            throw new IllegalStateException("Este artigo já foi devolvido.");
        }

        t.setDataDevolucaoReal(LocalDate.now());
        t.setEstadoTransacao("DEVOLVIDA");

        transacaoRepository.save(t);
    }

    private TransacaoResumoDto toResumoDto(Transacao t) {
        return new TransacaoResumoDto(
                idHasher.encode(t.getId()),
                t.getArtigo().getNome(),
                t.getArtigo().getDescricao(),
                idHasher.encode(t.getArtigo().getId()),
                t.getDataInicio(),
                t.getDataFimPrevista(),
                t.getValorFinal()
        );
    }
}