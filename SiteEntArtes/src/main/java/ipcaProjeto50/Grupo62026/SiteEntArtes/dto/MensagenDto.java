package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Mensagen;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link Mensagen}
 */
public record MensagenDto(String id, UtilizadoreResumoDto remetente, UtilizadoreResumoDto destinatario, String conteudo,
                          LocalDateTime enviadaEm) implements Serializable {
}