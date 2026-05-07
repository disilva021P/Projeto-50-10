package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.MensagenPreviewDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.MensagensGrupo;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.MensagensGrupoRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MensagensGrupoService {

    private final MensagensGrupoRepository mensagensGrupoRepository;
    private final IdHasher idHasher;

    public List<MensagenPreviewDto> buscarPreviewGrupos(String userIdHashed) {
        Integer userId = idHasher.decode(userIdHashed);

        // Usamos a query que criámos no repository para buscar a última mensagem
        // de cada grupo onde o utilizador é membro
        List<MensagensGrupo> ultimas = mensagensGrupoRepository.findUltimasMensagensPorMembro(userId);

        return ultimas.stream()
                .map(this::converterParaPreviewDto)
                .toList();
    }

    public MensagenPreviewDto converterParaPreviewDto(MensagensGrupo mensagem) {
        if (mensagem == null) return null;

        // Agora acedemos ao Grupo, e não à Turma diretamente
        return new MensagenPreviewDto(
                "GRUPO_" + idHasher.encode(mensagem.getGrupo().getId()),
                mensagem.getGrupo().getNome(),
                mensagem.getConteudo(),
                mensagem.getEnviadaEm(),
                true // isGroup
        );
    }
}