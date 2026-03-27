package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Mensagen;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.MensagenRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadorLogRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MensagemService {
    private final UtilizadoreRepository utilizadoreRepository;
    private final MensagenRepository mensagenRepository;
    private final IdHasher idHasher;
    private final UtilizadorLogRepository utilizadorLogRepository;

    public  MensagemService(MensagenRepository mensagenRepository,IdHasher idHasher, UtilizadoreRepository utilizadoreRepository,
                            UtilizadorLogRepository utilizadorLogRepository){
        this.mensagenRepository =mensagenRepository;
        this.idHasher = idHasher;
        this.utilizadorLogRepository = utilizadorLogRepository;
        this.utilizadoreRepository = utilizadoreRepository;
    }
    public List<MensagenPreviewDto> buscarPreviewMensagens(String idUser){
        Integer id = idHasher.decode(idUser);
        boolean exists = utilizadoreRepository.existsById(id);

        if(!exists) throw new EntityNotFoundException("Utilizador não encontrado com o ID fornecido.");

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
        return conversasUnicas.values().stream()
                .map(m -> converterParaPreviewDto(m, id))
                .toList();    }
    public MensagenDto criar(String idUser, MensagemCriarDto mensagenDto){
        Utilizadore remetente = utilizadoreRepository.findById(idHasher.decode(idUser))
                .orElseThrow(() -> new EntityNotFoundException("Remetente com o id fornecido não encontrado"));
        Utilizadore destinatario = utilizadoreRepository.findById(idHasher.decode(mensagenDto.destinatario()))
                .orElseThrow(() -> new EntityNotFoundException("Destinatario com o id devolvido, não encontrado"));

        return this.converterParaDto(mensagenRepository.save(new Mensagen(
                null,
                remetente,
                destinatario,
                mensagenDto.conteudo(),
                LocalDateTime.now()
                )));
        //TODO: CRIA NOTIFICAÇÃO
    }
    public void eliminar(String id){
        mensagenRepository.deleteById(idHasher.decode(id));
    }
    public List<MensagenDto> mensagensConversa(String idUser, String idConversa ){
        Utilizadore utilizadore = utilizadoreRepository.findById(idHasher.decode(idUser))
                .orElseThrow(() -> new RuntimeException("Utilizador com o email não encontrado"));

        return mensagenRepository.findChatHistory(utilizadore.getId(),idHasher.decode(idConversa)).stream().map(this::converterParaDto).toList();
    }
    public MensagenPreviewDto converterParaPreviewDto(Mensagen mensagen, Integer currentUserId) {
        if (mensagen == null) return null;

        Utilizadore outro = mensagen.getRemetente().getId().equals(currentUserId)
                ? mensagen.getDestinatario()
                : mensagen.getRemetente();

        return new MensagenPreviewDto(
                idHasher.encode(outro.getId()),
                outro.getNome(),
                mensagen.getConteudo(),
                mensagen.getEnviadaEm()
        );
    }
    public MensagenDto converterParaDto(Mensagen mensagen) {
        if (mensagen == null) return null;
        return new MensagenDto(
                idHasher.encode(mensagen.getId()),
                new UtilizadoreResumoDto(
                        idHasher.encode(mensagen.getRemetente().getId()),
                        mensagen.getRemetente().getNome()
                ),
                new UtilizadoreResumoDto(
                        idHasher.encode(mensagen.getDestinatario().getId()),
                        mensagen.getDestinatario().getNome()
                ),
                mensagen.getConteudo(),
                mensagen.getEnviadaEm()
        );
    }
}
