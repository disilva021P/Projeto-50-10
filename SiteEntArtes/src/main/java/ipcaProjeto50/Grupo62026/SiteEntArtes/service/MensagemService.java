package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Mensagen;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.MensagensGrupo;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class MensagemService {
    private final MensagensGrupoService mensagensTurmaService;
    private final UtilizadoreRepository utilizadoreRepository;
    private final MensagensGrupoRepository mensagensGrupoRepository;
    private final GrupoRepository grupoRepository;
    private final MensagenRepository mensagenRepository;
    private final IdHasher idHasher;
    private final UtilizadorLogRepository utilizadorLogRepository;
    private final NotificacoesService notificacoesService;

    private static final Logger logger = LoggerFactory.getLogger(MensagemService.class);

    public List<MensagenPreviewDto> buscarPreviewMensagens(String idUser) {
        Integer id = idHasher.decode(idUser);
        List<MensagenPreviewDto> previews = new ArrayList<>();

        if (!utilizadoreRepository.existsById(id)) {
            throw new EntityNotFoundException("Utilizador não encontrado com o ID fornecido.");
        }

        List<Mensagen> mensagensPrivadas = mensagenRepository
                .findAllByRemetenteIdOrDestinatarioIdOrderByEnviadaEmDesc(id, id);

        Map<String, Mensagen> conversasUnicas = new LinkedHashMap<>();
        for (Mensagen m : mensagensPrivadas) {
            int id1 = m.getRemetente().getId();
            int id2 = m.getDestinatario().getId();
            String chave = Math.min(id1, id2) + "-" + Math.max(id1, id2);
            conversasUnicas.putIfAbsent(chave, m);
        }

        conversasUnicas.values().stream()
                .map(m -> converterParaPreviewDto(m, id))
                .forEach(previews::add);

        List<ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Grupo> meusGrupos =
                grupoRepository.findByMembros_Id(id);

        for (ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Grupo g : meusGrupos) {
            Optional<MensagensGrupo> ultimaMsg = mensagensGrupoRepository
                    .findFirstByGrupoIdOrderByEnviadaEmDesc(g.getId());

            previews.add(new MensagenPreviewDto(
                    "GRUPO_" + idHasher.encode(g.getId()),
                    g.getNome(),
                    ultimaMsg.map(MensagensGrupo::getConteudo).orElse("Novo grupo criado!"),
                    ultimaMsg.map(MensagensGrupo::getEnviadaEm).orElse(g.getCriadoEm()),
                    true,
                    idHasher.encode(g.getCriador().getId())
            ));
        }

        return previews.stream()
                .sorted(Comparator.comparing(MensagenPreviewDto::horas).reversed())
                .toList();
    }

    public MensagenDto criar(String idUser, MensagemCriarDto mensagenDto) throws Exception {
        Utilizadore remetente = utilizadoreRepository.findById(idHasher.decode(idUser))
                .orElseThrow(() -> new EntityNotFoundException("Remetente com o id fornecido não encontrado"));

        Utilizadore destinatario = utilizadoreRepository.findById(idHasher.decode(mensagenDto.destinatario()))
                .orElseThrow(() -> new EntityNotFoundException("Destinatario com o id devolvido, não encontrado"));

        if (ehAlunoMenor(remetente)) {
            throw new IllegalArgumentException("Alunos menores de idade não podem enviar mensagens diretas.");
        }

        if (ehAlunoMenor(destinatario)) {
            throw new IllegalArgumentException("Não é permitido enviar mensagens diretas para alunos menores de idade.");
        }

        Mensagen novaMensagem = mensagenRepository.save(new Mensagen(
                null,
                remetente,
                destinatario,
                mensagenDto.conteudo(),
                LocalDateTime.now()
        ));

        notificacoesService.criarNotificacao(
                destinatario.getId(),
                remetente.getId(),
                "Nova Mensagem",
                mensagenDto.conteudo(),
                "MENSAGEM",
                idHasher.encode(remetente.getId())
        );

        return this.converterParaDto(novaMensagem);
    }

    public void eliminar(String id) {
        mensagenRepository.deleteById(idHasher.decode(id));
    }

    public List<MensagenDto> mensagensConversa(String idUser, String idConversa) throws Exception {
        Utilizadore utilizadore = utilizadoreRepository.findById(idHasher.decode(idUser))
                .orElseThrow(() -> new Exception("Utilizador com o email não encontrado"));

        return mensagenRepository.findChatHistory(utilizadore.getId(), idHasher.decode(idConversa))
                .stream()
                .map(this::converterParaDto)
                .toList();
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
                false,
                null
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

    public List<MensagenDto> mensagensConversaGrupo(String idUserHashed, String idGrupoHashed) throws Exception {
        Integer userId = idHasher.decode(idUserHashed);
        Integer grupoId = idHasher.decode(idGrupoHashed);

        utilizadoreRepository.findById(userId)
                .orElseThrow(() -> new Exception("Utilizador não encontrado"));

        return mensagensGrupoRepository.findByGrupoIdOrderByEnviadaEmAsc(grupoId)
                .stream()
                .map(this::converterGrupoParaDto)
                .toList();
    }

    public MensagenDto criarMensagemGrupo(String idUserHashed, MensagemGrupoCriarDto dto) {
        Integer userId = idHasher.decode(idUserHashed);
        Integer grupoId = idHasher.decode(dto.grupoId());

        Utilizadore remetente = utilizadoreRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Remetente não encontrado"));

        ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado"));

        MensagensGrupo novaMensagem = new MensagensGrupo();
        novaMensagem.setGrupo(grupo);
        novaMensagem.setRemetente(remetente);
        novaMensagem.setConteudo(dto.conteudo());
        novaMensagem.setEnviadaEm(LocalDateTime.now());

        MensagensGrupo salva = mensagensGrupoRepository.save(novaMensagem);

        grupo.getMembros().forEach(membro -> {
            if (!membro.getId().equals(userId)) {
                try {
                    notificacoesService.criarNotificacao(
                            membro.getId(),
                            remetente.getId(),
                            "Grupo: " + grupo.getNome(),
                            remetente.getNome() + ": " + dto.conteudo(),
                            "MENSAGEM_GRUPO",
                            "GRUPO_" + idHasher.encode(grupo.getId())
                    );
                } catch (Exception e) {
                    logger.warn("Falha ao enviar notificação para membro {}: {}", membro.getId(), e.getMessage());
                }
            }
        });

        return converterGrupoParaDto(salva);
    }

    public MensagenDto converterGrupoParaDto(MensagensGrupo m) {
        return new MensagenDto(
                idHasher.encode(m.getId()),
                new UtilizadoreResumoDto(
                        idHasher.encode(m.getRemetente().getId()),
                        m.getRemetente().getNome()
                ),
                null,
                m.getConteudo(),
                m.getEnviadaEm()
        );
    }

    private boolean ehAlunoMenor(Utilizadore utilizador) {
        return utilizador.getTipo().getId() == 3
                && utilizador.getDataNascimento() != null
                && LocalDate.now().getYear() - utilizador.getDataNascimento().getYear() < 18;
    }
}