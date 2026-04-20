package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Mensagen;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.MensagensGrupo;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.MensagenRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.MensagensGrupoRepository;
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
    private final MensagensGrupoService mensagensTurmaService;
    private final UtilizadoreRepository utilizadoreRepository;
    private final MensagensGrupoRepository mensagensGrupoRepository;
    private final MensagenRepository mensagenRepository;
    private final IdHasher idHasher;
    private final UtilizadorLogRepository utilizadorLogRepository;

    public List<MensagenPreviewDto> buscarPreviewMensagens(String idUser) {
        Integer id = idHasher.decode(idUser);
        List<MensagenPreviewDto> previews = new ArrayList<>();

        if (!utilizadoreRepository.existsById(id))
            throw new EntityNotFoundException("Utilizador não encontrado com o ID fornecido.");

        // ── 1. Conversas individuais ──
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

        // ── 2. Grupos e Turmas (Unificados) ──
        // Aqui usamos o novo repository que criámos para as mensagens de grupo
        List<MensagensGrupo> ultimasMsgGrupos = mensagensGrupoRepository.findUltimasMensagensPorMembro(id);

        for (MensagensGrupo mg : ultimasMsgGrupos) {
            previews.add(new MensagenPreviewDto(
                    "GRUPO_" + idHasher.encode(mg.getGrupo().getId()), // Prefixo para o Front saber que é grupo
                    mg.getGrupo().getNome(),
                    mg.getConteudo(),
                    mg.getEnviadaEm(),
                    true // isTurma/isGrupo = true
            ));
        }

        // ── 3. Ordenação Final ──
        return previews.stream()
                .sorted(Comparator.comparing(MensagenPreviewDto::horas).reversed())
                .toList();
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


    // --- MÉTODOS PARA GRUPOS ---

    public List<MensagenDto> mensagensConversaGrupo(String emailUser, String idGrupoHashed) {
        // 1. Buscar o utilizador pelo email (principal do token)
        Utilizadore utilizadore = utilizadoreRepository.findByEmail(emailUser)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        Integer grupoId = idHasher.decode(idGrupoHashed);

        // 2. Opcional: Validar se o utilizador pertence ao grupo antes de mostrar as mensagens
        // (Isso dá segurança extra ao teu sistema)

        // 3. Buscar mensagens do grupo ordenadas por data
        return mensagensGrupoRepository.findByGrupoIdOrderByEnviadaEmAsc(grupoId)
                .stream()
                .map(this::converterGrupoParaDto)
                .toList();
    }

    public MensagenDto criarMensagemGrupo(String emailUser, MensagemGrupoCriarDto dto) {
        Utilizadore remetente = utilizadoreRepository.findByEmail(emailUser)
                .orElseThrow(() -> new EntityNotFoundException("Remetente não encontrado"));

        Integer grupoId = idHasher.decode(dto.grupoId());

        // Buscar o Grupo (podes injetar o GrupoRepository ou usar o do MensagensGrupo)
        ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Grupo grupo = new ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Grupo();
        grupo.setId(grupoId); // Criação de referência rápida ou busca no repo

        MensagensGrupo novaMensagem = new MensagensGrupo();
        novaMensagem.setGrupo(grupo);
        novaMensagem.setRemetente(remetente);
        novaMensagem.setConteudo(dto.conteudo());
        novaMensagem.setEnviadaEm(LocalDateTime.now());

        return converterGrupoParaDto(mensagensGrupoRepository.save(novaMensagem));
    }

    // Mapper específico para Mensagens de Grupo -> MensagenDto
    public MensagenDto converterGrupoParaDto(MensagensGrupo m) {
        return new MensagenDto(
                idHasher.encode(m.getId()),
                new UtilizadoreResumoDto(
                        idHasher.encode(m.getRemetente().getId()),
                        m.getRemetente().getNome()
                ),
                null, // Destinatário é nulo pois é um grupo
                m.getConteudo(),
                m.getEnviadaEm()
        );
    }
}