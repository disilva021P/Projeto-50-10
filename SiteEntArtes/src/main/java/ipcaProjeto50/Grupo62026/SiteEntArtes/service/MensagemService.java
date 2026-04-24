package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.*;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Mensagen;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.MensagensGrupo;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.*;
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
    private final GrupoRepository grupoRepository;
    private final MensagenRepository mensagenRepository;
    private final IdHasher idHasher;
    private final UtilizadorLogRepository utilizadorLogRepository;
    private final NotificacoesService notificacoesService;

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

        // ── 2. Grupos (Corrigido para incluir grupos sem mensagens) ──

        // Injetar o GrupoRepository no teu Service para usar aqui:
        // List<Grupo> meusGrupos = grupoRepository.findAllByMembrosId(id);

        // Como alternativa, se o teu mensagensGrupoRepository conseguir buscar grupos:
        List<ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Grupo> meusGrupos =
                grupoRepository.findByMembros_Id(id); // Precisas de criar este método no GrupoRepository

        for (ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Grupo g : meusGrupos) {
            // Tenta encontrar a última mensagem deste grupo específico
            Optional<MensagensGrupo> ultimaMsg = mensagensGrupoRepository
                    .findFirstByGrupoIdOrderByEnviadaEmDesc(g.getId());

            if (ultimaMsg.isPresent()) {
                // Se tem mensagem, mostra a mensagem
                previews.add(new MensagenPreviewDto(
                        "GRUPO_" + idHasher.encode(g.getId()),
                        g.getNome(),
                        ultimaMsg.get().getConteudo(),
                        ultimaMsg.get().getEnviadaEm(),
                        true
                ));
            } else {
                // SE NÃO TEM MENSAGEM: Mostra o grupo com texto de boas-vindas
                // Usamos a data de criação do grupo ou a data atual para não dar erro no DTO
                previews.add(new MensagenPreviewDto(
                        "GRUPO_" + idHasher.encode(g.getId()),
                        g.getNome(),
                        "Novo grupo criado!",
                        LocalDateTime.now(), // Ou g.getCriadoEm() se tiveres esse campo
                        true
                ));
            }
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

        Mensagen novaMensagem = mensagenRepository.save(new Mensagen(
                null,
                remetente,
                destinatario,
                mensagenDto.conteudo(),
                LocalDateTime.now()
        ));

        // --- DISPARAR NOTIFICAÇÃO ---
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


    public void eliminar(String id){
        mensagenRepository.deleteById(idHasher.decode(id));
    }


    public List<MensagenDto> mensagensConversa(String idUser, String idConversa ){
        Utilizadore utilizadore = utilizadoreRepository.findById(idHasher.decode(idUser))
                .orElseThrow(() -> new Exception("Utilizador com o email não encontrado"));

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

    public List<MensagenDto> mensagensConversaGrupo(String idUserHashed, String idGrupoHashed) {
        // 1. Descodificar IDs
        Integer userId = idHasher.decode(idUserHashed);
        Integer grupoId = idHasher.decode(idGrupoHashed);

        // 2. Buscar o utilizador pelo ID real
        Utilizadore utilizadore = utilizadoreRepository.findById(userId)
                .orElseThrow(() -> new Exception("Utilizador não encontrado"));

        // 3. Buscar mensagens do grupo
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

        // --- DISPARAR NOTIFICAÇÕES PARA O GRUPO ---
        grupo.getMembros().forEach(membro -> {
            // Não notificar a própria pessoa que enviou
            if (!membro.getId().equals(userId)) {
                notificacoesService.criarNotificacao(
                        membro.getId(),
                        remetente.getId(),
                        "Grupo: " + grupo.getNome(),
                        remetente.getNome() + ": " + dto.conteudo(),
                        "MENSAGEM_GRUPO",
                        "GRUPO_" + idHasher.encode(grupo.getId()) // Referência para abrir o grupo
                );
            }
        });

        return converterGrupoParaDto(salva);
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