package ipcaProjeto50.Grupo62026.SiteEntArtes.service;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.UtilizadoreResumoDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.dto.NotificacoeDto;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Notificacoe;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.NotificacoeRepository;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.UtilizadoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class NotificacoesService {

    private final NotificacoeRepository notificacoeRepository;
    private final UtilizadoreRepository utilizadoreRepository; // Necessário para buscar users
    private final IdHasher idHasher;

    public Page<NotificacoeDto> findNotificacoesUtilizador(String idHasheado, Pageable pageable) {
        Integer idReal = idHasher.decode(idHasheado);
        return notificacoeRepository
                .findAllByDestinatarioIdAndLidaFalse(idReal, pageable)
                .map(this::converterParaNotificacaoDto);
    }

    @Transactional
    public void criarNotificacao(Integer destinatarioId, Integer remetenteId, String titulo, String mensagem, String tipo, String referenciaId) {
        Utilizadore destinatario = utilizadoreRepository.findById(destinatarioId)
                .orElseThrow(() -> new RuntimeException("Destinatário não encontrado"));

        Utilizadore remetente = null;
        if (remetenteId != null) {
            remetente = utilizadoreRepository.findById(remetenteId).orElse(null);
        }

        Notificacoe nova = new Notificacoe();
        nova.setDestinatario(destinatario);
        nova.setRemetente(remetente);
        nova.setTitulo(titulo);
        nova.setMensagem(mensagem);
        nova.setTipo(tipo);
        nova.setReferenciaId(referenciaId);
        nova.setLida(false);
        nova.setCriadaEm(Instant.now());

        notificacoeRepository.save(nova);
    }

    public NotificacoeDto converterParaNotificacaoDto(Notificacoe notificacoe) {
        return new NotificacoeDto(
                idHasher.encode(notificacoe.getId()),
                new UtilizadoreResumoDto(
                        idHasher.encode(notificacoe.getDestinatario().getId()),
                        notificacoe.getDestinatario().getNome()
                ),
                notificacoe.getRemetente() != null ?
                        new UtilizadoreResumoDto(
                                idHasher.encode(notificacoe.getRemetente().getId()),
                                notificacoe.getRemetente().getNome()
                        ) : null,
                notificacoe.getTitulo(),
                notificacoe.getMensagem(),
                notificacoe.getTipo(),
                notificacoe.getReferenciaId(),
                notificacoe.getLida(),
                notificacoe.getCriadaEm()
        );
    }

    @Transactional
    public void marcarComoLida(String idHasheado) {
        Integer idReal = idHasher.decode(idHasheado);
        Notificacoe notificacao = notificacoeRepository.findById(idReal)
                .orElseThrow(() -> new EntityNotFoundException("Notificação não encontrada"));

        notificacao.setLida(true);
        notificacoeRepository.save(notificacao);
    }
}