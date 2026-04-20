package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Aula;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Mensagen;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.MensagensTurma;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.MensagenRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadorLogRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
@RequiredArgsConstructor
@Service
public class MensagemService {
    private final MensagensTurmaService mensagensTurmaService;
    private final UtilizadoreRepository utilizadoreRepository;
    private final MensagenRepository mensagenRepository;
    private final IdHasher idHasher;
    private final UtilizadorLogRepository utilizadorLogRepository;

    public List<MensagenPreviewDto> buscarPreviewMensagens(String idUser) {
        Integer id = idHasher.decode(idUser);

        if (!utilizadoreRepository.existsById(id))
            throw new EntityNotFoundException("Utilizador não encontrado com o ID fornecido.");

        List<MensagenPreviewDto> previews = new ArrayList<>();

        // ── 1. Conversas individuais (lógica original) ────────────────────────────
        List<Mensagen> mensagens = mensagenRepository
                .findAllByRemetenteIdOrDestinatarioIdOrderByEnviadaEmDesc(id, id);

        Map<String, Mensagen> conversasUnicas = new LinkedHashMap<>();
        for (Mensagen m : mensagens) {
            int id1 = m.getRemetente().getId();
            int id2 = m.getDestinatario().getId();
            String chave = Math.min(id1, id2) + "-" + Math.max(id1, id2);
            conversasUnicas.putIfAbsent(chave, m);
        }

        conversasUnicas.values().stream()
                .map(m -> converterParaPreviewDto(m, id))
                .forEach(previews::add)
        ;
        previews.addAll(mensagensTurmaService.buscarPreviewGrupos(idUser));
        return previews.stream().sorted(Comparator.comparing(MensagenPreviewDto::horas).reversed()).toList();
    }
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
                mensagen.getEnviadaEm(),
                false
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