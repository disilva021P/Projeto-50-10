package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

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

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ArtigoRepository artigoRepository;
    private final UtilizadoreRepository utilizadoreRepository;

    @Transactional
    public void realizarTransacao(TransacaoRequest request) {
        // 1. Buscar o Artigo
        Artigo artigo = artigoRepository.findById(request.artigoId())
                .orElseThrow(() -> new RuntimeException("Artigo não encontrado"));

        // 2. Verificar se já está arquivado (Boolean check)
        if (Boolean.TRUE.equals(artigo.getArquivado())) {
            throw new RuntimeException("Este artigo já não está disponível.");
        }

        // 3. Buscar Comprador
        Utilizadore comprador = utilizadoreRepository.findById(request.compradorId())
                .orElseThrow(() -> new RuntimeException("Comprador não encontrado"));

        // 4. Criar a Transação
        Transacao t = new Transacao();
        t.setArtigo(artigo);
        t.setComprador(comprador);

        // CORREÇÃO: Usar o nome exato do campo na tua entidade Artigo
        t.setVendedor(artigo.getDonoUtilizador());

        t.setTipo(request.tipo());
        t.setValorFinal(request.valorFinal());
        t.setDataInicio(request.dataInicio());
        t.setDataFimPrevista(request.dataFimPrevista());

        transacaoRepository.save(t);

        // 5. CORREÇÃO: Setar como true (Boolean) e não 1 (int)
        artigo.setArquivado(true);
        artigoRepository.save(artigo);
    }
}
