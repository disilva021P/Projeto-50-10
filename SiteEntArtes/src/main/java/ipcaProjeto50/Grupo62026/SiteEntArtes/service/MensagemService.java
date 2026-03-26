package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.AulaDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagenPreviewDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Mensagen;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.MensagenRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MensagemService {

    private final MensagenRepository mensagenRepository;
    private final IdHasher idHasher;
    public  MensagemService(MensagenRepository mensagenRepository,IdHasher idHasher){
        this.mensagenRepository =mensagenRepository;
        this.idHasher = idHasher;
    }
    public List<MensagenPreviewDto> buscarPreviewMensagens(Integer id){
        List<Mensagen> mensagens = mensagenRepository.findAllByRemetenteIdOrDestinatarioIdOrderByEnviadaEmDesc(id, id);

        // Usamos um Map para filtrar: a chave é o "par de IDs", o valor é a Mensagem
        Map<String, Mensagen> conversasUnicas = new LinkedHashMap<>();

        for (Mensagen m : mensagens) {
            // Cria uma identificação única para a conversa (ex: "1-5")
            int id1 = m.getRemetente().getId();
            int id2 = m.getDestinatario().getId();
            String chave = Math.min(id1, id2) + "-" + Math.max(id1, id2);

            // .putIfAbsent só adiciona se a conversa ainda não estiver no mapa
            conversasUnicas.putIfAbsent(chave, m);
        }

        return converterListaMensagemParaMensagemPreviewDto(conversasUnicas.values().stream().toList());
    }



    public List<MensagenPreviewDto> converterListaMensagemParaMensagemPreviewDto(List<Mensagen> mensagens) {
        return mensagens.stream()
                .map(this::converterParaDto)
                .toList();
    }
    public MensagenPreviewDto converterParaDto(Mensagen mensagen) {
        return new MensagenPreviewDto(
                idHasher.encode(mensagen.getRemetente().getId()),
                mensagen.getRemetente().getNome(),
                mensagen.getConteudo(),
                mensagen.getEnviadaEm().getHour()+":"+mensagen.getEnviadaEm().getMinute()
        );
    }
}
