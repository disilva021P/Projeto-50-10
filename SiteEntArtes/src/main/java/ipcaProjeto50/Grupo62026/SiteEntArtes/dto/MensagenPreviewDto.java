package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Mensagen}
 */
public record MensagenPreviewDto(String id, String nome, String conteudo, LocalDateTime horas,boolean isTurma) implements Serializable {

}