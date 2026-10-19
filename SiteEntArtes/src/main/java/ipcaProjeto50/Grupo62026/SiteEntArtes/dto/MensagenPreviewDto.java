package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record MensagenPreviewDto(
        String id,
        String nome,
        String conteudo,
        LocalDateTime horas,
        boolean isTurma,
        String criadorId
) implements Serializable { }