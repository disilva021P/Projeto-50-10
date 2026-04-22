package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;
import java.time.Instant;

public record NotificacoeDto(
        String id,
        UtilizadoreResumoDto destinatario,
        UtilizadoreResumoDto remetente, // Novo
        String titulo,
        String mensagem,
        String tipo,       // Novo
        String referenciaId, // Novo
        Boolean lida,
        Instant criadaEm
) implements Serializable { }