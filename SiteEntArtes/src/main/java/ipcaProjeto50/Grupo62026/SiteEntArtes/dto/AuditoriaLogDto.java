package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link ipcaProjeto50.Grupo62026.SiteEntArtes.entity.AuditoriaLog}
 */
public record AuditoriaLogDto(String id, UtilizadoreResumoDto idUtilizador, String acao,
                              LocalDateTime criadoEm) implements Serializable {
}