package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagenPreviewDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Mensagen;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.MensagensTurma;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.MensagensTurmaRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MensagensTurmaService {

    private final MensagensTurmaRepository mensagemTurmaRepository;
    private final UtilizadoreRepository utilizadoreRepository;
    private final IdHasher idHasher;

    public List<MensagenPreviewDto> buscarPreviewGrupos(String userId) {

        List<MensagensTurma> ultimas = mensagemTurmaRepository
                .findMessagesByAlunoTurma(idHasher.decode(userId));

        return ultimas.stream()
                .collect(Collectors.toMap(
                        m -> m.getTurma().getId(), // Chave: ID da Turma
                        m -> m,                    // Valor: A mensagem
                        (existente, nova) -> existente // Se houver duplicado, mantém a existente (que é a mais recente devido ao DESC)
                ))
                .values() // Pega apenas as mensagens únicas
                .stream()
                .map(m -> converterParaPreviewDto(m, idHasher.decode(userId)))
                .toList();
    }
    public MensagenPreviewDto converterParaPreviewDto(MensagensTurma mensagen, Integer currentUserId) {
        if (mensagen == null) return null;

        return new MensagenPreviewDto(
                "TURMA_"+idHasher.encode(mensagen.getId()),
                mensagen.getTurma().getNome(),
                mensagen.getConteudo(),
                mensagen.getEnviadaEm(),
                true
        );
    }
}