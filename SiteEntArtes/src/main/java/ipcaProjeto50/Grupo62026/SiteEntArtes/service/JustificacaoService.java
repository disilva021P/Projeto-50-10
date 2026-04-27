package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher; // Garante que o import está correto
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.FaltaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Cancelamento;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.JustificacaoFalta;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.CancelamentoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.JustificacaoFaltaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class JustificacaoService {
    private final JustificacaoFaltaRepository justificacaoFaltaRepository;
    private final CancelamentoRepository cancelamentoRepository;
    private final IdHasher idHasher; // Injetar o Hasher
    private final UtilizadorService utilizadorService;
    private final NotificacoesService notificacoesService;

    @Transactional // Recomendado: Garante que se o PDF falhar, o motivo não é guardado (e vice-versa)
    public void submeterJustificacao(String faltaIdHash, byte[] pdfData, String motivoEncarregado) throws Exception {
        // 1. Descodificar o ID
        Integer idReal = idHasher.decode(faltaIdHash);

        // 2. Ir buscar o cancelamento que o professor criou
        Cancelamento falta = cancelamentoRepository.findById(idReal)
                .orElseThrow(() -> new RuntimeException("Falta não encontrada"));

        // 3. O encarregado detalha o motivo
        falta.setMotivo(motivoEncarregado);
        cancelamentoRepository.save(falta);

        // 4. Guardar o ficheiro PDF
        JustificacaoFalta jf = new JustificacaoFalta();
        jf.setIdfalta(falta);
        jf.setJustificacaoPdf(pdfData);
        for(Utilizadore utilizadore : utilizadorService.findAllCoordenacao()){
            notificacoesService.criarNotificacao(
                    utilizadore.getId(),
                    falta.getUtilizador().getId(),
                    "Justificação de falta submetida! ",
                    "A justificação para a aula de coaching de " + falta.getAula().getDataAula() +
                            " (" + falta.getAula().getHoraInicio() + " - " + falta.getAula().getHoraFim() +
                            ") foi indeferida pelo professor " + falta.getMarcardo_por().getNome() + ".", // Mensagem alterada
                    "USTIFICACAO SUBMETIDA",
                    idHasher.encode( falta.getId())
            );
        }
        justificacaoFaltaRepository.save(jf);
    }
    public void validarFalta(String faltaIdHash, boolean aprovada, String idAprovado_por) throws Exception {
        // 1. Descodificar o ID
        Integer idReal = idHasher.decode(faltaIdHash);

        Cancelamento falta = cancelamentoRepository.findById(idReal)
                .orElseThrow(() -> new RuntimeException("Falta não encontrada"));

        falta.setJustificado(aprovada);
        falta.setJustificadoEm(Instant.now());
        if(aprovada){
            notificacoesService.criarNotificacao(
                    falta.getUtilizador().getId(),
                    idHasher.decode(idAprovado_por),
                    "Justificação de falta validada! ",
                    "A sua justificação para a aula de coaching de " + falta.getAula().getDataAula() +
                            " (" + falta.getAula().getHoraInicio() + " - " + falta.getAula().getHoraFim() +
                            ") foi aprovada pela coordenação.", // Mensagem alterada
                    "JUSTIFICACAO ACEITE ",
                    idHasher.encode( falta.getId())
            );
        }else{
            notificacoesService.criarNotificacao(
                    falta.getUtilizador().getId(),
                    idHasher.decode(idAprovado_por),
                    "Justificação de falta validada! ",
                    "A sua justificação para a aula de coaching de " + falta.getAula().getDataAula() +
                            " (" + falta.getAula().getHoraInicio() + " - " + falta.getAula().getHoraFim() +
                            ") foi negada pela coordenação.", // Mensagem alterada
                    "JUSTIFICACAO NEGADA ",
                    idHasher.encode( falta.getId())
            );
        }

        cancelamentoRepository.save(falta);
    }

    public byte[] verConteudoPdf(String faltaIdHash) {
        // 1. Descodificar o ID
        Integer idReal = idHasher.decode(faltaIdHash);

        // Procura a justificação associada àquela falta usando o ID real
        JustificacaoFalta jf = justificacaoFaltaRepository.findByIdfalta_Id(idReal)
                .orElseThrow(() -> new RuntimeException("PDF não encontrado para a falta: " + idReal));

        return jf.getJustificacaoPdf();
    }
}