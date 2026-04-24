package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Notificacoe;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.NotificacoeDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Pagamento;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.NotificacoeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import java.util.List;

@RequiredArgsConstructor
@Service
public class NotificacoesService {
    private final NotificacoeRepository notificacoeRepository;
    private final IdHasher idHasher;
    List<NotificacoeDto> findNotificacoesUtilizador(String id){
        return notificacoeRepository.findAllByUtilizadorIdOrderByCriadaEmDesc(idHasher.decode(id)).stream().map(this::converterParaNotificacaoDto).toList();
    }
    List<NotificacoeDto> findNotificacoesUtilizador(Integer id){
        return notificacoeRepository.findAllByUtilizadorIdOrderByCriadaEmDesc(id).stream().map(this::converterParaNotificacaoDto).toList();
    }
    // Versão com ID Hasher (String)
    public Page<NotificacoeDto> findNotificacoesUtilizador(String id, Pageable pageable) {
        return notificacoeRepository
                .findAllByUtilizadorId(idHasher.decode(id), pageable)
                .map(this::converterParaNotificacaoDto);
    }

    // Versão com Integer
    public Page<NotificacoeDto> findNotificacoesUtilizador(Integer id, Pageable pageable) {
        return notificacoeRepository
                .findAllByUtilizadorId(id, pageable)
                .map(this::converterParaNotificacaoDto);
    }
    NotificacoeDto converterParaNotificacaoDto(Notificacoe notificacoe){
        return new NotificacoeDto(
                idHasher.encode(notificacoe.getId()),
                new UtilizadoreResumoDto(idHasher.encode(notificacoe.getUtilizador().getId()),notificacoe.getUtilizador().getNome()),
                notificacoe.getTitulo(),
                notificacoe.getMensagem(),
                notificacoe.getLida(),
                notificacoe.getCriadaEm()
        );
    }

}
